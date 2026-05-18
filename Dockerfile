# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/

# FIX 1: Ensure gradlew is executable to prevent "Permission Denied" errors
RUN chmod +x ./gradlew 

COPY build.gradle settings.gradle ./

# FIX 2: Restrict Gradle's memory footprint during dependency resolution
RUN ./gradlew dependencies --no-daemon -Dorg.gradle.jvmargs="-Xmx256m" 2>/dev/null || true

COPY src/ src/

# FIX 3: Restrict Gradle's memory footprint during the application build
RUN ./gradlew bootJar --no-daemon -x test -Dorg.gradle.jvmargs="-Xmx256m"

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["./docker-entrypoint.sh"]