# Built from the services/ folder (see docker-compose.yml) so the build can
# see the parent pom.xml and any shared modules this service depends on.

# Stage 1: build the jar with the Maven wrapper (needs a full JDK)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# -pl = build this module; -am = also build the modules it depends on.
# The cache mount keeps Maven's downloads between builds, so rebuilds stay fast.
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -q -pl user-service -am package -DskipTests

# Stage 2: run the jar on a smaller Java runtime (no build tools)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
