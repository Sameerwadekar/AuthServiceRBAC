package com.learn.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class SecurityConfig {

	private final AuthTokenFilter authTokenFilter;
	private final AuthEntryPointJwt authEntryPointJwt;

	public SecurityConfig(AuthTokenFilter authTokenFilter, AuthEntryPointJwt authEntryPointJwt) {
		this.authTokenFilter = authTokenFilter;
		this.authEntryPointJwt = authEntryPointJwt;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(csrf -> csrf.disable())
				.cors(cors -> {})
				.exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPointJwt))
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.requestCache(cache -> cache.disable())
				.authorizeHttpRequests(req -> req
						.requestMatchers("/", "/health", "/favicon.ico", "/users/register", "/users/login", "/users/refresh").permitAll()
						.anyRequest().authenticated()
				);
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
