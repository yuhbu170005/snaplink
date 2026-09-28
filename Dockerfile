# ==========================================
# Multi-Stage Build Dockerfile for SnapLink
# Java 21 / Spring Boot 4.x
# ==========================================

# Stage 1: Build JAR package
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create a non-root dedicated application user
RUN addgroup -S snapgroup && adduser -S snapuser -G snapgroup

# Copy compiled executable JAR from builder stage
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R snapuser:snapgroup /app

USER snapuser:snapgroup

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
