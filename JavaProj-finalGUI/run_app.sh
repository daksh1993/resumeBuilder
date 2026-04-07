#!/bin/bash
set -e

echo "Compiling..."
mkdir -p bin
find bin src -type f -name "*.class" -delete

javac \
  --module-path lib \
  --add-modules javafx.controls \
  -cp "lib/*" \
  -d bin \
  $(find src -name "*.java")

echo "Running Application..."
java \
  --enable-native-access=javafx.graphics \
  --module-path lib \
  --add-modules javafx.controls \
  -cp "bin:lib/*" \
  com.resumebuilder.gui.ResumeBuilderApp
