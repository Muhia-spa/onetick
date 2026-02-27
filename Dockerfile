FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN addgroup --system onetick && adduser --system --ingroup onetick onetick
COPY --from=build /workspace/target/onetick-0.1.0.jar /app/onetick.jar
USER onetick
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/onetick.jar"]
