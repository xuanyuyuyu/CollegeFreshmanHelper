package com.example.collegefreshmanhelper;

import com.example.collegefreshmanhelper.admin.mapper.AdminOperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.collegefreshmanhelper.forum.mapper.ForumPostImageMapper;
import com.example.collegefreshmanhelper.forum.mapper.ForumPostMapper;
import com.example.collegefreshmanhelper.forum.mapper.ForumReplyMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.collegefreshmanhelper.user.mapper.SysUserMapper;
import com.example.collegefreshmanhelper.user.mapper.UserStatsMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.profiles.active=test")
class CollegeFreshmanHelperApplicationTests {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SysUserMapper sysUserMapper;

    @MockitoBean
    private ForumPostMapper forumPostMapper;

    @MockitoBean
    private ForumPostImageMapper forumPostImageMapper;

    @MockitoBean
    private ForumReplyMapper forumReplyMapper;

    @MockitoBean
    private UserStatsMapper userStatsMapper;

    @MockitoBean
    private AdminOperationLogMapper adminOperationLogMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldSerializeLongIdsAsStrings() throws Exception {
        String json = objectMapper.writeValueAsString(new SampleIdPayload(2048703262591692801L, 1000000000000000001L));
        assertThat(json).contains("\"id\":\"2048703262591692801\"");
        assertThat(json).contains("\"parentId\":\"1000000000000000001\"");
    }

    private record SampleIdPayload(Long id, long parentId) {
    }

}
