FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests -pl services/discovery-server -am clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/discovery-server/target/*.jar /app/app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/app.jar"]