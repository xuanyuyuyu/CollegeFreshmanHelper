package com.example.collegefreshmanhelper.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rag.bailian")
public class BailianProperties {

    /**
     * 总开关，关闭时完全回退到本地文本召回。
     */
    private boolean enabled = true;

    /**
     * 百炼 API Key。
     */
    private String apiKey;

    /**
     * OpenAI 兼容模式基础地址。
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * 聊天模型。
     */
    private String llmModel = "qwen-plus";

    /**
     * Embedding 模型。
     */
    private String embeddingModel = "text-embedding-v4";

    /**
     * 是否启用 LLM 生成。
     */
    private boolean llmEnabled = true;

    /**
     * 是否启用 Embedding 语义重排。
     */
    private boolean embeddingEnabled = true;

    /**
     * 文本召回后进入语义重排的候选数。
     */
    private int embeddingCandidateLimit = 10;

    /**
     * LLM 参与回答时最多拼接的来源数。
     */
    private int llmSourceLimit = 4;

    /**
     * 生成温度。
     */
    private double temperature = 0.2D;
}
