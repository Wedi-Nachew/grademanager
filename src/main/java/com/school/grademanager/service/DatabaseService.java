package com.school.grademanager.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {
    private static DatabaseService instance;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/grademanager";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private DatabaseService() {
        // Private constructor to prevent instantiation
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Static method for backward compatibility
    public static Connection getConnection() throws SQLException {
        return getInstance().createConnection();
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (Exception ignored) {}
        try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
        try { if (conn != null) conn.close(); } catch (Exception ignored) {}
    }

    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }
} 