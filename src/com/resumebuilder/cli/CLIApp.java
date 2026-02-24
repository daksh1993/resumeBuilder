package com.resumebuilder.cli;

import com.resumebuilder.backend.model.User;
import com.resumebuilder.backend.util.DatabaseConnection;

import java.util.Scanner;

public class CLIApp {
    private User currentUser;
    private Scanner scanner;

    public CLIApp() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        // Initialize database
        DatabaseConnection.initializeDatabase();

        // Show authentication screen
        AuthScreen authScreen = new AuthScreen();
        currentUser = authScreen.show();

        // Show main menu
        showMainMenu();
    }

    private void showMainMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("  RESUME BUILDER - PHASE 2: USER PROFILE");
            System.out.println("  Logged in as: " + currentUser.getUsername());
            System.out.println("=".repeat(50));
            System.out.println("1. View/Update Profile");
            System.out.println("2. Logout");
            System.out.print("\nChoose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    ProfileScreen profileScreen = new ProfileScreen(currentUser);
                    profileScreen.show();
                    break;
                case "2":
                    System.out.println("\nLogging out... Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("\nInvalid option. Please try again.");
            }
        }
    }

    public static void main(String[] args) {
        CLIApp app = new CLIApp();
        app.start();
    }
}

