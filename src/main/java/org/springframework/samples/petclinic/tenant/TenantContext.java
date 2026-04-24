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

/**
 * Context holder for the current tenant (clinic) identifier. Uses ThreadLocal to store
 * the clinic ID for the current request.
 *
 * @author Multi-Tenant Architecture Team
 */
public final class TenantContext {

	private TenantContext() {
	}

	private static final ThreadLocal<Integer> currentClinicId = new ThreadLocal<>();

	public static void setCurrentClinicId(Integer clinicId) {
		currentClinicId.set(clinicId);
	}

	public static Integer getCurrentClinicId() {
		return currentClinicId.get();
	}

	public static void clear() {
		currentClinicId.remove();
	}

}
