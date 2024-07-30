FROM openjdk:11
VOLUME /tmp
EXPOSE 8090
ADD ./target/springboot-servicio-gateway-server-0.0.1-SNAPSHOT.jar servicio-gateway.jar
ENTRYPOINT ["java", "-jar", "servicio-gateway.jar"]