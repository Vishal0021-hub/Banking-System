# =============================================================
#  transaction-service  (Port 8084)
#  Build context: repo root
#  docker build -f transaction-service/Dockerfile -t bankops/transaction-service .
# =============================================================

FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY common-lib /build/common-lib
COPY transaction-service /build/transaction-service
RUN mvn -f /build/pom.xml -N install -DskipTests --no-transfer-progress && mvn -f /build/common-lib/pom.xml install -DskipTests --no-transfer-progress
RUN mvn -f /build/transaction-service/pom.xml package -DskipTests --no-transfer-progress

FROM amazoncorretto:21-alpine
RUN addgroup -S banking && adduser -S bankingapp -G banking
WORKDIR /app
COPY --from=builder /build/transaction-service/target/*.jar app.jar
RUN chown bankingapp:banking app.jar
USER bankingapp
EXPOSE 8084
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
