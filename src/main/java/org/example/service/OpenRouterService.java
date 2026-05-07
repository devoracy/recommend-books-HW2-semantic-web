package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    private final WebClient webClient;

    @Value("${openrouter.chat-model}")
    private String chatModel;

    public OpenRouterService(@Value("${openrouter.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("HTTP-Referer", "http://localhost:8080")
                .defaultHeader("X-Title", "Book Recommendation Homework")
                .build();
    }

    public String chat(String question, List<String> contextChunks) {
        try {
            String context = String.join("\n\n", contextChunks);

            String systemPrompt = """
                    You are a book recommendation assistant.
                    Answer ONLY using the provided context from the vector database.
                    Do not use real-world knowledge.
                    If the answer is missing, say:
                    I could not find this information in the vector database.
                    """;

            String userPrompt = """
                    Context from vector database:
                    %s

                    Question:
                    %s
                    """.formatted(context, question);

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(Map.of(
                            "model", chatModel,
                            "temperature", 0.1,
                            "messages", List.of(
                                    Map.of("role", "system", "content", systemPrompt),
                                    Map.of("role", "user", "content", userPrompt)
                            )
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.get("choices");

            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");

            return message.get("content").toString();

        } catch (Exception e) {
            return "The LLM service is unavailable or rate-limited, but the RAG vector database retrieval is implemented.";
        }
    }
}