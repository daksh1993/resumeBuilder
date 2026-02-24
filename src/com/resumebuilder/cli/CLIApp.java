package com.resumebuilder.cli;

import com.resumebuilder.backend.model.User;
import com.resumebuilder.backend.util.DatabaseConnection;

public class CLIApp {
    private User currentUser;

    public void start() {
        // Initialize database
        DatabaseConnection.initializeDatabase();

        // Show authentication screen
        AuthScreen authScreen = new AuthScreen();
        currentUser = authScreen.show();

        // After successful login
        System.out.println("  Successfully logged in as: " + currentUser.getUsername());
        System.out.println("  Email: " + currentUser.getEmail());
        System.out.println("=".repeat(50));
        System.out.println("\nPhase 1 Complete - Basic Authentication Working!");
        System.out.println("Next phases will add more features...\n");
    }

    public static void main(String[] args) {
        CLIApp app = new CLIApp();
        app.start();
    }
}
