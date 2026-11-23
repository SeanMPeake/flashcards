# Built from the repository root (see docker-compose.yml), the same way any host
# builds it, so the build can see the parent pom.xml and the shared common module.
# No BuildKit cache mount: some hosts (e.g. Railway) require their own cache-mount
# format, so the plain form keeps this Dockerfile portable at the cost of slower builds.

# Stage 1: build the jar with the Maven wrapper (needs a full JDK)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY services/ .
# -pl = build this module; -am = also build the modules it depends on (common)
RUN chmod +x mvnw && ./mvnw -q -pl user-service -am package -DskipTests

# Stage 2: run the jar on a smaller Java runtime (no build tools)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
