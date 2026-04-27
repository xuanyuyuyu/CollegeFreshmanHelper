package com.example.collegefreshmanhelper.forum.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ForumReplyThreadVO {

    private ForumReplyVO rootReply;
    private List<ForumReplyVO> childReplies;
}
