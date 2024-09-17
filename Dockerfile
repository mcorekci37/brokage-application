# Use a base image with Java
FROM openjdk:21-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/brokage-application-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your microservice will run on
EXPOSE 8222

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
