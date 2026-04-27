package com.example.collegefreshmanhelper.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ForumPostCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private List<String> imageUrls;

    private String tags;
}
