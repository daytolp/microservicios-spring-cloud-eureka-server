package com.formacionbdi.springboot.app.item;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@Configuration
public class AppConfig {

	@Bean("clienteRest")
	@LoadBalanced
	public RestTemplate registrarRestTemplate() {
		return new RestTemplate();
	}
	
	@Bean
	public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory.configureDefault(id -> {
			System.out.println("id del cortocircuito: " + id);
			return new Resilience4JConfigBuilder(id)
					.circuitBreakerConfig(CircuitBreakerConfig.custom()
							.slidingWindowSize(10)//numero de peticiones en la ventana deslizande
							.failureRateThreshold(50)//umbral o numero de fallos que admite para entrar a estado abierto CircuitBreaker
							.waitDurationInOpenState(Duration.ofSeconds(10L))//Duracion de espera para recuperar el estado normal despues del corto circuito (de un minuto que es el tiempo de espera lo pase a 2 segundos de espera)
							.permittedNumberOfCallsInHalfOpenState(5)//Numero de llamadas en estado semi abierto, por defecto son 10 si de esas 10 mas de la mitad
							// son peticiones fallidas abre de nuevo el cortocircuito, pero si de esas 10 mas del 50% son exitosas pasa a modo cerrado, pero como en ves de 10 puse 5 peticiones, seria lo mismo pero en 5 peticiones
							
							//Estas dos configuraciones son para las llamadas lentas
							.slowCallRateThreshold(50)//configura el porcentaje el umbral de llamadas lentas, significa que si supera o es igual al 50 % de llamadas lentas entra a circuitbreaker estado abierto
							.slowCallDurationThreshold(Duration.ofSeconds(2L))//tiempo maximo que se debe demorar una llamada, si una llamada o peticion supera los 2 segundos definidos aqui se considera llamada lenta
							.build())
					.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(6L)).build())// tiempo de espera una peticion el cuircuitbreaker en segundos
					.build();
		});
	}
}
