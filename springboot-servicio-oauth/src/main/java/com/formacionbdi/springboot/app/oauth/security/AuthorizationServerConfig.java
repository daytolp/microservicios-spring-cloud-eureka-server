package com.formacionbdi.springboot.app.oauth.security;

import java.util.Arrays;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.formacionbdi.springboot.app.oauth.services.IUsuarioService;
//import com.formacionbdi.springboot.app.oauth.services.UsuarioService;

@RefreshScope
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
			
	@Autowired
	Environment env;
		
	@Autowired
	private AuthenticationManager authenticationManager;	
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private InfoAdicionalToken infoAdicionalToken;
	
    @Autowired
    private IUsuarioService userDetailsService;
//	private UsuarioService userDetailsService;
	
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()")//para dar permiso a todos los enpoints, el token access es practicamente la ruta oauht/token
		.checkTokenAccess("isAuthenticated()");//se encarga de validar el token
	}

	/**
	 * Configura los clientes (frontend) que se podran conectar a la api mediante una contraseña asi como el alcance si es de escritura o lectura que tendra el front**/
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients
		.inMemory()
		.withClient(env.getProperty("config.security.oauth.client.id"))
		.secret(passwordEncoder.encode(env.getProperty("config.security.oauth.client.secret")))
		.scopes("read", "write")
		.authorizedGrantTypes("password", "refresh_token")//define el tipo de consecion o como es que vamos a obtener el token, en este caso el tipo de consecion es password
		//cuando usamos credenciales, pero tambien existe el authorizationcode (codigo de autorizacion), implicit que se utiliza para aplicaciones publicas que no requieran de mucha seguridad
		//la concesion refresh_token genera un token que sirve para renovar el token antes de que caduce
		.accessTokenValiditySeconds(3600) //tiempo que tarda en caducar el token
		.refreshTokenValiditySeconds(3600)
		.and()
		.withClient("androidApp")
		.secret(passwordEncoder.encode(env.getProperty("config.security.oauth.client.secret")))
		.scopes("read", "write")
		.authorizedGrantTypes("password", "refresh_token")
		.accessTokenValiditySeconds(3600) 
		.refreshTokenValiditySeconds(3600);
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(infoAdicionalToken, accessTokenConverter()));
				
		endpoints.authenticationManager(authenticationManager)
		.tokenStore(tokenStore())//se encarga de guardar el token con los datos del accessTokenconverter, como la firma, el username, roles, etc.
		.accessTokenConverter(accessTokenConverter())//Debe ser de tipo Jwt para crear el token
		.tokenEnhancer(tokenEnhancerChain)
	    .userDetailsService(userDetailsService);
	}

	@Bean
	public JwtTokenStore tokenStore() {		
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
		jwtAccessTokenConverter.setSigningKey(Base64.getEncoder().encodeToString(env.getProperty("config.security.oauth.jwt.key").getBytes()));//agrega la firma al token
		return jwtAccessTokenConverter;
	}
	
	
}
