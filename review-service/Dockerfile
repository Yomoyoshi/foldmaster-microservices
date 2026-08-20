FROM amazoncorretto:17-alpine
WORKDIR /app

# Копируем собранный JAR из локальной папки target
COPY target/review-service-*.jar app.jar

# Очищаем кэш apk (если есть)
RUN rm -rf /var/cache/apk/*

# Порт
EXPOSE 8083

# Запуск
ENTRYPOINT ["java", "-jar", "app.jar"]