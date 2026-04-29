import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class ImportKnowledgeJson {

    private static final Path DATA_FILE = Path.of("src/main/resources/static/data/data.json");

    private static final String DEFAULT_DB_URL = "jdbc:mysql://127.0.0.1:3306/college_freshman_helper?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false";
    private static final String DEFAULT_DB_USERNAME = "root";
    private static final String DEFAULT_DB_PASSWORD = "12345678";

    private static final String DEFAULT_COLLECTION_NAME = "freshman_qa";
    private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-v4";
    private static final String DEFAULT_LLM_MODEL = "qwen-plus";
    private static final int DEFAULT_VECTOR_DIMENSION = 1024;

    private static final int STATUS_PENDING_VECTOR = 1;
    private static final int STATUS_QDRANT_READY = 2;
    private static final int STATUS_FAILED = 3;
    private static final int SOURCE_TYPE_SEED_IMPORT = 3;

    private static final String DEFAULT_BAILIAN_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private static final String DEFAULT_QDRANT_BASE_URL = "http://127.0.0.1:6333";
    private static final String DEFAULT_QDRANT_DISTANCE = "Cosine";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws Exception {
        if (!Files.exists(DATA_FILE)) {
            throw new IllegalStateException("找不到数据文件: " + DATA_FILE.toAbsolutePath());
        }

        List<KnowledgeSeed> seeds = OBJECT_MAPPER.readValue(
                Files.readString(DATA_FILE, StandardCharsets.UTF_8),
                new TypeReference<List<KnowledgeSeed>>() {}
        );
        if (seeds == null || seeds.isEmpty()) {
            System.out.println("数据文件为空，无需导入。");
            return;
        }

        RuntimeConfig config = RuntimeConfig.fromEnv();
        if (config.bailianApiKey() == null || config.bailianApiKey().isBlank()) {
            throw new IllegalStateException("未配置 BAILIAN_API_KEY，无法执行 Embedding 导入。");
        }

        try (Connection connection = DriverManager.getConnection(config.dbUrl(), buildProperties(config.dbUsername(), config.dbPassword()))) {
            connection.setAutoCommit(false);

            Set<String> existingKeys = loadExistingKeys(connection);
            int inserted = 0;
            int skipped = 0;
            int failed = 0;
            long nextId = Math.max(loadMaxId(connection) + 1, System.currentTimeMillis());
            boolean collectionEnsured = false;

            for (KnowledgeSeed seed : seeds) {
                String question = normalize(seed.question());
                String answer = normalize(seed.answer());
                String category = normalize(seed.category());

                if (question == null || answer == null) {
                    skipped += 1;
                    continue;
                }

                String uniqueKey = buildUniqueKey(question, answer);
                if (existingKeys.contains(uniqueKey)) {
                    skipped += 1;
                    continue;
                }

                long knowledgeId = nextId++;
                String qdrantPointId = UUID.randomUUID().toString();
                LocalDateTime now = LocalDateTime.now();

                insertPendingKnowledge(connection, knowledgeId, qdrantPointId, question, answer, category, now, config);

                try {
                    List<Double> vector = embedText(buildContext(question, answer), config);
                    if (!collectionEnsured) {
                        ensureQdrantCollection(config, vector.size());
                        collectionEnsured = true;
                    }
                    upsertPointToQdrant(knowledgeId, qdrantPointId, vector, question, answer, category, config);
                    markKnowledgeReady(connection, knowledgeId, now);
                    connection.commit();
                    existingKeys.add(uniqueKey);
                    inserted += 1;
                } catch (Exception exception) {
                    markKnowledgeFailed(connection, knowledgeId, exception.getMessage(), now);
                    connection.commit();
                    failed += 1;
                    System.err.println("导入失败，knowledgeId=" + knowledgeId + "，原因: " + exception.getMessage());
                }
            }

            System.out.println("知识导入完成。新增成功: " + inserted + "，失败: " + failed + "，跳过(重复/非法): " + skipped);
        }
    }

    private static void ensureQdrantCollection(RuntimeConfig config, int actualVectorDimension) throws Exception {
        HttpRequest getRequest = baseRequest(config.qdrantBaseUrl() + "/collections/" + config.collectionName(), config.qdrantApiKey())
                .GET()
                .build();
        HttpResponse<String> getResponse = HTTP_CLIENT.send(getRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (getResponse.statusCode() == 200) {
            JsonNode root = OBJECT_MAPPER.readTree(getResponse.body());
            JsonNode result = root.path("result");
            int currentSize = result.path("config").path("params").path("vectors").path("size").asInt();
            int pointsCount = result.path("points_count").asInt();
            if (currentSize == actualVectorDimension) {
                return;
            }
            if (pointsCount > 0) {
                throw new IllegalStateException("Qdrant collection 维度为 " + currentSize + "，但当前 embedding 维度为 " + actualVectorDimension + "，且 collection 中已有数据，不能自动重建。");
            }
            recreateQdrantCollection(config, actualVectorDimension);
            return;
        }
        if (getResponse.statusCode() != 404) {
            throw new IllegalStateException("检查 Qdrant collection 失败: HTTP " + getResponse.statusCode() + " " + getResponse.body());
        }
        createQdrantCollection(config, actualVectorDimension);
    }

    private static void recreateQdrantCollection(RuntimeConfig config, int actualVectorDimension) throws Exception {
        HttpRequest deleteRequest = baseRequest(config.qdrantBaseUrl() + "/collections/" + config.collectionName(), config.qdrantApiKey())
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = HTTP_CLIENT.send(deleteRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (deleteResponse.statusCode() < 200 || deleteResponse.statusCode() >= 300) {
            throw new IllegalStateException("重建前删除 Qdrant collection 失败: HTTP " + deleteResponse.statusCode() + " " + deleteResponse.body());
        }
        createQdrantCollection(config, actualVectorDimension);
    }

    private static void createQdrantCollection(RuntimeConfig config, int actualVectorDimension) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vectors", Map.of(
                "size", actualVectorDimension,
                "distance", config.qdrantDistance()
        ));

        HttpRequest createRequest = baseRequest(config.qdrantBaseUrl() + "/collections/" + config.collectionName(), config.qdrantApiKey())
                .PUT(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> createResponse = HTTP_CLIENT.send(createRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (createResponse.statusCode() < 200 || createResponse.statusCode() >= 300) {
            throw new IllegalStateException("创建 Qdrant collection 失败: HTTP " + createResponse.statusCode() + " " + createResponse.body());
        }
    }

    private static void insertPendingKnowledge(
            Connection connection,
            long knowledgeId,
            String qdrantPointId,
            String question,
            String answer,
            String category,
            LocalDateTime now,
            RuntimeConfig config
    ) throws Exception {
        String insertSql = """
                INSERT INTO knowledge_qa_trace (
                  id, source_type, created_by_admin_id, question_text, answer_text, category,
                  source_file_name, context_text, collection_name, qdrant_point_id, vector_dimension, embedding_model,
                  llm_model, like_count_snapshot, reward_points, status, fail_reason, synced_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setLong(1, knowledgeId);
            statement.setInt(2, SOURCE_TYPE_SEED_IMPORT);
            statement.setObject(3, null);
            statement.setString(4, question);
            statement.setString(5, answer);
            statement.setString(6, category);
            statement.setString(7, DATA_FILE.getFileName().toString());
            statement.setString(8, buildContext(question, answer));
            statement.setString(9, config.collectionName());
            statement.setString(10, qdrantPointId);
            statement.setInt(11, config.vectorDimension());
            statement.setString(12, config.embeddingModel());
            statement.setString(13, config.llmModel());
            statement.setInt(14, 0);
            statement.setInt(15, 0);
            statement.setInt(16, STATUS_PENDING_VECTOR);
            statement.setString(17, null);
            statement.setTimestamp(18, null);
            statement.setTimestamp(19, Timestamp.valueOf(now));
            statement.setTimestamp(20, Timestamp.valueOf(now));
            statement.executeUpdate();
        }
    }

    private static void markKnowledgeReady(Connection connection, long knowledgeId, LocalDateTime now) throws Exception {
        String sql = """
                UPDATE knowledge_qa_trace
                SET status = ?, fail_reason = NULL, synced_at = ?, updated_at = ?
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, STATUS_QDRANT_READY);
            statement.setTimestamp(2, Timestamp.valueOf(now));
            statement.setTimestamp(3, Timestamp.valueOf(now));
            statement.setLong(4, knowledgeId);
            statement.executeUpdate();
        }
    }

    private static void markKnowledgeFailed(Connection connection, long knowledgeId, String reason, LocalDateTime now) throws Exception {
        String sql = """
                UPDATE knowledge_qa_trace
                SET status = ?, fail_reason = ?, updated_at = ?
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, STATUS_FAILED);
            statement.setString(2, truncate(reason, 255));
            statement.setTimestamp(3, Timestamp.valueOf(now));
            statement.setLong(4, knowledgeId);
            statement.executeUpdate();
        }
    }

    private static List<Double> embedText(String text, RuntimeConfig config) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", config.embeddingModel());
        payload.put("input", List.of(text));

        HttpRequest request = baseRequest(config.bailianBaseUrl() + "/embeddings", null)
                .header("Authorization", "Bearer " + config.bailianApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("百炼 Embedding 调用失败: HTTP " + response.statusCode() + " " + response.body());
        }

        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        JsonNode embeddingNode = root.path("data").path(0).path("embedding");
        if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
            throw new IllegalStateException("百炼 Embedding 返回为空或格式异常");
        }

        List<Double> vector = new ArrayList<>(embeddingNode.size());
        for (JsonNode node : embeddingNode) {
            vector.add(node.asDouble());
        }
        return vector;
    }

    private static void upsertPointToQdrant(
            long knowledgeId,
            String qdrantPointId,
            List<Double> vector,
            String question,
            String answer,
            String category,
            RuntimeConfig config
    ) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("id", qdrantPointId);
        point.put("vector", vector);
        point.put("payload", buildQdrantPayload(knowledgeId, question, answer, category));
        payload.put("points", List.of(point));

        String url = config.qdrantBaseUrl() + "/collections/" + config.collectionName() + "/points?wait=true";
        HttpRequest request = baseRequest(url, config.qdrantApiKey())
                .PUT(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Qdrant upsert 失败: HTTP " + response.statusCode() + " " + response.body());
        }
    }

    private static Map<String, Object> buildQdrantPayload(long knowledgeId, String question, String answer, String category) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("knowledge_id", knowledgeId);
        payload.put("question_text", question);
        payload.put("answer_text", answer);
        payload.put("context_text", buildContext(question, answer));
        payload.put("category", category);
        payload.put("source_type", SOURCE_TYPE_SEED_IMPORT);
        payload.put("source_file_name", DATA_FILE.getFileName().toString());
        return payload;
    }

    private static Set<String> loadExistingKeys(Connection connection) throws Exception {
        Set<String> keys = new LinkedHashSet<>();
        String sql = "SELECT question_text, answer_text FROM knowledge_qa_trace WHERE status <> 3";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String question = normalize(resultSet.getString("question_text"));
                String answer = normalize(resultSet.getString("answer_text"));
                if (question != null && answer != null) {
                    keys.add(buildUniqueKey(question, answer));
                }
            }
        }
        return keys;
    }

    private static long loadMaxId(Connection connection) throws Exception {
        String sql = "SELECT COALESCE(MAX(id), 0) AS max_id FROM knowledge_qa_trace";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getLong("max_id");
            }
        }
        return 0L;
    }

    private static Properties buildProperties(String username, String password) {
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty("remarks", "true");
        properties.setProperty("useInformationSchema", "true");
        return properties;
    }

    private static HttpRequest.Builder baseRequest(String url, String apiKey) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json");
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("api-key", apiKey.trim());
        }
        return builder;
    }

    private static String buildContext(String question, String answer) {
        return "Q: " + question + "\nA: " + answer;
    }

    private static String buildUniqueKey(String question, String answer) {
        return question + "\n---\n" + answer;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private record KnowledgeSeed(String question, String answer, String category) {
    }

    private record RuntimeConfig(
            String dbUrl,
            String dbUsername,
            String dbPassword,
            String collectionName,
            String embeddingModel,
            String llmModel,
            int vectorDimension,
            String bailianBaseUrl,
            String bailianApiKey,
            String qdrantBaseUrl,
            String qdrantApiKey,
            String qdrantDistance
    ) {
        private static RuntimeConfig fromEnv() {
            return new RuntimeConfig(
                    env("DB_URL", DEFAULT_DB_URL),
                    env("DB_USERNAME", DEFAULT_DB_USERNAME),
                    env("DB_PASSWORD", DEFAULT_DB_PASSWORD),
                    env("QDRANT_COLLECTION_NAME", DEFAULT_COLLECTION_NAME),
                    env("BAILIAN_EMBEDDING_MODEL", DEFAULT_EMBEDDING_MODEL),
                    env("BAILIAN_LLM_MODEL", DEFAULT_LLM_MODEL),
                    Integer.parseInt(env("VECTOR_DIMENSION", String.valueOf(DEFAULT_VECTOR_DIMENSION))),
                    env("BAILIAN_BASE_URL", DEFAULT_BAILIAN_BASE_URL),
                    env("BAILIAN_API_KEY", ""),
                    env("QDRANT_BASE_URL", DEFAULT_QDRANT_BASE_URL),
                    env("QDRANT_API_KEY", ""),
                    env("QDRANT_DISTANCE", DEFAULT_QDRANT_DISTANCE)
            );
        }
    }
}
