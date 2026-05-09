package org.example.controller;

import org.example.service.RdfService;
import org.example.service.RagService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
public class GraphController {

    private final RdfService rdfService;
    private final RagService ragService;

    public GraphController(RdfService rdfService, RagService ragService) {
        this.rdfService = rdfService;
        this.ragService = ragService;
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadRdf(@RequestParam("file") MultipartFile file) {
        rdfService.uploadRdfFile(file);
        ragService.rebuildVectorDatabase();
        return "redirect:/graph";
    }

    @GetMapping("/graph")
    public String graphPage(Model model) {
        model.addAttribute("nodes", rdfService.getGraphNodes());
        model.addAttribute("edges", rdfService.getGraphEdges());
        return "graph";
    }

    @GetMapping("/api/graph")
    @ResponseBody
    public Map<String, Object> graphJson() {
        return Map.of(
                "nodes", rdfService.getGraphNodes(),
                "edges", rdfService.getGraphEdges()
        );
    }
}
