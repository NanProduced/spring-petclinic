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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import org.springframework.samples.petclinic.tenant.TenantContext;

import jakarta.servlet.http.HttpSession;

/**
 * Security configuration for the multi-tenant pet clinic application.
 *
 * @author Multi-Tenant Architecture Team
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private final TenantAuthenticationSuccessHandler successHandler;

	public SecurityConfiguration(TenantAuthenticationSuccessHandler successHandler) {
		this.successHandler = successHandler;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(
				auth -> auth.requestMatchers("/", "/login", "/resources/**", "/webjars/**", "/error", "/actuator/**")
					.permitAll()
					.anyRequest()
					.authenticated())
			.formLogin(form -> form.loginPage("/login").permitAll().successHandler(successHandler))
			.logout(logout -> logout.logoutUrl("/logout")
				.logoutSuccessUrl("/")
				.addLogoutHandler((request, response, authentication) -> {
					TenantContext.clear();
					HttpSession session = request.getSession(false);
					if (session != null) {
						session.invalidate();
					}
					new SecurityContextLogoutHandler().logout(request, response, authentication);
				})
				.permitAll())
			.csrf(csrf -> csrf.disable());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
