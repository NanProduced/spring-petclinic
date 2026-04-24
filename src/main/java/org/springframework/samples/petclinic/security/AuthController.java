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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtTokenProvider tokenProvider;

	@Value("${jwt.cookie.max-age:86400}")
	private int cookieMaxAge;

	public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
			JwtTokenProvider tokenProvider) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenProvider = tokenProvider;
	}

	@GetMapping("/login")
	public String loginPage(@RequestParam(required = false) Boolean error,
			@RequestParam(required = false) Boolean oauthError, Model model) {
		if (error != null && error) {
			model.addAttribute("error", "Invalid username or password");
		}
		if (oauthError != null && oauthError) {
			model.addAttribute("error", "OAuth2 authentication failed");
		}
		return "auth/login";
	}

	@GetMapping("/register")
	public String registerPage(Model model) {
		model.addAttribute("user", new RegistrationForm());
		return "auth/register";
	}

	@PostMapping("/register")
	public String processRegister(@Valid @ModelAttribute("user") RegistrationForm form, BindingResult result,
			Model model, HttpServletResponse response) {
		if (result.hasErrors()) {
			return "auth/register";
		}

		if (userRepository.existsByUsername(form.getUsername())) {
			result.rejectValue("username", "duplicate", "Username already exists");
			return "auth/register";
		}

		if (userRepository.existsByEmail(form.getEmail())) {
			result.rejectValue("email", "duplicate", "Email already exists");
			return "auth/register";
		}

		if (!form.getPassword().equals(form.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
			return "auth/register";
		}

		User user = new User();
		user.setUsername(form.getUsername());
		user.setEmail(form.getEmail());
		user.setPassword(passwordEncoder.encode(form.getPassword()));
		user.setEnabled(true);
		user.addRole(Role.USER);

		user = userRepository.save(user);

		String token = tokenProvider.generateToken(user);
		addJwtCookie(response, token);

		return "redirect:/";
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
