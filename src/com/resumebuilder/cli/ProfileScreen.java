package com.resumebuilder.cli;

import com.resumebuilder.backend.dao.UserDAO;
import com.resumebuilder.backend.model.User;

import java.util.Scanner;

public class ProfileScreen {
    private Scanner scanner;
    private UserDAO userDAO;
    private User user;

    public ProfileScreen(User user) {
        this.scanner = new Scanner(System.in);
        this.userDAO = new UserDAO();
        this.user = user;
    }

    public void show() {
        while (true) {
            System.out.println("  USER PROFILE");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Full Name: " + (user.getFullName() != null ? user.getFullName() : "[Not set]"));
            System.out.println("Phone: " + (user.getPhone() != null ? user.getPhone() : "[Not set]"));
            System.out.println("Address: " + (user.getAddress() != null ? user.getAddress() : "[Not set]"));
            System.out.println("\n1. Update Profile");
            System.out.println("2. Back to Main Menu");
            System.out.print("\nChoose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    updateProfile();
                    break;
                case "2":
                    return;
                default:
                    System.out.println("\nInvalid option. Please try again.");
            }
        }
    }

    private void updateProfile() {
        System.out.println("\n--- UPDATE PROFILE ---");
        
        System.out.print("Full Name [" + (user.getFullName() != null ? user.getFullName() : "") + "]: ");
        String fullName = scanner.nextLine();
        if (!fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }

        System.out.print("Phone [" + (user.getPhone() != null ? user.getPhone() : "") + "]: ");
        String phone = scanner.nextLine();
        if (!phone.trim().isEmpty()) {
            user.setPhone(phone);
        }

        System.out.print("Address [" + (user.getAddress() != null ? user.getAddress() : "") + "]: ");
        String address = scanner.nextLine();
        if (!address.trim().isEmpty()) {
            user.setAddress(address);
        }

        if (userDAO.updateUserProfile(user)) {
            System.out.println("\n✓ Profile updated successfully!");
        } else {
            System.out.println("\n✗ Failed to update profile.");
        }
    }
}
