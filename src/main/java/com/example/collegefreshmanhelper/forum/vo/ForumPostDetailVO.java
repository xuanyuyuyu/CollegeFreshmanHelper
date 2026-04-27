package com.example.collegefreshmanhelper.forum.vo;

import com.example.collegefreshmanhelper.forum.entity.ForumPost;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ForumPostDetailVO {

    private ForumPost post;
    private List<String> imageUrls;
}
