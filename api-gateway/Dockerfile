# =============================================================
#  API Gateway — Edge Server, JWT Auth & Routing (Port 8088)
#  Build: docker build -t bankops/api-gateway .
# =============================================================

FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY common-lib /build/common-lib
COPY api-gateway /build/api-gateway
RUN mvn -f /build/pom.xml -N install -DskipTests --no-transfer-progress && mvn -f /build/common-lib/pom.xml install -DskipTests --no-transfer-progress
RUN mvn -f /build/api-gateway/pom.xml package -DskipTests --no-transfer-progress

FROM amazoncorretto:21-alpine
RUN addgroup -S banking && adduser -S bankingapp -G banking
WORKDIR /app
COPY --from=builder /build/api-gateway/target/*.jar app.jar
RUN chown bankingapp:banking app.jar
USER bankingapp
EXPOSE 8088
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
