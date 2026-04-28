package com.example.collegefreshmanhelper.forum.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumAuthorVO {

    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String title;
}
