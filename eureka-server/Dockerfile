# =============================================================
#  Eureka Server — Service Discovery
#  Build: docker build -t bankops/eureka-server .
# =============================================================

# ---- Stage 1: Build ----
FROM maven:3.9.6-amazoncorretto-21 AS builder

WORKDIR /build

# 1. Copy root POM (multi-module parent) so Maven can resolve modules
COPY pom.xml /build/pom.xml

# 2. Copy common-lib first (eureka-server has no dep on it, but parent POM lists it)
COPY common-lib /build/common-lib

# 3. Copy this service source
COPY eureka-server /build/eureka-server

# 4. Build only common-lib then this service (skip tests in CI — tests run separately)
RUN mvn -f /build/pom.xml install -pl common-lib -am -DskipTests --no-transfer-progress
RUN mvn -f /build/eureka-server/pom.xml package -DskipTests --no-transfer-progress

# ---- Stage 2: Runtime ----
FROM amazoncorretto:21-alpine

# Non-root user for security
RUN addgroup -S banking && adduser -S bankingapp -G banking

WORKDIR /app

COPY --from=builder /build/eureka-server/target/*.jar app.jar

# Fix ownership
RUN chown bankingapp:banking app.jar

USER bankingapp

EXPOSE 8761

# JVM tuning: container-aware memory limits + fast startup
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
