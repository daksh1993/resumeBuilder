package com.resumebuilder.cli;

import com.resumebuilder.backend.dao.ResumeDAO;
import com.resumebuilder.backend.exception.DatabaseException;
import com.resumebuilder.backend.exception.ResumeNotFoundException;
import com.resumebuilder.backend.model.Resume;
import com.resumebuilder.backend.model.User;
import com.resumebuilder.pdf.PdfGenerator;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Scanner;

public class DashboardScreen {
    private final User currentUser;
    private final ResumeDAO resumeDAO;
    private final Scanner scanner;
    
    public DashboardScreen(User user) {
        this.currentUser = user;
        this.resumeDAO = new ResumeDAO();
        this.scanner = new Scanner(System.in);
    }
    
    public boolean show() {
        while (true) {
            System.out.println("\nDASHBORD " + currentUser.getUsername() + " Resums\n");
            
            List<Resume> resumes = loadResumes();
            
            System.out.println("Your Resums");
            if (resumes.isEmpty()) {
                System.out.println("  No resums found Creat one");
            } else {
                for (int i = 0; i < resumes.size(); i++) {
                    Resume r = resumes.get(i);
                    System.out.println("  " + (i + 1) + " " + r.getTitle() + 
                                     " " + r.getTemplateName() + " " + r.getColorTheme());
                }
            }
            
            System.out.println("\nOtions");
            System.out.println("  [C] Creat New Resum");
            System.out.println("  [E] Edit Resum");
            System.out.println("  [D] Delet Resum");
            System.out.println("  [P] Export to PDF");
            System.out.println("  [L] Logout\n");
            
            System.out.print("Choose an option ");
            String choice = scanner.nextLine().toLowerCase().trim();
            
            switch (choice) {
                case "c":
                    createNewResume();
                    break;
                case "e":
                    editResume(resumes);
                    break;
                case "d":
                    deleteResume(resumes);
                    break;
                case "p":
                    exportToPdf(resumes);
                    break;
                case "l":
                    return true;
                default:
                    System.out.println("Invlid option Pls try again");
            }
        }
    }
    
    private void createNewResume() {
        Resume newResume = new Resume();
        newResume.setUserId(currentUser.getId());
        
        ResumeEditorScreen editor = new ResumeEditorScreen(newResume, resumeDAO);
        editor.show();
    }
    
    private void editResume(List<Resume> resumes) {
        if (resumes.isEmpty()) {
            System.out.println("No resums to edit");
            return;
        }
        
        System.out.println("\nEDIT RESUM\n");
        System.out.println("Your Resumes");
        for (int i = 0; i < resumes.size(); i++) {
            Resume r = resumes.get(i);
            System.out.println("  " + (i + 1) + " " + r.getTitle() + 
                             " " + r.getTemplateName() + " " + r.getColorTheme());
        }
        
        System.out.print("\nEnter resum numbr to edit 0 to cancl ");
        int index = readInt(0, resumes.size());
        
        if (index == 0) {
            return;
        }
        
        Resume selected = resumes.get(index - 1);
        
        try {
            Resume fullResume = resumeDAO.getResumeByIdOrThrow(selected.getId());
            ResumeEditorScreen editor = new ResumeEditorScreen(fullResume, resumeDAO);
            editor.show();
        } catch (ResumeNotFoundException e) {
            System.out.println("ERROR Resume not found");
        } catch (DatabaseException e) {
            System.out.println("ERROR Failed to load resume");
        }
    }
    
    private void deleteResume(List<Resume> resumes) {
        if (resumes.isEmpty()) {
            System.out.println("No resums to delet");
            return;
        }
        
        System.out.println("\nDELET RESUM\n");
        System.out.println("Your Resumes");
        for (int i = 0; i < resumes.size(); i++) {
            Resume r = resumes.get(i);
            System.out.println("  " + (i + 1) + " " + r.getTitle() + 
                             " " + r.getTemplateName() + " " + r.getColorTheme());
        }
        
        System.out.print("\nEnter resum numbr to delet 0 to cancl ");
        int index = readInt(0, resumes.size());
        
        if (index == 0) {
            return;
        }
        
        Resume selected = resumes.get(index - 1);
        
        System.out.print("Are u sure u want to delet " + selected.getTitle() + " y/n ");
        String confirm = scanner.nextLine().toLowerCase().trim();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            try {
                boolean success = resumeDAO.deleteResumeOrThrow(selected.getId());
                if (success) {
                    System.out.println("Resum deletd succesfully");
                } else {
                    System.out.println("ERROR Faild to delet resum");
                }
            } catch (ResumeNotFoundException e) {
                System.out.println("ERROR Resume not found");
            } catch (DatabaseException e) {
                System.out.println("ERROR Faild to delet resum");
            }
        } else {
            System.out.println("Deletin canceld");
        }
    }
    
    private void exportToPdf(List<Resume> resumes) {
        if (resumes.isEmpty()) {
            System.out.println("No resums to exprt");
            return;
        }
        
        System.out.println("\nEXPORT TO PDF\n");
        System.out.println("Your Resumes");
        for (int i = 0; i < resumes.size(); i++) {
            Resume r = resumes.get(i);
            System.out.println("  " + (i + 1) + " " + r.getTitle() + 
                             " " + r.getTemplateName() + " " + r.getColorTheme());
        }
        
        System.out.print("\nEnter resum numbr to exprt 0 to cancl ");
        int index = readInt(0, resumes.size());
        
        if (index == 0) {
            return;
        }
        
        Resume selected = resumes.get(index - 1);
        try {
            Resume fullResume = resumeDAO.getResumeByIdOrThrow(selected.getId());
            String filename = fullResume.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
            String filepath = filename;
            FileOutputStream out = new FileOutputStream(filepath);
            PdfGenerator.generate(fullResume, out);
            out.close();
            System.out.println("PDF generatd succesfully " + filepath);
        } catch (ResumeNotFoundException e) {
            System.out.println("ERROR Resume not found");
        } catch (DatabaseException e) {
            System.out.println("ERROR Failed to load resume");
        } catch (Exception e) {
            System.out.println("ERROR Faild to generat PDF " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Resume> loadResumes() {
        try {
            return resumeDAO.getResumesByUserIdOrThrow(currentUser.getId());
        } catch (DatabaseException e) {
            System.out.println("ERROR Failed to load resumes");
            return java.util.Collections.emptyList();
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
