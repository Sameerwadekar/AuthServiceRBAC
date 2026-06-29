package com.learn.auth.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.learn.auth.security.jwt.AuthEntryPointJwt;
import com.learn.auth.security.jwt.AuthTokenFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	private AuthTokenFilter authTokenFilter;
	
	@Autowired
	private AuthEntryPointJwt authEntryPointJwt;
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
		httpSecurity.csrf(csrf -> csrf.disable())
		.cors(cors -> {})
		.exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPointJwt))
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .requestCache(cache -> cache.disable())
		.authorizeHttpRequests(req -> req.requestMatchers(HttpMethod.POST,"/users/**","/auth/login","/cart/**","/cart-item/**","/products/**").permitAll()
		.requestMatchers(HttpMethod.GET, "/product/**", "/products/**", "/categories/**","/cart/**").permitAll()
		.requestMatchers(
			    "/",
			    "/health",
			    "/favicon.ico"
			).permitAll()
		.requestMatchers(HttpMethod.DELETE,"/cart/**").permitAll()
		.requestMatchers(HttpMethod.PUT,"/cart/**").permitAll()
		.requestMatchers(HttpMethod.GET,"/orders/new-order").hasRole("ADMIN")
		.requestMatchers(HttpMethod.POST, "/oauth2/**").permitAll()
		.requestMatchers(HttpMethod.GET,"/oauth2/**").permitAll()
		.requestMatchers("/login", "/login/**", "/error").permitAll()
		.anyRequest().authenticated()) ;
		httpSecurity.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
		return httpSecurity.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
