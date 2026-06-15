FROM maven:3.9.9-eclipse-temurin-21 AS build

ARG SERVICE
WORKDIR /workspace
COPY ${SERVICE}/pom.xml ./pom.xml
COPY ${SERVICE}/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8050 8070 8081 8082 8083 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
