package com.resumebuilder.cli;

import com.resumebuilder.backend.dao.UserDAO;
import com.resumebuilder.backend.model.User;

import java.util.Scanner;

public class AuthScreen {
    private Scanner scanner;
    private UserDAO userDAO;

    public AuthScreen() {
        this.scanner = new Scanner(System.in);
        this.userDAO = new UserDAO();
    }

    public User show() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("  RESUME BUILDER - PHASE 1: AUTHENTICATION");
            System.out.println("=".repeat(50));
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("\nChoose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    User user = handleLogin();
                    if (user != null) {
                        return user;
                    }
                    break;
                case "2":
                    handleRegister();
                    break;
                case "3":
                    System.out.println("\nGoodbye!");
                    System.exit(0);
                default:
                    System.out.println("\nInvalid option. Please try again.");
            }
        }
    }

    private User handleLogin() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDAO.loginUser(username, password);
        if (user != null) {
            System.out.println("\n✓ Login successful! Welcome, " + user.getUsername());
            return user;
        } else {
            System.out.println("\n✗ Invalid credentials. Please try again.");
            return null;
        }
    }

    private void handleRegister() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        User user = new User(username, password, email);
        if (userDAO.registerUser(user)) {
            System.out.println("\n✓ Registration successful! You can now login.");
        } else {
            System.out.println("\n✗ Registration failed. Username or email may already exist.");
        }
    }
}
