# ============================================
# GeoEdu Backend - Multi-stage Docker Build
# ============================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="GeoEdu Team"
LABEL version="1.0"
LABEL description="GeoEdu Backend Service"

WORKDIR /app

RUN addgroup -S geoedu && adduser -S geoedu -G geoedu

RUN mkdir -p /data/images && chown -R geoedu:geoedu /data/images

COPY --from=builder /build/target/*.jar app.jar

RUN chown geoedu:geoedu app.jar

USER geoedu

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m" \
    SPRING_PROFILES_ACTIVE="docker" \
    TZ="Asia/Shanghai"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/api/v1/admin/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
