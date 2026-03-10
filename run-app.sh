#!/bin/bash

# TodoApp Application Startup Script
# This script sets the correct Java version and runs the Spring Boot application

echo "Starting TodoApp Application..."

# Set JAVA_HOME to Java 21
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Verify Java version
echo "Using Java version:"
java -version

# Check if we're in the correct directory
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found. Please run this script from the spring-boot-backend directory."
    exit 1
fi

# Set default environment variables for local development
export DATABASE_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5432/todoapp}"
export DATABASE_USER="${DATABASE_USER:-postgres}"
export DATABASE_PASSWORD="${DATABASE_PASSWORD:-postgres}"
export JWT_SECRET="${JWT_SECRET:-your-256-bit-secret-key-change-me-in-production-please-this-needs-to-be-at-least-256-bits-long}"
export SMTP_HOST="${SMTP_HOST:-smtp.gmail.com}"
export SMTP_PORT="${SMTP_PORT:-587}"
export SMTP_USERNAME="${SMTP_USERNAME:-your-email@gmail.com}"
export SMTP_PASSWORD="${SMTP_PASSWORD:-your-app-password}"
export FRONTEND_URL="${FRONTEND_URL:-http://localhost:3000}"
export CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:3000}"

echo "Environment variables set for local development"
echo "Database URL: $DATABASE_URL"
echo "Frontend URL: $FRONTEND_URL"

# Try to run with Spring Boot Maven plugin first
echo "Attempting to start application with Maven..."

if command -v mvn &> /dev/null; then
    echo "Running with Maven Spring Boot plugin..."
    mvn spring-boot:run
elif [ -f "./mvnw" ]; then
    echo "Running with Maven wrapper..."
    ./mvnw spring-boot:run
else
    echo "Maven not found. Please install Maven or add mvnw to the project."
    echo "You can install Maven using Homebrew: brew install maven"
    exit 1
fi
