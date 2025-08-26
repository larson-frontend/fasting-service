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
# Falls der Jar-Name variiert, * verwendet lassen:
COPY --from=build /app/target/*.jar app.jar

# Render nutzt $PORT (Default 10000)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 10000

# Profil kommt per ENV (SPRING_PROFILES_ACTIVE)
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
