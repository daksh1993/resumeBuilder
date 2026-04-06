#!/bin/bash

# Create lib directory if it doesn't exist
mkdir -p lib

echo "Downloading Dependencies..."

# MySQL Connector
echo "Downloading MySQL Connector..."
curl -L -o lib/mysql-connector-j-8.3.0.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar

# JavaFX 21 (compatible with Java 21)
echo "Downloading JavaFX 21..."
curl -L -o lib/javafx-base-21.jar https://repo1.maven.org/maven2/org/openjfx/javafx-base/21/javafx-base-21.jar
curl -L -o lib/javafx-controls-21.jar https://repo1.maven.org/maven2/org/openjfx/javafx-controls/21/javafx-controls-21.jar
curl -L -o lib/javafx-graphics-21.jar https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/21/javafx-graphics-21.jar
curl -L -o lib/javafx-fxml-21.jar https://repo1.maven.org/maven2/org/openjfx/javafx-fxml/21/javafx-fxml-21.jar
curl -L -o lib/javafx-swing-21.jar https://repo1.maven.org/maven2/org/openjfx/javafx-swing/21/javafx-swing-21.jar

# Platform-specific JavaFX native libraries (macOS AArch64 - Apple Silicon)
echo "Downloading JavaFX native libraries for macOS Apple Silicon..."
curl -L -o lib/javafx-base-21-mac-aarch64.jar https://repo1.maven.org/maven2/org/openjfx/javafx-base/21/javafx-base-21-mac-aarch64.jar
curl -L -o lib/javafx-controls-21-mac-aarch64.jar https://repo1.maven.org/maven2/org/openjfx/javafx-controls/21/javafx-controls-21-mac-aarch64.jar
curl -L -o lib/javafx-graphics-21-mac-aarch64.jar https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/21/javafx-graphics-21-mac-aarch64.jar
curl -L -o lib/javafx-fxml-21-mac-aarch64.jar https://repo1.maven.org/maven2/org/openjfx/javafx-fxml/21/javafx-fxml-21-mac-aarch64.jar
curl -L -o lib/javafx-swing-21-mac-aarch64.jar https://repo1.maven.org/maven2/org/openjfx/javafx-swing/21/javafx-swing-21-mac-aarch64.jar

# OpenPDF
echo "Downloading OpenPDF..."
curl -L -o lib/openpdf-1.3.30.jar https://repo1.maven.org/maven2/com/github/librepdf/openpdf/1.3.30/openpdf-1.3.30.jar

# PDFBox (For Preview Rendering)
echo "Downloading PDFBox..."
curl -L -o lib/pdfbox-2.0.30.jar https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.30/pdfbox-2.0.30.jar
curl -L -o lib/fontbox-2.0.30.jar https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/2.0.30/fontbox-2.0.30.jar
curl -L -o lib/commons-logging-1.2.jar https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar

echo "Download Complete!"
ls -l lib/
