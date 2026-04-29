package com.example.collegefreshmanhelper.user.service;

import java.util.Collection;

public interface UserStatsSyncService {

    void syncKnowledgeStatsForUsers(Collection<Long> userIds);
}
