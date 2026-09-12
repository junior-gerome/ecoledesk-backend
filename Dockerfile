FROM gradle:8.14-jdk21 AS build
#Pour faire fonctionner mon application, donne-moi un environnement Java 21

WORKDIR /workspace
#À l'intérieur du conteneur, mon application travaillera dans /app

COPY --chown=gradle:gradle build.gradle settings.gradle gradle.properties ./
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle src ./src
#COPY --chown=gradle:gradle services ./services

RUN gradle bootJar --no-daemon -x test


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/build/libs/school-management-0.0.1-SNAPSHOT.jar app.jar
RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/actuator/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
