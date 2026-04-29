package com.example.collegefreshmanhelper.rag.service;

import com.example.collegefreshmanhelper.rag.dto.AssistantAskRequest;
import com.example.collegefreshmanhelper.rag.vo.AssistantAnswerVO;
import com.example.collegefreshmanhelper.rag.vo.AssistantStreamChunkVO;

import java.util.function.Consumer;

public interface RagQueryService {

    AssistantAnswerVO ask(AssistantAskRequest request);

    void askStream(AssistantAskRequest request, Consumer<AssistantStreamChunkVO> chunkConsumer);
}
