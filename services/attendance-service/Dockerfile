FROM gradle:8.14-jdk21 AS build

WORKDIR /workspace

COPY --chown=gradle:gradle build.gradle settings.gradle gradle.properties ./
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle services ./services

RUN gradle :services:attendance-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/services/attendance-service/build/libs/attendance-service-0.1.0-SNAPSHOT.jar app.jar
RUN chown spring:spring app.jar

USER spring

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8083/api/actuator/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
