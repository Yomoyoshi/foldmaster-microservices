FROM amazoncorretto:17-alpine
WORKDIR /app
COPY target/product-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]