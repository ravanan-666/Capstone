# Multi-stage Dockerfile for DJ Mart E-Commerce Website
# Stage 1: Build & Package WAR with Maven & Eclipse Temurin JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy Maven Project Object Model
COPY pom.xml .

# Download dependencies offline for caching
RUN mvn -B dependency:go-offline -DskipTests || true

# Copy source code and resources
COPY src ./src

# Build production WAR artifact
RUN mvn -B clean package -DskipTests

# Stage 2: Production Runtime with Apache Tomcat 10.1 (Jakarta EE 6)
FROM tomcat:10.1-jdk17-temurin

WORKDIR /usr/local/tomcat

# Clean default web applications
RUN rm -rf webapps/*

# Create application data directory for embedded H2 database
RUN mkdir -p /usr/local/tomcat/data && chmod 777 /usr/local/tomcat/data

# Copy built WAR as ROOT.war to serve at the root context ("/")
COPY --from=builder /build/target/djmart.war /usr/local/tomcat/webapps/ROOT.war

# Default cloud container port
ENV PORT=8080
EXPOSE 8080 10000

# Copy startup script that ensures dual-port binding (8080 and 10000) for Render
COPY scripts/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

CMD ["/usr/local/bin/docker-entrypoint.sh"]
