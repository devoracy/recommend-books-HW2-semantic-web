package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ChromaService {

    private final WebClient webClient;

    @Value("${chroma.collection}")
    private String collectionName;

    private String collectionId;

    private final String tenant = "default_tenant";
    private final String database = "default_database";

    public ChromaService(@Value("${chroma.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void createCollectionIfNeeded() {
        try {
            Map response = webClient.get()
                    .uri("/api/v2/tenants/" + tenant + "/databases/" + database + "/collections/" + collectionName)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            collectionId = response.get("id").toString();

        } catch (Exception e) {
            Map response = webClient.post()
                    .uri("/api/v2/tenants/" + tenant + "/databases/" + database + "/collections")
                    .bodyValue(Map.of(
                            "name", collectionName,
                            "get_or_create", true
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            collectionId = response.get("id").toString();
        }
    }

    public void clearCollection() {
        try {
            webClient.delete()
                    .uri("/api/v2/tenants/" + tenant + "/databases/" + database + "/collections/" + collectionName)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception ignored) {
        }

        collectionId = null;
        createCollectionIfNeeded();
    }

    public void addDocuments(List<String> ids,
                             List<String> documents,
                             List<List<Double>> embeddings) {
        createCollectionIfNeeded();

        webClient.post()
                .uri("/api/v2/tenants/" + tenant + "/databases/" + database + "/collections/" + collectionId + "/upsert")
                .bodyValue(Map.of(
                        "ids", ids,
                        "documents", documents,
                        "embeddings", embeddings
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public List<String> query(List<Double> queryEmbedding, int numberOfResults) {
        createCollectionIfNeeded();

        Map response = webClient.post()
                .uri("/api/v2/tenants/" + tenant + "/databases/" + database + "/collections/" + collectionId + "/query")
                .bodyValue(Map.of(
                        "query_embeddings", List.of(queryEmbedding),
                        "n_results", numberOfResults,
                        "include", List.of("documents")
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<List<String>> documents =
                (List<List<String>>) response.get("documents");

        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return documents.get(0);
    }
}
