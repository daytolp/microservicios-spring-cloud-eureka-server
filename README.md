PROYECTO CREADO CON SPRING CLOUD, JAVA 11 Y MAVEN

Este proyecto contiene Resilience4j con la versión de sprin boot 2.5.3 y springcloud 2020.0.3.
* Tiene dos proxis (servicios) uno con Zuul-server en la versión 2.3.12.RELEASE y otro Api-Gateway Reactivo con spring boot 2.5.3.
* La Authenticación esta desarrollada con Oauth 2 y spring security en el servicio (springboot-servicio-oauth) y cuenta con la configuración en los servicios (springboot-servicio-gateway-server) y (springboot-servicio-zuul-server).
* Cuenta con un servicio de configuración el cual centraliza información de los properties de cada uno de los microservicios que lo componen.
* El consumo entre microservicios es por medio de Feign y RestTemplate
* Base datos son Postgres y MySql.
* Cuenta con Sleuth y Zipkin para la trazabilidad de logs mediante la configuración de RabbitMQ
* Los microservicios cuntan con su archivo Dockerfile y Docker-compose
