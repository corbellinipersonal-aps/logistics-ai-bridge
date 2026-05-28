# Production runtime image — JRE only, not the full JDK
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the Maven build output (fat jar) into the container
COPY target/apibridge-0.0.1-SNAPSHOT.jar app.jar

# Copy demo assets so /api/demo/populate can load relative path "demo-assets"
COPY demo-assets ./demo-assets

# Expose port 8080 for the Spring Boot app
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","app.jar"]
