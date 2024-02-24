FROM amazoncorretto:17-alpine

# Add app user
ARG APPLICATION_USER=appuser
RUN adduser --no-create-home -u 1000 -D "$APPLICATION_USER"

# Configure working directory
RUN mkdir /app && \
    chown -R "$APPLICATION_USER" /app

USER 1000

COPY --chown=1000:1000 ./target/airq-prometheus-exporter-1.0.0.jar /app/airq-prometheus-exporter.jar
WORKDIR /app

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/app/airq-prometheus-exporter.jar" ]
