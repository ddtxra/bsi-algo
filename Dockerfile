# Use a base image with Java installed
FROM openjdk:17

# Set the working directory inside the container
WORKDIR /app

COPY target/bsi-algorithm-1.0-SNAPSHOT.jar app.jar

# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "app.jar", "/data"]