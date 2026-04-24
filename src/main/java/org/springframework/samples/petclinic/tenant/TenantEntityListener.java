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

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * JPA Entity listener that automatically sets clinic_id on tenant-aware entities.
 *
 * @author Multi-Tenant Architecture Team
 */
public class TenantEntityListener {

	@PrePersist
	public void prePersist(Object entity) {
		if (entity instanceof TenantAware tenantAware) {
			Integer clinicId = TenantContext.getCurrentClinicId();
			if (clinicId != null && tenantAware.getClinicId() == null) {
				tenantAware.setClinicId(clinicId);
			}
		}
	}

	@PreUpdate
	public void preUpdate(Object entity) {
		if (entity instanceof TenantAware tenantAware) {
			Integer clinicId = TenantContext.getCurrentClinicId();
			Integer entityClinicId = tenantAware.getClinicId();
			if (clinicId != null && entityClinicId != null && !clinicId.equals(entityClinicId)) {
				throw new TenantAccessDeniedException("Cannot modify entity from different clinic");
			}
		}
	}

}
