# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy Maven POM and source files
COPY pom.xml .
COPY src ./src

# Build executable Spring Boot JAR
RUN mvn clean package -DskipTests

# Stage 2: Production runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Set up non-root user for secure container execution
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

# Copy executable JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar /app/app.jar

USER appuser

# Render dynamically assigns PORT at runtime (defaults to 8080 locally)
ENV PORT=8080
EXPOSE 8080

# Launch Spring Boot application using assigned port
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -Dserver.port=${PORT} -jar /app/app.jar"]
