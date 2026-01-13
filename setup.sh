#!/bin/bash

# Setup script for Legacy Order Entry Application
# This script downloads and installs everything required to build and run the Java app

set -e

echo "=========================================="
echo "Legacy Order Entry Application Setup"
echo "=========================================="
echo ""

# Detect OS
OS="$(uname -s)"
case "${OS}" in
    Linux*)     MACHINE=Linux;;
    Darwin*)    MACHINE=Mac;;
    CYGWIN*)    MACHINE=Cygwin;;
    MINGW*)     MACHINE=MinGw;;
    *)          MACHINE="UNKNOWN:${OS}"
esac

echo "Detected operating system: ${MACHINE}"
echo ""

# Check if Java is already installed
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    echo "Java is already installed: version ${JAVA_VERSION}"
    
    # Check if version is 11 or higher
    MAJOR_VERSION=$(echo ${JAVA_VERSION} | cut -d'.' -f1)
    if [ "${MAJOR_VERSION}" -ge 11 ]; then
        echo "Java version is compatible (11+). Setup complete!"
        echo ""
        echo "You can now run:"
        echo "  ./mvnw -q exec:java              # Run the desktop application"
        echo "  ./mvnw compile                   # Compile the code"
        exit 0
    else
        echo "Warning: Java version ${JAVA_VERSION} is installed but version 11+ is recommended"
        echo "The app will still work (Java 8 compatible) but we recommend upgrading."
    fi
else
    echo "Java is not installed. Installing JDK..."
fi

echo ""

# Install Java based on OS
case "${MACHINE}" in
    Linux)
        echo "Installing OpenJDK 11 on Linux..."
        
        # Check if apt is available (Ubuntu/Debian)
        if command -v apt-get &> /dev/null; then
            echo "Using apt-get..."
            sudo apt-get update
            sudo apt-get install -y openjdk-11-jdk
        
        # Check if yum is available (CentOS/RHEL)
        elif command -v yum &> /dev/null; then
            echo "Using yum..."
            sudo yum install -y java-11-openjdk-devel
        
        # Check if dnf is available (Fedora)
        elif command -v dnf &> /dev/null; then
            echo "Using dnf..."
            sudo dnf install -y java-11-openjdk-devel
        
        else
            echo "Error: Could not detect package manager (apt-get, yum, or dnf)"
            echo "Please install OpenJDK 11 manually:"
            echo "  Ubuntu/Debian: sudo apt-get install openjdk-11-jdk"
            echo "  CentOS/RHEL:   sudo yum install java-11-openjdk-devel"
            echo "  Fedora:        sudo dnf install java-11-openjdk-devel"
            exit 1
        fi
        ;;
    
    Mac)
        echo "Installing OpenJDK 11 on macOS..."
        
        # Check if Homebrew is installed
        if command -v brew &> /dev/null; then
            echo "Using Homebrew..."
            brew install openjdk@11
            
            # Create symlink as recommended by Homebrew
            sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
            
            echo ""
            echo "Note: You may need to add the following to your shell profile:"
            echo "export PATH=\"/usr/local/opt/openjdk@11/bin:\$PATH\""
        else
            echo "Error: Homebrew is not installed."
            echo "Please install Homebrew first: https://brew.sh"
            echo "Or install Java manually from: https://adoptium.net/"
            exit 1
        fi
        ;;
    
    Cygwin|MinGw)
        echo "For Windows, please download and install JDK manually:"
        echo ""
        echo "1. Download OpenJDK 11 from: https://adoptium.net/"
        echo "2. Run the installer"
        echo "3. Add JAVA_HOME to your environment variables"
        echo "4. Add %JAVA_HOME%\\bin to your PATH"
        echo ""
        echo "After installation, run this script again to verify."
        exit 1
        ;;
    
    *)
        echo "Error: Unsupported operating system: ${MACHINE}"
        echo "Please install OpenJDK 11 manually from: https://adoptium.net/"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "Installation complete!"
echo "=========================================="
echo ""

# Verify installation
if command -v java &> /dev/null; then
    java -version
    echo ""
    echo "Setup successful! You can now:"
    echo "  ./mvnw -q exec:java              # Run the desktop application"
    echo "  ./mvnw compile                   # Compile the code"
else
    echo "Warning: Java installation may not be complete. Please restart your terminal and verify with: java -version"
fi

echo ""
