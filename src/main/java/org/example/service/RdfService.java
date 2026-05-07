package org.example.service;

import org.example.model.Book;
import org.example.model.GraphEdge;
import org.example.model.GraphNode;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class RdfService {

    private static final String NS = "http://example.org/book-recommendation#";
    private static final String RDF_FILE = "src/main/resources/data/books.rdf";

    private Model model;

    public RdfService() {
        loadModel();
    }

    public void loadModel() {
        model = ModelFactory.createDefaultModel();

        try (InputStream inputStream = new FileInputStream(RDF_FILE)) {
            model.read(inputStream, null, "RDF/XML");
        } catch (Exception e) {
            throw new RuntimeException("Could not load RDF file", e);
        }
    }

    public void saveModel() {
        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            model.write(outputStream, "RDF/XML");
        } catch (Exception e) {
            throw new RuntimeException("Could not save RDF file", e);
        }
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();

        Resource bookClass = model.createResource(NS + "Book");
        ResIterator iterator = model.listResourcesWithProperty(RDF.type, bookClass);

        while (iterator.hasNext()) {
            Resource bookResource = iterator.nextResource();
            books.add(resourceToBook(bookResource));
        }

        return books;
    }

    public Book getBookById(String id) {
        Resource bookResource = model.getResource(NS + id);

        if (!model.contains(bookResource, RDF.type, model.createResource(NS + "Book"))) {
            return null;
        }

        return resourceToBook(bookResource);
    }

    public void addBook(Book book) {
        String id = createId(book.getTitle());

        Resource bookResource = model.createResource(NS + id);
        Resource bookClass = model.createResource(NS + "Book");

        Property title = model.createProperty(NS + "title");
        Property author = model.createProperty(NS + "author");
        Property hasTheme = model.createProperty(NS + "hasTheme");
        Property level = model.createProperty(NS + "suitableForLevel");

        bookResource.addProperty(RDF.type, bookClass);
        bookResource.addProperty(title, book.getTitle());
        bookResource.addProperty(author, book.getAuthor());
        bookResource.addProperty(level, model.createResource(NS + createId(book.getReadingLevel())));

        for (String theme : book.getThemes()) {
            bookResource.addProperty(hasTheme, model.createResource(NS + createId(theme.trim())));
        }

        saveModel();
    }

    public void updateReadingLevel(String bookId, String newLevel) {
        Resource bookResource = model.getResource(NS + bookId);
        Property level = model.createProperty(NS + "suitableForLevel");

        model.removeAll(bookResource, level, null);
        bookResource.addProperty(level, model.createResource(NS + createId(newLevel)));

        saveModel();
    }

    public void uploadRdfFile(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), Path.of(RDF_FILE), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            loadModel();
        } catch (Exception e) {
            throw new RuntimeException("Could not upload RDF file", e);
        }
    }

    public List<GraphNode> getGraphNodes() {
        Set<String> nodeIds = new HashSet<>();
        List<GraphNode> nodes = new ArrayList<>();

        StmtIterator iterator = model.listStatements();

        while (iterator.hasNext()) {
            Statement statement = iterator.nextStatement();

            String subject = shortName(statement.getSubject().toString());
            if (nodeIds.add(subject)) {
                nodes.add(new GraphNode(subject, subject));
            }

            RDFNode object = statement.getObject();
            String objectName = object.isResource()
                    ? shortName(object.asResource().toString())
                    : object.toString();

            if (nodeIds.add(objectName)) {
                nodes.add(new GraphNode(objectName, objectName));
            }
        }

        return nodes;
    }

    public List<GraphEdge> getGraphEdges() {
        List<GraphEdge> edges = new ArrayList<>();
        StmtIterator iterator = model.listStatements();

        while (iterator.hasNext()) {
            Statement statement = iterator.nextStatement();

            String subject = shortName(statement.getSubject().toString());
            String predicate = shortName(statement.getPredicate().toString());

            RDFNode object = statement.getObject();
            String objectName = object.isResource()
                    ? shortName(object.asResource().toString())
                    : object.toString();

            edges.add(new GraphEdge(subject, objectName, predicate));
        }

        return edges;
    }

    public List<String> getBookChunksForRag() {
        List<String> chunks = new ArrayList<>();

        for (Book book : getAllBooks()) {
            String chunk = "Book: " + book.getTitle()
                    + ". Author: " + book.getAuthor()
                    + ". Themes: " + String.join(", ", book.getThemes())
                    + ". Reading level: " + book.getReadingLevel() + ".";

            chunks.add(chunk);
        }

        chunks.add("User: Alice. Preferred theme: Science Fiction. Reading level: Intermediate.");
        chunks.add("User: Bob. Preferred theme: Mystery. Reading level: Beginner.");

        return chunks;
    }

    private Book resourceToBook(Resource resource) {
        Property title = model.createProperty(NS + "title");
        Property author = model.createProperty(NS + "author");
        Property hasTheme = model.createProperty(NS + "hasTheme");
        Property level = model.createProperty(NS + "suitableForLevel");

        Book book = new Book();
        book.setId(shortName(resource.toString()));

        Statement titleStmt = resource.getProperty(title);
        Statement authorStmt = resource.getProperty(author);
        Statement levelStmt = resource.getProperty(level);

        book.setTitle(titleStmt != null ? rdfNodeToDisplayValue(titleStmt.getObject()) : "");
        book.setAuthor(authorStmt != null ? rdfNodeToDisplayValue(authorStmt.getObject()) : "");
        book.setReadingLevel(levelStmt != null ? rdfNodeToDisplayValue(levelStmt.getObject()) : "");

        List<String> themes = new ArrayList<>();
        StmtIterator themeIterator = resource.listProperties(hasTheme);
        while (themeIterator.hasNext()) {
            themes.add(rdfNodeToDisplayValue(themeIterator.nextStatement().getObject()));
        }

        book.setThemes(themes);

        return book;
    }

    private String shortName(String uri) {
        if (uri.contains("#")) {
            return uri.substring(uri.indexOf("#") + 1);
        }
        return uri;
    }

    private String rdfNodeToDisplayValue(RDFNode node) {
        if (node.isResource()) {
            Resource resource = node.asResource();
            Statement nameStmt = resource.getProperty(model.createProperty(NS + "name"));
            return nameStmt != null ? nameStmt.getString() : shortName(resource.toString());
        }
        return node.asLiteral().getString();
    }

    private String createId(String title) {
        return title.replaceAll("[^a-zA-Z0-9]", "");
    }
}
