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
package org.springframework.samples.petclinic.tenant;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.springframework.samples.petclinic.security.User;

/**
 * Servlet filter that initializes and cleans up the tenant context for each request.
 * Extracts clinic_id from Spring Security context first, then from request header.
 *
 * @author Multi-Tenant Architecture Team
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantContextFilter implements Filter {

	private static final String CLINIC_ID_HEADER = "X-Clinic-Id";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			setupTenantContext((HttpServletRequest) request);
			chain.doFilter(request, response);
		}
		finally {
			TenantContext.clear();
		}
	}

	private void setupTenantContext(HttpServletRequest request) {
		Integer clinicIdFromSecurity = getClinicIdFromSecurityContext();
		if (clinicIdFromSecurity != null) {
			TenantContext.setCurrentClinicId(clinicIdFromSecurity);
			return;
		}

		String clinicIdHeader = request.getHeader(CLINIC_ID_HEADER);
		if (clinicIdHeader != null && !clinicIdHeader.isEmpty()) {
			try {
				Integer clinicId = Integer.parseInt(clinicIdHeader);
				TenantContext.setCurrentClinicId(clinicId);
			}
			catch (NumberFormatException e) {
				TenantContext.clear();
			}
		}
	}

	private Integer getClinicIdFromSecurityContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()
				&& authentication.getPrincipal() instanceof User user) {
			return user.getClinicId();
		}
		return null;
	}

}
