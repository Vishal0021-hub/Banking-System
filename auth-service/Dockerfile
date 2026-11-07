# =============================================================
#  auth-service  (Port 8081)
#  Build context: repo root
#  docker build -f auth-service/Dockerfile -t bankops/auth-service .
# =============================================================

FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY common-lib /build/common-lib
COPY auth-service /build/auth-service
RUN mvn -f /build/pom.xml install -pl common-lib -am -DskipTests --no-transfer-progress
RUN mvn -f /build/auth-service/pom.xml package -DskipTests --no-transfer-progress

FROM amazoncorretto:21-alpine
RUN addgroup -S banking && adduser -S bankingapp -G banking
WORKDIR /app
COPY --from=builder /build/auth-service/target/*.jar app.jar
RUN chown bankingapp:banking app.jar
USER bankingapp
EXPOSE 8081
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
