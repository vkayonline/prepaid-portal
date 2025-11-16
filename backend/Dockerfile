# 1. Build Stage
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY .mvn/ mvnw pom.xml ./

# Download dependencies
RUN ./mvnw -B dependency:resolve dependency:resolve-plugins

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw -B clean package -DskipTests

# 2. Runtime Stage
FROM eclipse-temurin:21-jre-ubi9-minimal

WORKDIR /app

# Copy fat JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Run Spring Boot App
ENTRYPOINT ["java","-jar","app.jar"]
