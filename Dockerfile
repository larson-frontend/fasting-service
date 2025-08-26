# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ---- run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Create non-root user
RUN useradd -u 10001 -ms /bin/bash appuser
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+AlwaysActAsServerClassMachine"
EXPOSE 10000
USER appuser
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
