# Build stage
FROM gradle:jdk17 as build
WORKDIR /workspace
COPY --chown=gradle:gradle . /workspace
RUN gradle :bootstrap-server:build --no-daemon

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /workspace/bootstrap-server/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]