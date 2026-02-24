# Resume Builder - Phase 2: Authentication + User Profile

## Overview
This is **Phase 2** of the Resume Builder project, adding **user profile management** to the authentication system.

## Features
- ✓ User Registration
- ✓ User Login
- ✓ **User Profile Management (NEW)**
- ✓ **Profile Update Functionality (NEW)**
- ✓ SQLite Database Integration
- ✓ CLI Interface

## Project Structure
```
JavaProj-phase2/
├── src/
│   └── com/resumebuilder/
│       ├── backend/
│       │   ├── dao/
│       │   │   └── UserDAO.java (updated)
│       │   ├── model/
│       │   │   └── User.java (updated with profile fields)
│       │   └── util/
│       │       └── DatabaseConnection.java (updated)
│       └── cli/
│           ├── AuthScreen.java
│           ├── ProfileScreen.java (NEW)
│           └── CLIApp.java (updated)
├── db/
├── lib/
└── run_app.sh
```

## New in Phase 2
- **Profile Fields**: Full name, phone number, and address
- **Profile Screen**: View and update user profile information
- **Main Menu**: Navigate between profile and logout

## How to Run
```bash
./download_dependencies.sh  # First time only
./run_app.sh
```

## What's Next?
- **Phase 3**: Add basic resume data models (Education, Experience, Skills)
- **Phase 4**: Add full resume CRUD operations
- **Phase 5**: Add template system
- **Phase 6**: Add PDF generation
- **Phase 7**: Complete application with all features
