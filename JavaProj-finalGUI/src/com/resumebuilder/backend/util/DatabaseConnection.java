package com.resumebuilder.backend.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/resume_builder_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Default XAMPP password is empty

    public static Connection getConnection() throws SQLException {
        try {
            // Load Driver explicitly for older environments, though often auto-loaded in newer JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found! Add the connector jar to classpath.");
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    // Test connection
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connected to the database successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }
}
