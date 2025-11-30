# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY .mvn/ .mvn

COPY mvnw pom.xml ./

RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

RUN ./mvnw dependency:resolve

COPY src ./src

ENV SPRING_PROFILES_ACTIVE=test

CMD ["./mvnw", "spring-boot:run"]