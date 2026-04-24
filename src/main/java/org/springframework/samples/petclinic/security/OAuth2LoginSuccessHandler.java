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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final UserRepository userRepository;

	private final JwtTokenProvider tokenProvider;

	@Value("${jwt.cookie.max-age:86400}")
	private int cookieMaxAge;

	public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtTokenProvider tokenProvider) {
		this.userRepository = userRepository;
		this.tokenProvider = tokenProvider;
	}

	@Override
	@Transactional
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
		OAuth2User oauth2User = oauthToken.getPrincipal();
		String registrationId = oauthToken.getAuthorizedClientRegistrationId();

		Map<String, Object> attributes = oauth2User.getAttributes();
		String email = (String) attributes.get("email");
		String name = (String) attributes.get("name");
		String oauthId = (String) attributes.get("sub");

		Optional<User> existingUser = userRepository.findByOauthProviderAndOauthId(registrationId, oauthId);

		User user;
		if (existingUser.isPresent()) {
			user = existingUser.get();
		}
		else {
			Optional<User> userByEmail = userRepository.findByEmail(email);
			if (userByEmail.isPresent()) {
				user = userByEmail.get();
				user.setOauthProvider(registrationId);
				user.setOauthId(oauthId);
			}
			else {
				user = new User();
				user.setEmail(email);
				user.setUsername(generateUniqueUsername(name, email));
				user.setOauthProvider(registrationId);
				user.setOauthId(oauthId);
				user.setEnabled(true);
				user.addRole(Role.USER);
			}
			user = userRepository.save(user);
		}

		String token = tokenProvider.generateToken(user);
		addJwtCookie(response, token);

		clearAuthenticationAttributes(request);
		getRedirectStrategy().sendRedirect(request, response, "/");
	}

	private String generateUniqueUsername(String name, String email) {
		String baseUsername;
		if (name != null && !name.isEmpty()) {
			baseUsername = name.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9_]", "");
		}
		else {
			baseUsername = email.split("@")[0].toLowerCase();
		}

		if (!userRepository.existsByUsername(baseUsername)) {
			return baseUsername;
		}

		int counter = 1;
		while (userRepository.existsByUsername(baseUsername + counter)) {
			counter++;
		}
		return baseUsername + counter;
	}

	private void addJwtCookie(HttpServletResponse response, String token) {
		Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
		jwtCookie.setPath("/");
		jwtCookie.setHttpOnly(true);
		jwtCookie.setSecure(false);
		jwtCookie.setMaxAge(cookieMaxAge);
		response.addCookie(jwtCookie);
	}

}
