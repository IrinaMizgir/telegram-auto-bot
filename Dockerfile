FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

ARG APP_TELEGRAM_BOT_TOKEN=test

ENV APP_TELEGRAM_BOT_TOKEN=${APP_TELEGRAM_BOT_TOKEN}

RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

COPY --chown=appuser:appuser build/libs/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar"]
