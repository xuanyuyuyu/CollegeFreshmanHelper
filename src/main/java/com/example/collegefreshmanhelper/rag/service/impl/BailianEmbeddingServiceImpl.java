package com.example.collegefreshmanhelper.rag.service.impl;

import com.example.collegefreshmanhelper.common.exception.BusinessException;
import com.example.collegefreshmanhelper.rag.config.BailianProperties;
import com.example.collegefreshmanhelper.rag.service.BailianEmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BailianEmbeddingServiceImpl implements BailianEmbeddingService {

    private final BailianProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isAvailable() {
        return properties.isEnabled()
                && properties.isEmbeddingEnabled()
                && StringUtils.hasText(properties.getApiKey());
    }

    @Override
    public List<List<Double>> embedTexts(List<String> texts) {
        if (!isAvailable() || texts == null || texts.isEmpty()) {
            return List.of();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", properties.getEmbeddingModel());
        payload.put("input", texts);
        JsonNode root = invoke("/embeddings", payload);
        JsonNode data = root.path("data");
        if (!data.isArray()) {
            throw new BusinessException("百炼 Embedding 返回格式异常");
        }

        List<List<Double>> vectors = new ArrayList<>();
        for (JsonNode item : data) {
            JsonNode embedding = item.path("embedding");
            List<Double> vector = new ArrayList<>(embedding.size());
            for (JsonNode dimension : embedding) {
                vector.add(dimension.asDouble());
            }
            vectors.add(vector);
        }
        return vectors;
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
            throw new BusinessException("调用百炼 Embedding 失败: " + exception.getMessage());
        }
    }
}
