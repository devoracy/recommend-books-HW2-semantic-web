package org.example.controller;

import org.example.model.Book;
import org.example.service.RdfService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.example.service.RagService;

import java.util.Arrays;

@Controller
public class BookController {

    private final RdfService rdfService;
    private final RagService ragService;

    public BookController(RdfService rdfService, RagService ragService) {
        this.rdfService = rdfService;
        this.ragService = ragService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", rdfService.getAllBooks());
        return "books";
    }

    @GetMapping("/books/{id}")
    public String bookDetails(@PathVariable String id, Model model) {
        Book book = rdfService.getBookById(id);

        if (book == null) {
            return "redirect:/books";
        }

        model.addAttribute("book", book);
        return "book-details";
    }

    @GetMapping("/books/add")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "add-book";
    }

    @PostMapping("/books/add")
    public String addBook(@RequestParam String title,
                          @RequestParam String author,
                          @RequestParam String themes,
                          @RequestParam String readingLevel) {

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setThemes(Arrays.asList(themes.split(",")));
        book.setReadingLevel(readingLevel);

        rdfService.addBook(book);
        ragService.rebuildVectorDatabase();

        return "redirect:/books";
    }

    @PostMapping("/books/{id}/reading-level")
    public String updateReadingLevel(@PathVariable String id,
                                     @RequestParam String readingLevel) {
        rdfService.updateReadingLevel(id, readingLevel);
        ragService.rebuildVectorDatabase();
        return "redirect:/books/" + id;
    }
}