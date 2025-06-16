package com.school.grademanager.model;

import java.util.List;

public class Teacher extends User {
    private String fullName;
    private String email;
    private List<String> classLevels;

    public Teacher(String userId, String username, String passwordHash, String fullName, String email, List<String> classLevels) {
        super(userId, username, passwordHash, Role.TEACHER);
        this.fullName = fullName;
        this.email = email;
        this.classLevels = classLevels;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<String> getClassLevels() { return classLevels; }
    public void setClassLevels(List<String> classLevels) { this.classLevels = classLevels; }
}
