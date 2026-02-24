package com.resumebuilder.backend.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:db/resumebuilder.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
           
            System.out.println("DB initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error in initializing db: " + e.getMessage());
        }
    }
}
