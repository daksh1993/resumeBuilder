package com.resumebuilder.cli;

import com.resumebuilder.backend.model.User;

public class CLIApp {
    
    public static void main(String[] args) {
        System.out.println("\nRESUME BUILDR CLI APPLICATON\n");
        
        try {
            runApplication();
        } catch (Exception e) {
            System.err.println("\nFatal eror occured");
            e.printStackTrace();
        }
        
        System.out.println("\nThank u for using Resume Buildr");
    }
    
    private static void runApplication() {
        AuthScreen authScreen = new AuthScreen();
        
        while (true) {
            User currentUser = authScreen.show();
            
            if (currentUser == null) {
                break;
            }
            
            DashboardScreen dashboard = new DashboardScreen(currentUser);
            boolean logout = dashboard.show();
            
            if (logout) {
                System.out.println("Loged out succesfully");
            }
        }
    }
}
