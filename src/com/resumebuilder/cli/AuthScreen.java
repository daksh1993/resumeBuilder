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

            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("\nChoose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    User user = methLogin();
                    if (user != null) {
                        return user;
                    }
                    break;
                case "2":
                    methRegister();
                    break;
                case "3":
                    System.out.println("exiting");
                    System.exit(0);
                default:
                    System.out.println("invalid try again");
            }
        }
    }

    private User methLogin() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDAO.loginUser(username, password);
        if (user != null) {
            System.out.println("\n✓ Login success " + user.getUsername());
            return user;
        } else {
            System.out.println("\n Invalid id,pass");
            return null;
        }
    }

    private void methRegister() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        User user = new User(username, password, email);
        if (userDAO.registerUser(user)) {
            System.out.println("Regi successfulgoto login");
        } else {
            System.out.println("Regi failed, username, email already exists");
        }
    }
}
