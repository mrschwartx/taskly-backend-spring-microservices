FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -ntp -q dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -q -DskipTests package

FROM eclipse-temurin:25-jre-alpine AS runner
ENV APP_HOST=0.0.0.0 \
    APP_PORT=8081 \
    LOG_FILE=
WORKDIR /app
RUN addgroup -S taskly && adduser -S -G taskly taskly
COPY --from=build --chown=taskly:taskly /build/target/taskly-user-service.jar ./app.jar
COPY --chown=taskly:taskly public ./public
RUN mkdir -p public/images/users && chown -R taskly:taskly /app
USER taskly
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
