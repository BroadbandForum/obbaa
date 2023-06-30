FROM openjdk:8-jdk-alpine

MAINTAINER Madhukar <madhukar.shetty@nokia.com>

EXPOSE 8093

VOLUME /tmp

#Setup proxy if container is run on behind proxy host
ARG http_proxy
ARG https_proxy

ENV http_proxy $http_proxy
ENV https_proxy $https_proxy

RUN apk add --no-cache bash

ARG JAR_FILE=target/micro-services-discovery-6.0.0.jar

# Add the application's jar to the container
ADD ${JAR_FILE} micro-services-discovery.jar

CMD ["./mvnw", "spring-boot:run"]

# Run the jar file
ENTRYPOINT ["java","-jar","-Djava.security.egd=file:/dev/./urandom","/micro-services-discovery.jar"]