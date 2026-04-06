package com.resumebuilder.backend.model;

public class Education {
    private int id;
    private int resumeId;
    private String degree;
    private String university;
    private String year;
    private String cgpa;

    public Education() {}

    public Education(String degree, String university, String year, String cgpa) {
        this.degree = degree;
        this.university = university;
        this.year = year;
        this.cgpa = cgpa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }
    
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
    
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    
    public String getCgpa() { return cgpa; }
    public void setCgpa(String cgpa) { this.cgpa = cgpa; }
}
