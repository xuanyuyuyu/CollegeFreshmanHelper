package com.example.collegefreshmanhelper.rag.controller;

import com.example.collegefreshmanhelper.common.model.ApiResponse;
import com.example.collegefreshmanhelper.rag.dto.AssistantAskRequest;
import com.example.collegefreshmanhelper.rag.service.RagQueryService;
import com.example.collegefreshmanhelper.rag.vo.AssistantAnswerVO;
import com.example.collegefreshmanhelper.rag.vo.AssistantStreamChunkVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assistant")
public class AssistantController {

    private final RagQueryService ragQueryService;
    private final ObjectMapper objectMapper;

    @PostMapping("/ask")
    public ApiResponse<AssistantAnswerVO> ask(@Valid @RequestBody AssistantAskRequest request) {
        return ApiResponse.success(ragQueryService.ask(request));
    }

    @PostMapping(value = "/ask/stream", produces = "application/x-ndjson")
    public StreamingResponseBody askStream(
            @Valid @RequestBody AssistantAskRequest request,
            HttpServletResponse response
    ) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/x-ndjson;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        return outputStream -> {
            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                ragQueryService.askStream(request, chunk -> writeChunk(writer, chunk));
            }
        };
    }

    private void writeChunk(Writer writer, AssistantStreamChunkVO chunk) {
        try {
            writer.write(objectMapper.writeValueAsString(chunk));
            writer.write('\n');
            writer.flush();
        } catch (Exception exception) {
            throw new IllegalStateException("写出流式响应失败: " + exception.getMessage(), exception);
        }
    }
}
