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

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Specifications for tenant-aware queries.
 *
 * @author Multi-Tenant Architecture Team
 */
public class TenantSpecifications {

	private TenantSpecifications() {
	}

	public static <T extends TenantAware> Specification<T> byCurrentClinic() {
		return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
			Integer clinicId = TenantContext.getCurrentClinicId();
			if (clinicId == null) {
				return builder.conjunction();
			}
			Predicate clinicPredicate = builder.equal(root.get("clinicId"), clinicId);
			return builder.and(clinicPredicate);
		};
	}

	public static <T extends TenantAware> Specification<T> byClinicId(Integer clinicId) {
		return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
			if (clinicId == null) {
				return builder.conjunction();
			}
			return builder.equal(root.get("clinicId"), clinicId);
		};
	}

}
