package com.resumebuilder.cli;

import com.resumebuilder.backend.dao.UserDAO;
import com.resumebuilder.backend.model.User;
import java.util.Scanner;

public class AuthScreen {
    private final UserDAO userDAO;
    private final Scanner scanner;
    
    public AuthScreen() {
        this.userDAO = new UserDAO();
        this.scanner = new Scanner(System.in);
    }
    
    public User show() {
        while (true) {
            System.out.println("\nRESUME BUILDR LOGIN\n");
            System.out.println("1 Login");
            System.out.println("2 Regster");
            System.out.println("3 Exit\n");
            
            System.out.print("Choose an option ");
            int choice = readInt(1, 3);
            
            switch (choice) {
                case 1:
                    User loggedInUser = handleLogin();
                    if (loggedInUser != null) {
                        return loggedInUser;
                    }
                    break;
                case 2:
                    handleRegister();
                    break;
                case 3:
                    return null;
            }
        }
    }
    
    private User handleLogin() {
        System.out.println("\nLOGIN\n");
        
        System.out.print("Username ");
        String username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty");
            return null;
        }
        
        System.out.print("Password ");
        String password = scanner.nextLine();
        
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty");
            return null;
        }
        
        User user = userDAO.login(username, password);
        
        if (user != null) {
            System.out.println("Login succesfull Welcom " + username);
            return user;
        } else {
            System.out.println("ERROR Invlid credentials Pls try again");
            return null;
        }
    }
    
    private void handleRegister() {
        System.out.println("\nREGISTER\n");
        
        System.out.print("Username ");
        String username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty");
            return;
        }
        
        System.out.print("Password ");
        String password = scanner.nextLine();
        
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty");
            return;
        }
        
        System.out.print("Confirm Password ");
        String confirmPassword = scanner.nextLine();
        
        if (!password.equals(confirmPassword)) {
            System.out.println("ERROR Paswords dont match");
            return;
        }
        
        User newUser = new User(username, password);
        boolean success = userDAO.register(newUser);
        
        if (success) {
            System.out.println("Registraton succesfull You can now login");
        } else {
            System.out.println("ERROR Registraton faild Usrname may alredy exist");
        }
    }
    
    private int readInt(int min, int max) {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.print("Please enter a number between " + min + " and " + max + " ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input Please enter a valid number ");
            }
        }
    }
}
