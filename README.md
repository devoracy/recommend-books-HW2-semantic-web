# Book Recommendation System (RDF + OWL + RAG Chatbot)

This project implements a semantic book recommendation system using RDF, OWL, SPARQL, and a Retrieval-Augmented Generation (RAG) chatbot.

It combines:
- Semantic Web technologies (RDF, OWL, SPARQL)
- Java (Spring Boot + Jena)
- Vector database (ChromaDB)
- LLM integration (OpenRouter / Gemini)


## Features

### 1. RDF/XML Knowledge Base
- Models users, books, genres, and reading levels
- Example:
  - Alice → Intermediate, Science Fiction
  - Bob → Beginner, Mystery
  - Books: Dune, Hunger Games, The Silent Patient

### 2. RDF Graph Visualization
- Upload RDF/XML file
- Visualize graph using Java (JUNG / RDF API)

### 3. Book Management (Jena API)
- Add new book (e.g. Harry Potter)
- Modify existing book (e.g. change reading level)
- RDF used for persistence

### 4. Book Listing & Pages
- List all books from RDF
- Individual page for each book

### 5. OWL Ontology
- Defines:
  - Classes: User, Book, Theme, ReadingLevel
  - Object properties: prefersTheme, hasTheme
  - Data properties: readingLevel
- Visualized in:
  - Protégé OR GraphDB

### 6. SPARQL Queries
- 5 queries saved in `sparql_owl.txt`
- Example:
  - Recommended books for a user
  - Books by theme
  - Books by reading level

### 7. RAG Chatbot (Main Feature)


#### Features implemented:
- Floating chat UI
- Context-aware conversation starters
- Retrieval-Augmented Generation (RAG)
- Query by author + theme
- Answers based ONLY on your data


#### Architecture:
RDF/XML - Jena - Text chunks - Embeddings (Gemini)
- ChromaDB (vector DB)
- Similarity search
- LLM (OpenRouter)
- Answer


## Technologies Used

- Java 17+
- Spring Boot
- Apache Jena
- RDF / OWL / SPARQL
- ChromaDB (vector database)
- Google Gemini (embeddings)
- OpenRouter (LLM API)
- HTML / JavaScript (chat UI)


## Contributors

### Dan Maria-Andrada
### Grec Carina-Gabriela
