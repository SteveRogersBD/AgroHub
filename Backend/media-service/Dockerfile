# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent pom and service pom
COPY pom.xml ../pom.xml
COPY media-service/pom.xml ./pom.xml

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY media-service/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8087

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
