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

import java.security.Principal;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Controller
public class GlobalControllerAdvice {

	private final UserRepository userRepository;

	public GlobalControllerAdvice(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@ModelAttribute
	public void addUserInfoToModel(Model model, Principal principal) {
		if (principal != null) {
			String username = principal.getName();
			Optional<User> userOpt = userRepository.findByUsername(username);
			if (userOpt.isPresent()) {
				User user = userOpt.get();
				model.addAttribute("currentUser", user);
				model.addAttribute("isAdmin", user.getRoles().contains(Role.ADMIN));
			}
		}
	}

}
