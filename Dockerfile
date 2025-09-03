FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -u 10001 spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]