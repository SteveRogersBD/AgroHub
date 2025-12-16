# Multi-stage build for API Gateway
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent POM and service POM
COPY pom.xml ../pom.xml
COPY api-gateway/pom.xml ./pom.xml

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY api-gateway/src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the gateway port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
