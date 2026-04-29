package com.example.collegefreshmanhelper.rag.service;

import java.util.List;

public interface BailianEmbeddingService {

    boolean isAvailable();

    List<List<Double>> embedTexts(List<String> texts);
}
