FROM maven:3.9.4-eclipse-temurin-21 as build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=target/ems-0.0.1-SNAPSHOT.jar
COPY --from=build /workspace/${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java","-Xms256m","-Xmx512m","-jar","/app/app.jar"]
