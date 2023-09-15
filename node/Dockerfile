# Build stage
FROM gradle:jdk17 as build
WORKDIR /workspace
COPY --chown=gradle:gradle . /workspace
RUN gradle :node:build --no-daemon

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /workspace/node/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]