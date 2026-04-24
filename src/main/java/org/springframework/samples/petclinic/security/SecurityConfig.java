/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.security;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final JwtCookieFilter jwtCookieFilter;

	private final Optional<OAuth2LoginSuccessHandler> oauth2LoginSuccessHandler;

	private final FormLoginSuccessHandler formLoginSuccessHandler;

	private final CustomUserDetailsService userDetailsService;

	private final Optional<ClientRegistrationRepository> clientRegistrationRepository;

	public SecurityConfig(JwtCookieFilter jwtCookieFilter,
			ObjectProvider<OAuth2LoginSuccessHandler> oauth2LoginSuccessHandler,
			FormLoginSuccessHandler formLoginSuccessHandler, CustomUserDetailsService userDetailsService,
			ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) {
		this.jwtCookieFilter = jwtCookieFilter;
		this.oauth2LoginSuccessHandler = Optional.ofNullable(oauth2LoginSuccessHandler.getIfAvailable());
		this.formLoginSuccessHandler = formLoginSuccessHandler;
		this.userDetailsService = userDetailsService;
		this.clientRegistrationRepository = Optional.ofNullable(clientRegistrationRepository.getIfAvailable());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth.requestMatchers("/", "/login", "/register", "/logout")
				.permitAll()
				.requestMatchers("/resources/**", "/webjars/**")
				.permitAll()
				.requestMatchers("/h2-console/**")
				.permitAll()
				.requestMatchers("/actuator/**")
				.permitAll()
				.requestMatchers("/error")
				.permitAll()
				.requestMatchers("/vets/**", "/vets.html")
				.permitAll()
				.anyRequest()
				.authenticated())
			.userDetailsService(userDetailsService)
			.formLogin(form -> form.loginPage("/login")
				.usernameParameter("username")
				.passwordParameter("password")
				.successHandler(formLoginSuccessHandler)
				.failureUrl("/login?error=true")
				.permitAll())
			.logout(logout -> logout.logoutUrl("/logout")
				.logoutSuccessUrl("/")
				.deleteCookies("JWT_TOKEN")
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.permitAll())
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

		if (clientRegistrationRepository.isPresent() && oauth2LoginSuccessHandler.isPresent()) {
			http.oauth2Login(oauth2 -> oauth2.loginPage("/login")
				.successHandler(oauth2LoginSuccessHandler.get())
				.failureUrl("/login?oauthError=true"));
		}

		http.addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}
