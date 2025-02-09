FROM openjdk:17-jdk-slim

LABEL maintainer="NikaMilon"

WORKDIR /app

COPY target/*.jar /app/app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m"

ENV SPRING_PROFILES_ACTIVE=prod

ENV LOGGING_LEVEL=INFO

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dlogging.level.root=$LOGGING_LEVEL -jar /app/app.jar"]

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
