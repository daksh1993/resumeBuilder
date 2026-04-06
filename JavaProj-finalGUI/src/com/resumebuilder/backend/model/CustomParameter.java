package com.resumebuilder.backend.model;

public class CustomParameter {
    private int id;
    private int sectionId;
    private String name;
    private String value;

    public CustomParameter() {}

    public CustomParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public CustomParameter(int id, int sectionId, String name, String value) {
        this.id = id;
        this.sectionId = sectionId;
        this.name = name;
        this.value = value;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
