package com.resumebuilder.backend.model;

public class Skill {
    private int id;
    private int resumeId;
    private String skillName;

    public Skill() {}

    public Skill(String skillName) {
        this.skillName = skillName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }
    
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
}
