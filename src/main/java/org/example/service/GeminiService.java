package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.embedding-model}")
    private String embeddingModel;

    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public List<Double> createEmbedding(String text) {
        Map response = webClient.post()
                .uri("/models/" + embeddingModel + ":embedContent?key=" + apiKey)
                .bodyValue(Map.of(
                        "model", "models/" + embeddingModel,
                        "content", Map.of(
                                "parts", List.of(Map.of("text", text))
                        )
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<String, Object> embedding =
                (Map<String, Object>) response.get("embedding");

        return (List<Double>) embedding.get("values");
    }
}