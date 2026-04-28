package com.example.collegefreshmanhelper.forum.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeToggleVO {

    private Boolean liked;
    private Integer likeCount;
}
