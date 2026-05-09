package org.example.model;

public class GraphNode {

    private String id;
    private String label;

    public GraphNode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}