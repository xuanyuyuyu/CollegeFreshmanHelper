package com.example.collegefreshmanhelper.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_qa_trace")
public class KnowledgeQaTrace {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("source_type")
    private Integer sourceType;

    @TableField("source_post_id")
    private Long sourcePostId;

    @TableField("source_reply_id")
    private Long sourceReplyId;

    @TableField("contributor_user_id")
    private Long contributorUserId;

    @TableField("created_by_admin_id")
    private Long createdByAdminId;

    @TableField("source_post_title_snapshot")
    private String sourcePostTitleSnapshot;

    @TableField("source_post_content_snapshot")
    private String sourcePostContentSnapshot;

    @TableField("source_reply_content_snapshot")
    private String sourceReplyContentSnapshot;

    @TableField("source_file_name")
    private String sourceFileName;

    @TableField("question_text")
    private String questionText;

    @TableField("answer_text")
    private String answerText;

    private String category;

    @TableField("qa_json")
    private String qaJson;

    @TableField("context_text")
    private String contextText;

    @TableField("collection_name")
    private String collectionName;

    @TableField("qdrant_point_id")
    private String qdrantPointId;

    @TableField("vector_dimension")
    private Integer vectorDimension;

    @TableField("embedding_model")
    private String embeddingModel;

    @TableField("llm_model")
    private String llmModel;

    @TableField("like_count_snapshot")
    private Integer likeCountSnapshot;

    @TableField("reward_points")
    private Integer rewardPoints;

    private Integer status;

    @TableField("fail_reason")
    private String failReason;

    @TableField("synced_at")
    private LocalDateTime syncedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
