# ---------- build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests -pl services/notification-service -am clean package

# ---------- run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/notification-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]