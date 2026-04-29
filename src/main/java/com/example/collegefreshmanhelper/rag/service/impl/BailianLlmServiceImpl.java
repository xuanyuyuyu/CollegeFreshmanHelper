package com.example.collegefreshmanhelper.rag.service.impl;

import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.rag.config.BailianProperties;
import com.example.collegefreshmanhelper.rag.service.BailianLlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class BailianLlmServiceImpl implements BailianLlmService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final BailianProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isAvailable() {
        return properties.isEnabled()
                && properties.isLlmEnabled()
                && StringUtils.hasText(properties.getApiKey());
    }

    @Override
    public String answerFromSources(String question, List<String> sources) {
        if (!isAvailable() || !StringUtils.hasText(question) || sources == null || sources.isEmpty()) {
            return null;
        }

        JsonNode root = invoke("/chat/completions", buildPayload(question, sources, false));
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        String answer = content.isMissingNode() ? null : content.asText();
        return StringUtils.hasText(answer) ? answer.trim() : null;
    }

    @Override
    public String streamAnswerFromSources(String question, List<String> sources, Consumer<String> chunkConsumer) {
        if (!isAvailable() || !StringUtils.hasText(question) || sources == null || sources.isEmpty()) {
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(buildPayload(question, sources, true)),
                            StandardCharsets.UTF_8
                    ))
                    .build();

            HttpResponse<java.io.InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new BusinessException("调用百炼 LLM 失败: HTTP " + response.statusCode() + " " + errorBody);
            }

            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.startsWith("data:")) {
                        continue;
                    }
                    String data = trimmed.substring(5).trim();
                    if (!StringUtils.hasText(data)) {
                        continue;
                    }
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    String content = extractDeltaContent(objectMapper.readTree(data));
                    if (!StringUtils.hasText(content)) {
                        continue;
                    }
                    builder.append(content);
                    if (chunkConsumer != null) {
                        chunkConsumer.accept(content);
                    }
                }
            }

            String answer = builder.toString().trim();
            return StringUtils.hasText(answer) ? answer : null;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("调用百炼 LLM 失败: " + exception.getMessage());
        }
    }

    private String buildPrompt(String question, List<String> sources) {
        StringBuilder builder = new StringBuilder();
        builder.append("用户问题：").append(question.trim()).append("\n\n");
        builder.append("知识片段：\n");
        int index = 1;
        for (String source : sources) {
            builder.append(index).append(". ").append(source).append("\n");
            index += 1;
        }
        builder.append("\n请根据以上知识片段回答。如果片段不足以支持明确结论，请直接说知识不足。");
        return builder.toString();
    }

    private Map<String, Object> buildPayload(String question, List<String> sources, boolean stream) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", properties.getLlmModel());
        payload.put("temperature", properties.getTemperature());
        payload.put("stream", stream);
        payload.put("messages", List.of(
                Map.of(
                        "role", "system",
                        "content", "你是河北工程大学新生入学帮助助手。请严格依据提供的知识片段回答，不要编造未出现的事实。若知识不足，请明确说明知识不足。请使用清晰、简洁、对新生友好的中文。"
                ),
                Map.of(
                        "role", "user",
                        "content", buildPrompt(question, sources)
                )
        ));
        return payload;
    }

    private String extractDeltaContent(JsonNode root) {
        JsonNode deltaNode = root.path("choices").path(0).path("delta").path("content");
        if (deltaNode.isTextual()) {
            return deltaNode.asText();
        }
        if (deltaNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : deltaNode) {
                if (item.isTextual()) {
                    builder.append(item.asText());
                    continue;
                }
                JsonNode textNode = item.path("text");
                if (textNode.isTextual()) {
                    builder.append(textNode.asText());
                }
            }
            return builder.toString();
        }
        return null;
    }

    private JsonNode invoke(String path, Map<String, Object> payload) {
        try {
            String body = RestClient.builder()
                    .baseUrl(properties.getBaseUrl())
                    .build()
                    .post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(body);
        } catch (Exception exception) {
            throw new BusinessException("调用百炼 LLM 失败: " + exception.getMessage());
        }
    }
}
