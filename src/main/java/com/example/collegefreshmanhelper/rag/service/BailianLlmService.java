package com.example.collegefreshmanhelper.rag.service;

import java.util.List;
import java.util.function.Consumer;

public interface BailianLlmService {

    boolean isAvailable();

    String answerFromSources(String question, List<String> sources);

    String streamAnswerFromSources(String question, List<String> sources, Consumer<String> chunkConsumer);
}
