package org.example.controller;

import org.example.service.RagService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RagService ragService;

    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        String pageContext = request.getOrDefault("pageContext", "");

        String answer = ragService.answer(message, pageContext);

        return Map.of("answer", answer);
    }

    @GetMapping("/starters")
    public Map<String, Object> starters(@RequestParam(defaultValue = "books-list") String pageContext) {
        return Map.of("starters", ragService.getStarters(pageContext));
    }

    @PostMapping("/rebuild")
    public Map<String, String> rebuildVectorDatabase() {
        ragService.rebuildVectorDatabase();
        return Map.of("status", "Vector database rebuilt from RDF/XML data.");
    }
}