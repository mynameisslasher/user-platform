# ---------- build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests -pl services/userdb-api -am clean package

# ---------- run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/userdb-api/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]