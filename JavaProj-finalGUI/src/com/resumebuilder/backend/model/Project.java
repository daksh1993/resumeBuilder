package com.resumebuilder.backend.model;

public class Project {
    private int id;
    private int resumeId;
    private String title;
    private String description;

    public Project() {}

    public Project(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
