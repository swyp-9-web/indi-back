# Step1: Build Stage
FROM --platform=linux/amd64 bellsoft/liberica-openjdk-alpine:17 AS build

LABEL maintainer="swypweb9team7@gmail.com"

WORKDIR /workspace

COPY gradle gradle
COPY src src
COPY .env .
COPY build.gradle .
COPY gradlew .
COPY settings.gradle .

RUN ./gradlew clean bootJar

# Step2: Run Stage
FROM --platform=linux/amd64 openjdk:17-slim

EXPOSE 8000

COPY --from=build /workspace/build/libs/*.jar artego-springboot.jar

ENTRYPOINT ["java", "-jar","/artego-springboot.jar"]