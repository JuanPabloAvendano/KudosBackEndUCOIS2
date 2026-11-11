# ---------- Stage 1: build ----------
FROM eclipse-temurin:27-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src ./src
RUN ./mvnw -B -DskipTests package

# ---------- Stage 2: run ----------
FROM eclipse-temurin:27-jre
LABEL org.opencontainers.image.authors="Juan Pablo Avendaño"
RUN apt-get update \
 && apt-get -o APT::Get::Always-Include-Phased-Updates=true upgrade -y --no-install-recommends \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system kudos && useradd --system --gid kudos kudos
WORKDIR /app
COPY --from=build /app/target/KudosBackEndUCOIS2-0.0.1-SNAPSHOT.jar app.jar
USER kudos
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]

