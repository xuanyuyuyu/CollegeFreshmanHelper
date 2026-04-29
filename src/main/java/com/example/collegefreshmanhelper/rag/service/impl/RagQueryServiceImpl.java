package com.example.collegefreshmanhelper.rag.service.impl;

import com.example.collegefreshmanhelper.admin.entity.KnowledgeQaTrace;
import com.example.collegefreshmanhelper.knowledge.service.KnowledgeTraceService;
import com.example.collegefreshmanhelper.rag.config.BailianProperties;
import com.example.collegefreshmanhelper.rag.dto.AssistantAskRequest;
import com.example.collegefreshmanhelper.rag.service.BailianEmbeddingService;
import com.example.collegefreshmanhelper.rag.service.BailianLlmService;
import com.example.collegefreshmanhelper.rag.service.RagQueryService;
import com.example.collegefreshmanhelper.rag.vo.AssistantAnswerVO;
import com.example.collegefreshmanhelper.rag.vo.AssistantSourceVO;
import com.example.collegefreshmanhelper.rag.vo.AssistantStreamChunkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RagQueryServiceImpl implements RagQueryService {

    private static final int MAX_MATCHED_SOURCES = 5;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[\\s,.;:!?，。；：！？、/\\\\()（）\\[\\]【】\"'“”‘’]+");

    private final KnowledgeTraceService knowledgeTraceService;
    private final BailianProperties bailianProperties;
    private final BailianEmbeddingService bailianEmbeddingService;
    private final BailianLlmService bailianLlmService;

    @Override
    public AssistantAnswerVO ask(AssistantAskRequest request) {
        ResolvedAnswer resolved = resolve(request);

        AssistantAnswerVO result = new AssistantAnswerVO();
        result.setQuestion(resolved.question());
        result.setMatchedCount(resolved.topMatches().size());
        result.setFallback(resolved.topMatches().isEmpty());
        result.setSources(resolved.topMatches().stream().map(item -> toSource(item.knowledge())).toList());
        result.setAnswer(resolved.topMatches().isEmpty()
                ? buildFallbackAnswer(resolved.category())
                : buildAnswer(resolved.question(), resolved.category(), resolved.topMatches()));
        return result;
    }

    @Override
    public void askStream(AssistantAskRequest request, Consumer<AssistantStreamChunkVO> chunkConsumer) {
        ResolvedAnswer resolved = resolve(request);
        List<AssistantSourceVO> sources = resolved.topMatches().stream().map(item -> toSource(item.knowledge())).toList();

        chunkConsumer.accept(metaChunk(resolved.question(), resolved.topMatches().size(), resolved.topMatches().isEmpty(), sources));

        try {
            String answer;
            if (resolved.topMatches().isEmpty()) {
                answer = buildFallbackAnswer(resolved.category());
                streamText(answer, chunkConsumer);
            } else {
                answer = buildAnswerStream(resolved.question(), resolved.category(), resolved.topMatches(), chunkConsumer);
            }
            chunkConsumer.accept(doneChunk(resolved.question(), answer, resolved.topMatches().size(), resolved.topMatches().isEmpty(), sources));
        } catch (Exception exception) {
            chunkConsumer.accept(errorChunk(exception.getMessage()));
        }
    }

    private ResolvedAnswer resolve(AssistantAskRequest request) {
        String question = normalize(request.getQuestion());
        String category = normalize(request.getCategory());
        List<ScoredKnowledge> scoredKnowledge = rerankWithEmbeddingIfPossible(searchKnowledge(question, category), question);
        List<ScoredKnowledge> topMatches = scoredKnowledge.stream()
                .filter(item -> item.score() > 0)
                .limit(MAX_MATCHED_SOURCES)
                .toList();
        return new ResolvedAnswer(question, category, topMatches);
    }

    private List<ScoredKnowledge> rerankWithEmbeddingIfPossible(List<ScoredKnowledge> scoredKnowledge, String question) {
        if (!bailianEmbeddingService.isAvailable() || scoredKnowledge.isEmpty() || !StringUtils.hasText(question)) {
            return scoredKnowledge;
        }

        List<ScoredKnowledge> positive = scoredKnowledge.stream()
                .filter(item -> item.score() > 0)
                .limit(Math.max(1, bailianProperties.getEmbeddingCandidateLimit()))
                .toList();
        if (positive.isEmpty()) {
            return scoredKnowledge;
        }

        List<String> texts = new ArrayList<>();
        texts.add(question);
        for (ScoredKnowledge item : positive) {
            texts.add(resolveContext(item.knowledge()));
        }

        List<List<Double>> vectors = bailianEmbeddingService.embedTexts(texts);
        if (vectors.size() != texts.size()) {
            return scoredKnowledge;
        }

        List<Double> questionVector = vectors.get(0);
        List<ScoredKnowledge> reranked = new ArrayList<>();
        for (int index = 0; index < positive.size(); index++) {
            double similarity = cosineSimilarity(questionVector, vectors.get(index + 1));
            int enhancedScore = positive.get(index).score() + (int) Math.round(similarity * 100);
            reranked.add(new ScoredKnowledge(positive.get(index).knowledge(), enhancedScore));
        }
        reranked.sort(Comparator
                .comparingInt(ScoredKnowledge::score).reversed()
                .thenComparing((ScoredKnowledge item) -> defaultZero(item.knowledge().getLikeCountSnapshot()), Comparator.reverseOrder()));
        return reranked;
    }

    private List<ScoredKnowledge> searchKnowledge(String question, String category) {
        String normalizedQuestion = simplify(question);
        Set<String> terms = buildTerms(question);
        return knowledgeTraceService.listEnabledKnowledge().stream()
                .filter(item -> matchesCategory(item, category))
                .map(item -> new ScoredKnowledge(item, score(item, normalizedQuestion, terms, category)))
                .sorted(Comparator
                        .comparingInt(ScoredKnowledge::score).reversed()
                        .thenComparing((ScoredKnowledge item) -> defaultZero(item.knowledge().getLikeCountSnapshot()), Comparator.reverseOrder())
                        .thenComparing((ScoredKnowledge item) -> item.knowledge().getCreatedAt(), Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .toList();
    }

    private boolean matchesCategory(KnowledgeQaTrace knowledge, String category) {
        if (!StringUtils.hasText(category)) {
            return true;
        }
        return simplify(knowledge.getCategory()).contains(simplify(category));
    }

    private int score(KnowledgeQaTrace knowledge, String normalizedQuestion, Set<String> terms, String category) {
        String questionText = simplify(knowledge.getQuestionText());
        String answerText = simplify(knowledge.getAnswerText());
        String contextText = simplify(resolveContext(knowledge));
        String categoryText = simplify(knowledge.getCategory());

        int score = 0;
        if (StringUtils.hasText(normalizedQuestion)) {
          if (questionText.contains(normalizedQuestion)) {
              score += 140;
          }
          if (answerText.contains(normalizedQuestion) || contextText.contains(normalizedQuestion)) {
              score += 100;
          }
        }

        for (String term : terms) {
            if (!StringUtils.hasText(term)) {
                continue;
            }
            if (questionText.contains(term)) {
                score += 18;
            }
            if (answerText.contains(term) || contextText.contains(term)) {
                score += 10;
            }
            if (categoryText.contains(term)) {
                score += 8;
            }
        }

        if (StringUtils.hasText(category) && categoryText.contains(simplify(category))) {
            score += 25;
        }
        score += Math.min(defaultZero(knowledge.getLikeCountSnapshot()), 20);
        score += Math.min(defaultZero(knowledge.getRewardPoints()), 10);
        return score;
    }

    private Set<String> buildTerms(String question) {
        Set<String> terms = new LinkedHashSet<>();
        String normalized = normalize(question);
        if (!StringUtils.hasText(normalized)) {
            return terms;
        }
        for (String part : SPLIT_PATTERN.split(normalized)) {
            String token = simplify(part);
            if (token.length() >= 2) {
                terms.add(token);
            }
        }
        String compact = simplify(normalized);
        if (compact.length() >= 2) {
            int maxWindow = Math.min(4, compact.length());
            for (int size = maxWindow; size >= 2; size--) {
                for (int index = 0; index + size <= compact.length(); index++) {
                    terms.add(compact.substring(index, index + size));
                    if (terms.size() >= 12) {
                        return terms;
                    }
                }
            }
        }
        return terms;
    }

    private String buildAnswer(String question, String category, List<ScoredKnowledge> matches) {
        String llmAnswer = buildAnswerByLlmIfPossible(question, matches);
        if (StringUtils.hasText(llmAnswer)) {
            return llmAnswer;
        }
        List<String> lines = new ArrayList<>();
        lines.add("根据当前知识库，先给你一个可直接参考的回答：");
        lines.add("");

        KnowledgeQaTrace primary = matches.get(0).knowledge();
        lines.add("1. " + compact(primary.getAnswerText()));

        if (matches.size() > 1) {
            lines.add("");
            lines.add("补充参考：");
            int index = 2;
            for (ScoredKnowledge match : matches.subList(1, matches.size())) {
                lines.add(index + ". " + compact(match.knowledge().getAnswerText()));
                index += 1;
            }
        }

        lines.add("");
        lines.add("当前回答基于知识库中的相近问答片段整理而成。");
        lines.add("如果你想确认细节或你的情况更特殊，可以继续追问，或结合下方来源回看原始知识条目。");
        return String.join("\n", lines);
    }

    private String buildAnswerStream(String question, String category, List<ScoredKnowledge> matches, Consumer<AssistantStreamChunkVO> chunkConsumer) {
        if (bailianLlmService.isAvailable()) {
            try {
                List<String> sources = matches.stream()
                        .limit(Math.max(1, bailianProperties.getLlmSourceLimit()))
                        .map(item -> formatSourceForLlm(item.knowledge()))
                        .toList();
                String streamed = bailianLlmService.streamAnswerFromSources(question, sources, chunk -> {
                    if (StringUtils.hasText(chunk)) {
                        chunkConsumer.accept(deltaChunk(chunk));
                    }
                });
                if (StringUtils.hasText(streamed)) {
                    return streamed;
                }
            } catch (Exception ignored) {
                // 流式 LLM 失败时降级到本地拼接答案，避免前端一直无内容。
            }
        }
        String fallbackAnswer = buildAnswer(question, category, matches);
        streamText(fallbackAnswer, chunkConsumer);
        return fallbackAnswer;
    }

    private String buildAnswerByLlmIfPossible(String question, List<ScoredKnowledge> matches) {
        if (!bailianLlmService.isAvailable() || matches == null || matches.isEmpty()) {
            return null;
        }
        List<String> sources = matches.stream()
                .limit(Math.max(1, bailianProperties.getLlmSourceLimit()))
                .map(item -> formatSourceForLlm(item.knowledge()))
                .toList();
        return bailianLlmService.answerFromSources(question, sources);
    }

    private String buildFallbackAnswer(String category) {
        if (StringUtils.hasText(category)) {
            return "我暂时没有在当前知识库里找到足够明确的「" + category + "」相关答案。你可以换一种问法继续提问，或者去论坛发帖补充更具体的场景。";
        }
        return "我暂时没有在当前知识库里找到足够明确的答案。你可以换一种更具体的问法继续提问，或者去论坛发帖咨询学长学姐。";
    }

    private void streamText(String text, Consumer<AssistantStreamChunkVO> chunkConsumer) {
        if (!StringUtils.hasText(text)) {
            return;
        }
        int step = 24;
        for (int index = 0; index < text.length(); index += step) {
            int end = Math.min(index + step, text.length());
            chunkConsumer.accept(deltaChunk(text.substring(index, end)));
        }
    }

    private AssistantStreamChunkVO metaChunk(String question, int matchedCount, boolean fallback, List<AssistantSourceVO> sources) {
        AssistantStreamChunkVO chunk = new AssistantStreamChunkVO();
        chunk.setType("meta");
        chunk.setQuestion(question);
        chunk.setMatchedCount(matchedCount);
        chunk.setFallback(fallback);
        chunk.setSources(sources);
        return chunk;
    }

    private AssistantStreamChunkVO deltaChunk(String content) {
        AssistantStreamChunkVO chunk = new AssistantStreamChunkVO();
        chunk.setType("delta");
        chunk.setContent(content);
        return chunk;
    }

    private AssistantStreamChunkVO doneChunk(String question, String answer, int matchedCount, boolean fallback, List<AssistantSourceVO> sources) {
        AssistantStreamChunkVO chunk = new AssistantStreamChunkVO();
        chunk.setType("done");
        chunk.setQuestion(question);
        chunk.setAnswer(answer);
        chunk.setMatchedCount(matchedCount);
        chunk.setFallback(fallback);
        chunk.setSources(sources);
        return chunk;
    }

    private AssistantStreamChunkVO errorChunk(String message) {
        AssistantStreamChunkVO chunk = new AssistantStreamChunkVO();
        chunk.setType("error");
        chunk.setMessage(message);
        return chunk;
    }

    private AssistantSourceVO toSource(KnowledgeQaTrace knowledge) {
        AssistantSourceVO source = new AssistantSourceVO();
        source.setKnowledgeId(knowledge.getId());
        source.setQuestionText(knowledge.getQuestionText());
        source.setAnswerText(compact(knowledge.getAnswerText()));
        source.setCategory(knowledge.getCategory());
        source.setSourcePostId(knowledge.getSourcePostId());
        source.setSourceReplyId(knowledge.getSourceReplyId());
        source.setContributorUserId(knowledge.getContributorUserId());
        source.setSourceLabel(resolveSourceLabel(knowledge));
        return source;
    }

    private String resolveSourceLabel(KnowledgeQaTrace knowledge) {
        if (knowledge.getSourceReplyId() != null) {
            return "来源于论坛回复";
        }
        if (knowledge.getSourcePostId() != null) {
            return "来源于论坛帖子";
        }
        if (knowledge.getCreatedByAdminId() != null) {
            return "后台手动录入";
        }
        return "知识库条目";
    }

    private String resolveContext(KnowledgeQaTrace knowledge) {
        if (StringUtils.hasText(knowledge.getContextText())) {
            return knowledge.getContextText();
        }
        return "Q: " + normalize(knowledge.getQuestionText()) + "\nA: " + normalize(knowledge.getAnswerText());
    }

    private String formatSourceForLlm(KnowledgeQaTrace knowledge) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(knowledge.getCategory())) {
            builder.append("分类：").append(knowledge.getCategory()).append("；");
        }
        builder.append("问题：").append(normalize(knowledge.getQuestionText())).append("；");
        builder.append("答案：").append(normalize(knowledge.getAnswerText()));
        return builder.toString();
    }

    private String compact(String text) {
        return normalize(text).replaceAll("\\s+", " ");
    }

    private String normalize(String text) {
        return StringUtils.hasText(text) ? text.trim() : "";
    }

    private String simplify(String text) {
        return normalize(text).toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double cosineSimilarity(List<Double> left, List<Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty() || left.size() != right.size()) {
            return 0D;
        }
        double dot = 0D;
        double leftNorm = 0D;
        double rightNorm = 0D;
        for (int index = 0; index < left.size(); index++) {
            double leftValue = left.get(index);
            double rightValue = right.get(index);
            dot += leftValue * rightValue;
            leftNorm += leftValue * leftValue;
            rightNorm += rightValue * rightValue;
        }
        if (leftNorm <= 0D || rightNorm <= 0D) {
            return 0D;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private record ScoredKnowledge(KnowledgeQaTrace knowledge, int score) {
    }

    private record ResolvedAnswer(String question, String category, List<ScoredKnowledge> topMatches) {
    }
}
