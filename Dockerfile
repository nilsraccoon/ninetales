# Stage 1: Build with Maven + Temurin JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Leverage Docker cache by copying pom.xml separately
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Now copy the source code
COPY src ./src

# Build the JAR (skip tests for speed)
RUN mvn package -DskipTests -B

# Stage 2: Runtime with Temurin JRE 21 (slim)
FROM eclipse-temurin:21-jre

WORKDIR /app

LABEL internal-port="8080"

LABEL arachne.name="Ninetales Discord Bot"
LABEL arachne.version="1.0.7"

LABEL ninetales.update-note="More informative archive names"

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar ./app.jar

EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]