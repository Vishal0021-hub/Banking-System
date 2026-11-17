# =============================================================
#  admin-service  (Port 8087)
#  Build context: repo root
#  docker build -f admin-service/Dockerfile -t bankops/admin-service .
# =============================================================

FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY common-lib /build/common-lib
COPY admin-service /build/admin-service
RUN mvn -f /build/pom.xml -N install -DskipTests --no-transfer-progress && mvn -f /build/common-lib/pom.xml install -DskipTests --no-transfer-progress
RUN mvn -f /build/admin-service/pom.xml package -DskipTests --no-transfer-progress

FROM amazoncorretto:21-alpine
RUN addgroup -S banking && adduser -S bankingapp -G banking
WORKDIR /app
COPY --from=builder /build/admin-service/target/*.jar app.jar
RUN chown bankingapp:banking app.jar
USER bankingapp
EXPOSE 8087
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
