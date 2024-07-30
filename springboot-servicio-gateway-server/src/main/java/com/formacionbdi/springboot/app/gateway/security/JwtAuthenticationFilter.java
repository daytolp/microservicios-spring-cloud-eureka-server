package com.formacionbdi.springboot.app.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;


@Component
public class JwtAuthenticationFilter implements WebFilter {
	
	@Autowired
	private ReactiveAuthenticationManager authenticationManager;
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
				.filter(authHeader -> authHeader.startsWith("Bearer "))//filtramos para obtener solo el token
				.switchIfEmpty(chain.filter(exchange).then(Mono.empty()))//si no existe el Bearer se termina el flujo devolviendo un Mono vacio;
				.map(token -> token.replace("Bearer ", ""))//le eliminamos la plabra Bearer para dejar solo el token
				.flatMap(token -> authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(null, token)))//flatMAp se usa cuando devuelve un flujo en vez de un objeto comun y corriente en este caso como 
				//devuelve un Mono se debe usar flatMap
				.flatMap(authentication -> chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));//se agrega al contexto de spring security
	}
	
	
}
