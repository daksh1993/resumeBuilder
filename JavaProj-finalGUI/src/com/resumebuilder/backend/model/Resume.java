package com.resumebuilder.backend.model;

import com.resumebuilder.backend.customsection.AbstractCustomSection;

import java.util.ArrayList;
import java.util.List;

public class Resume {
    private int id;
    private int userId;
    private String title;
    private String templateName;
    private String colorTheme;
    
    // Personal Info
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String summary;
    private String photoPath;
    
    // Child Sections
    private List<Education> educationList;
    private List<Experience> experienceList;
    private List<Project> projectList;
    private List<Skill> skillList;
    private List<AbstractCustomSection> customSections;

    public Resume() {
        this.educationList = new ArrayList<>();
        this.experienceList = new ArrayList<>();
        this.projectList = new ArrayList<>();
        this.skillList = new ArrayList<>();
        this.customSections = new ArrayList<>();
        // Defaults
        this.title = "My Resume";
        this.templateName = "Classic";
        this.colorTheme = "Blue";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    
    public String getColorTheme() { return colorTheme; }
    public void setColorTheme(String colorTheme) { this.colorTheme = colorTheme; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    
    public List<Education> getEducationList() { return educationList; }
    public void setEducationList(List<Education> educationList) { this.educationList = educationList; }
    
    public List<Experience> getExperienceList() { return experienceList; }
    public void setExperienceList(List<Experience> experienceList) { this.experienceList = experienceList; }
    
    public List<Project> getProjectList() { return projectList; }
    public void setProjectList(List<Project> projectList) { this.projectList = projectList; }
    
    public List<Skill> getSkillList() { return skillList; }
    public void setSkillList(List<Skill> skillList) { this.skillList = skillList; }
    
    public List<AbstractCustomSection> getCustomSections() { return customSections; }
    public void setCustomSections(List<AbstractCustomSection> customSections) {
        this.customSections = customSections != null ? customSections : new ArrayList<AbstractCustomSection>();
    }
}
