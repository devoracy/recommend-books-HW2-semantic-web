package org.example.model;

public class GraphEdge {

    private String from;
    private String to;
    private String label;

    public GraphEdge(String from, String to, String label) {
        this.from = from;
        this.to = to;
        this.label = label;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getLabel() {
        return label;
    }
}