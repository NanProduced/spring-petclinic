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

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		if (!userRepository.existsByUsername("admin")) {
			User admin = new User();
			admin.setUsername("admin");
			admin.setEmail("admin@petclinic.com");
			admin.setPassword(passwordEncoder.encode("admin123"));
			admin.setEnabled(true);
			admin.addRole(Role.ADMIN);
			admin.addRole(Role.USER);
			userRepository.save(admin);
		}

		if (!userRepository.existsByUsername("user")) {
			User user = new User();
			user.setUsername("user");
			user.setEmail("user@petclinic.com");
			user.setPassword(passwordEncoder.encode("user123"));
			user.setEnabled(true);
			user.addRole(Role.USER);
			userRepository.save(user);
		}
	}

}
