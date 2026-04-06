#!/bin/bash

JAVAFX_MODULES="javafx.controls"
CLASSPATH="lib/*"

echo "Compiling..."
mkdir -p bin
find bin -type f -name "*.class" -delete
find src -type f -name "*.class" -delete
javac --module-path lib --add-modules "$JAVAFX_MODULES" -cp "$CLASSPATH" -d bin $(find src -name "*.java")

if [ $? -ne 0 ]; then
    echo "Compilation Failed!"
    exit 1
fi

echo "Running Application..."
java --enable-native-access=javafx.graphics --module-path lib --add-modules "$JAVAFX_MODULES" -cp "bin:lib/*" com.resumebuilder.gui.ResumeBuilderApp
