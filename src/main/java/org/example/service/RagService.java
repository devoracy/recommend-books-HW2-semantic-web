package org.example.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagService {

    private final RdfService rdfService;
    private final GeminiService geminiService;
    private final ChromaService chromaService;
    private final OpenRouterService openRouterService;

    public RagService(RdfService rdfService,
                      GeminiService geminiService,
                      ChromaService chromaService,
                      OpenRouterService openRouterService) {
        this.rdfService = rdfService;
        this.geminiService = geminiService;
        this.chromaService = chromaService;
        this.openRouterService = openRouterService;
    }

    public void rebuildVectorDatabase() {
        List<String> chunks = rdfService.getBookChunksForRag();

        List<String> ids = new ArrayList<>();
        List<List<Double>> embeddings = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            ids.add("chunk-" + i);
            embeddings.add(geminiService.createEmbedding(chunks.get(i)));
        }

        chromaService.clearCollection();
        chromaService.addDocuments(ids, chunks, embeddings);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildVectorDatabaseOnStartup() {
        try {
            rebuildVectorDatabase();
        } catch (Exception e) {
            System.out.println("Vector database was not rebuilt on startup: " + e.getMessage());
        }
    }

    public String answer(String question, String pageContext) {
        String improvedQuestion = question;

        if (pageContext != null
                && !pageContext.isBlank()
                && !pageContext.equals("books-list")
                && !pageContext.equals("home")) {

            improvedQuestion = "Current page book: " + pageContext + ". User question: " + question;
        }

        List<Double> questionEmbedding = geminiService.createEmbedding(improvedQuestion);
        List<String> relevantChunks = chromaService.query(questionEmbedding, 4);

        return openRouterService.chat(improvedQuestion, relevantChunks);
    }

    public List<String> getStarters(String pageContext) {
        if (pageContext != null
                && !pageContext.isBlank()
                && !pageContext.equals("books-list")
                && !pageContext.equals("home")) {

            return List.of(
                    "Who is the author of this book?",
                    "What themes does this book have?",
                    "What reading level is this book suitable for?"
            );
        }

        return List.of(
                "What is a book that I am most likely to enjoy from this list?",
                "Which books have the theme Science Fiction?",
                "What book has the author Frank Herbert and the theme Science Fiction?"
        );
    }
}
