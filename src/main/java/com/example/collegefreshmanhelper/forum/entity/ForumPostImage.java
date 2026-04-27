package com.example.collegefreshmanhelper.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.collegefreshmanhelper.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("forum_post_image")
@EqualsAndHashCode(callSuper = true)
public class ForumPostImage extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("sort_no")
    private Integer sortNo;

    private Integer width;
    private Integer height;
    private Integer status;
}
