FROM amazoncorretto:17-alpine
WORKDIR /app
COPY target/contact-service-1.0.0.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]