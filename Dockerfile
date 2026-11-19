# Build multi-etapes : Angular (ecrit directement dans backend/src/main/resources/static,
# voir frontend/angular.json) puis Spring Boot (taskflow.jar).
# Utilise pour le deploiement sur Render.com (profil "render", base PostgreSQL) -- voir build.sh
# pour l'equivalent local.

FROM node:22-alpine AS frontend-build
WORKDIR /app
COPY frontend/ frontend/
COPY backend/src/main/resources backend/src/main/resources
RUN cd frontend && npm ci && npx ng build

FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY backend/pom.xml backend/pom.xml
RUN cd backend && mvn -B -q dependency:go-offline
COPY backend/src backend/src
COPY --from=frontend-build /app/backend/src/main/resources/static backend/src/main/resources/static
RUN cd backend && mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/taskflow.jar ./taskflow.jar
ENV SPRING_PROFILES_ACTIVE=render
# Offre gratuite Render = 512 Mo : la JVM se limite a 75 % de la memoire du conteneur
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "taskflow.jar"]
