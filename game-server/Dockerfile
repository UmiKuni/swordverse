FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package \
    && cp "$(find target -maxdepth 1 -name '*.jar' ! -name '*.original' | head -n 1)" application.jar


FROM eclipse-temurin:17-jre

WORKDIR /application

RUN groupadd --system swordverse \
    && useradd --system --gid swordverse swordverse

COPY --from=builder /workspace/application.jar application.jar

USER swordverse

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]
