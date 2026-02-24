# Resume Builder - Phase 1: Basic Authentication

## Overview
This is **Phase 1** of the Resume Builder project, focusing on **basic user authentication**.

## Features
- ✓ User Registration
- ✓ User Login  
- ✓ SQLite Database Integration
- ✓ CLI Interface

## Project Structure
```
JavaProj-phase1/
├── src/
│   └── com/resumebuilder/
│       ├── backend/
│       │   ├── dao/
│       │   │   └── UserDAO.java
│       │   ├── model/
│       │   │   └── User.java
│       │   └── util/
│       │       └── DatabaseConnection.java
│       └── cli/
│           ├── AuthScreen.java
│           └── CLIApp.java
├── db/
│   └── (database created at runtime)
├── lib/
│   └── sqlite-jdbc-3.47.1.0.jar
└── run_app.sh
```

## How to Run
```bash
chmod +x run_app.sh
./run_app.sh
```

## What's Next?
- **Phase 2**: Add user profile management
- **Phase 3**: Add basic resume data models
- **Phase 4**: Add full resume CRUD operations
- **Phase 5**: Add template system
- **Phase 6**: Add PDF generation
- **Phase 7**: Complete application with all features
