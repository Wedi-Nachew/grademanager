package com.school.grademanager.model;

import java.time.LocalDate;

public class Student extends User {
    private String studentId;
    private String fullName;
    private LocalDate dob;
    private String classLevel;
    private String email;
    private String stream;
    private String language;
    private String section;

    public Student(String userId, String username, String passwordHash, String studentId, String fullName, LocalDate dob, String classLevel, String email, String stream, String language, String section) {    
        super(userId, username, passwordHash, Role.STUDENT);
        this.studentId = studentId;
        this.fullName = fullName;
        this.dob = dob;
        this.classLevel = classLevel;
        this.email = email;
        this.stream = stream;
        this.language = language;
        this.section = section;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getClassLevel() { return classLevel; }
    public void setClassLevel(String classLevel) { this.classLevel = classLevel; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email;}
    public String getStream() { return stream; }
    public void setStream(String stream) { this.stream = stream; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
}
