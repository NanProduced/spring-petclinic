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

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that automatically applies tenant filtering to repository queries.
 * Intercepts findById methods and applies clinic_id filtering.
 *
 * @author Multi-Tenant Architecture Team
 */
@Aspect
@Component
public class TenantRepositoryAspect {

	@Around("execution(* org.springframework.data.repository.Repository+.findById(..))")
	public Object filterFindById(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result = joinPoint.proceed();

		Integer currentClinicId = TenantContext.getCurrentClinicId();
		if (currentClinicId == null) {
			return result;
		}

		if (result instanceof Optional<?> optional) {
			if (optional.isPresent()) {
				Object entity = optional.get();
				if (entity instanceof TenantAware tenantAware) {
					Integer entityClinicId = tenantAware.getClinicId();
					if (entityClinicId != null && !entityClinicId.equals(currentClinicId)) {
						return Optional.empty();
					}
				}
			}
		}
		else if (result instanceof TenantAware tenantAware) {
			Integer entityClinicId = tenantAware.getClinicId();
			if (entityClinicId != null && !entityClinicId.equals(currentClinicId)) {
				return null;
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Around("execution(* org.springframework.data.jpa.repository.JpaSpecificationExecutor+.findAll(..)) && args(pageable)")
	public Object filterFindAllWithPageable(ProceedingJoinPoint joinPoint,
			org.springframework.data.domain.Pageable pageable) throws Throwable {
		Integer currentClinicId = TenantContext.getCurrentClinicId();
		if (currentClinicId == null) {
			return joinPoint.proceed();
		}

		Object target = joinPoint.getTarget();
		if (target instanceof org.springframework.data.jpa.repository.JpaSpecificationExecutor<?> executor) {
			@SuppressWarnings("rawtypes")
			Specification spec = TenantSpecifications.byCurrentClinic();
			return executor.findAll(spec, pageable);
		}

		return joinPoint.proceed();
	}

	@SuppressWarnings("unchecked")
	@Around("execution(* org.springframework.data.jpa.repository.JpaSpecificationExecutor+.findAll())")
	public Object filterFindAll(ProceedingJoinPoint joinPoint) throws Throwable {
		Integer currentClinicId = TenantContext.getCurrentClinicId();
		if (currentClinicId == null) {
			return joinPoint.proceed();
		}

		Object target = joinPoint.getTarget();
		if (target instanceof org.springframework.data.jpa.repository.JpaSpecificationExecutor<?> executor) {
			@SuppressWarnings("rawtypes")
			Specification spec = TenantSpecifications.byCurrentClinic();
			return executor.findAll(spec);
		}

		return joinPoint.proceed();
	}

}
