package com.formacionbdi.springboot.app.oauth.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.formacionbdi.springboot.app.oauth.clients.UsuarioFeignClient;

import brave.Tracer;
import feign.FeignException;

import com.formacionbdi.springboot.app.commons.usuarios.models.entity.Usuario;

@Service
public class UsuarioService implements IUsuarioService, UserDetailsService  {

	private Logger log = LoggerFactory.getLogger(UsuarioService.class);
	
	@Autowired
	private UsuarioFeignClient client;
	@Autowired
	private Tracer tracer;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			Usuario usuario = client.findByUsername(username);
			
			List<GrantedAuthority> authorities = usuario.getRoles().stream()
	                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
	            	.peek(authority -> log.info("Role: " + authority.getAuthority()))
	                .collect(Collectors.toList());
			
			log.info("Usuario autenticado: " + username);
			return new User(usuario.getUsername(), usuario.getPassword(), usuario.getEnabled(), true, true, true, authorities);
		}catch (FeignException e) {
			String error = "Error en el login, no existe el usuario '"+username+"' en el sistema";
			log.error(error);
			tracer.currentSpan().tag("error.mensaje", error + " : " + e.getMessage());
			throw new UsernameNotFoundException("Error en el login, no existe el usuario '"+username+"' en el sistema");
		}
		
	}
	
	@Override
	public Usuario findByUsername(String username) {
		return client.findByUsername(username);
	}

	@Override
	public Usuario update(Usuario usuario, Long id) {
		return client.update(usuario, id);
	}


}
