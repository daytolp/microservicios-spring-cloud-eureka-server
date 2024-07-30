package com.formacionbdi.springboot.app.oauth.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.formacionbdi.springboot.app.commons.usuarios.models.entity.Usuario;
import com.formacionbdi.springboot.app.oauth.services.IUsuarioService;

import brave.Tracer;
import feign.FeignException;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {
	private Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);
	
	@Autowired
	private IUsuarioService usuarioService;
	
	@Autowired
	private Tracer tracer;
	
	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		if (authentication.getDetails() instanceof WebAuthenticationDetails) return;
		UserDetails user = (UserDetails) authentication.getPrincipal();
		log.info("success login -> {}", user.getUsername());
		Usuario usuario = usuarioService.findByUsername(authentication.getName());
		if (usuario.getIntentos() != null && usuario.getIntentos() > 0) {
			usuario.setIntentos(0);
			usuarioService.update(usuario, usuario.getId());
		}
	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		String mensaje = "error en el login -> " + exception.getMessage();
		log.info(mensaje);
		try {
			StringBuilder error = new StringBuilder();
			error.append(mensaje);
			Usuario usuario = usuarioService.findByUsername(authentication.getName());
			if (usuario.getIntentos() == null) usuario.setIntentos(0);
			
			log.info("Intentos antes: {}", usuario.getIntentos());
			usuario.setIntentos(usuario.getIntentos() + 1);
			log.info("Intentos despues: {}", usuario.getIntentos());
			
			error.append(" - Intentos del login: " + usuario.getIntentos());
			
			if (usuario.getIntentos() >= 3) {
				String errorMAxIntentos = String.format("Usuario deshabilitado por %s intentos.", usuario.getIntentos());
				log.info(errorMAxIntentos);
				error.append(" - " + errorMAxIntentos);
				usuario.setEnabled(false);
			}
			
			Usuario userUpdated = usuarioService.update(usuario, usuario.getId());
			log.info("Usuario actualizado: " + userUpdated.getUsername() + " intentos tiene: " + userUpdated.getIntentos());
			
			tracer.currentSpan().tag("error.mensaje", error.toString());
		} catch (FeignException e) {
			e.printStackTrace();
			log.error(String.format("El usuario %s no existe en el sistema.", authentication.getName()));
		}
	}

}
