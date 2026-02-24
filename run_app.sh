#!/bin/bash

# Compile Java files
echo "Compiling Java files..."
javac -d bin -cp "lib/*" src/com/resumebuilder/backend/model/*.java
javac -d bin -cp "lib/*:bin" src/com/resumebuilder/backend/util/*.java
javac -d bin -cp "lib/*:bin" src/com/resumebuilder/backend/dao/*.java
javac -d bin -cp "lib/*:bin" src/com/resumebuilder/cli/*.java

# Run the application
echo "Starting Resume Builder - Phase 2..."
java -cp "bin:lib/*" com.resumebuilder.cli.CLIApp
