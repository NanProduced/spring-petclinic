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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that automatically applies tenant filtering to repository queries.
 *
 * Strategy: - For methods returning single entity/Optional: filter after query by
 * checking clinic_id - For JpaSpecificationExecutor.findAll(): use Specification to
 * filter at query level - For custom query methods returning Collection/Page: filter
 * results after query
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
			return optional.filter(this::belongsToCurrentClinic);
		}
		else if (result instanceof TenantAware tenantAware) {
			Integer entityClinicId = tenantAware.getClinicId();
			if (entityClinicId != null && !entityClinicId.equals(currentClinicId)) {
				return null;
			}
		}

		return result;
	}

	@Around("execution(* org.springframework.data.jpa.repository.JpaSpecificationExecutor+.findAll(..))")
	public Object filterFindAllWithSpecificationExecutor(ProceedingJoinPoint joinPoint) throws Throwable {
		Integer currentClinicId = TenantContext.getCurrentClinicId();

		if (currentClinicId == null) {
			return joinPoint.proceed();
		}

		Object target = joinPoint.getTarget();
		Object[] args = joinPoint.getArgs();

		if (target instanceof JpaSpecificationExecutor<?> executor) {
			@SuppressWarnings("rawtypes")
			Specification spec = TenantSpecifications.byCurrentClinic();

			if (args.length == 0) {
				return executor.findAll(spec);
			}
			else if (args.length == 1 && args[0] instanceof Pageable pageable) {
				return executor.findAll(spec, pageable);
			}
		}

		return joinPoint.proceed();
	}

	@Around("execution(public * org.springframework.data.repository.Repository+.*(..)) "
			+ "&& !execution(* org.springframework.data.repository.Repository+.findById(..)) "
			+ "&& !execution(* org.springframework.data.jpa.repository.JpaSpecificationExecutor+.findAll(..))")
	public Object filterAllRepositoryQueries(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result = joinPoint.proceed();
		Integer currentClinicId = TenantContext.getCurrentClinicId();

		if (currentClinicId == null) {
			return result;
		}

		return filterResult(result, currentClinicId);
	}

	private Object filterResult(Object result, Integer currentClinicId) {
		if (result == null) {
			return null;
		}

		if (result instanceof Optional<?> optional) {
			return optional.filter(this::belongsToCurrentClinic);
		}

		if (result instanceof Page<?> page) {
			List<?> filteredContent = page.getContent()
				.stream()
				.filter(this::belongsToCurrentClinic)
				.collect(Collectors.toList());
			return new PageImpl<>(filteredContent, page.getPageable(), filteredContent.size());
		}

		if (result instanceof List<?> list) {
			return list.stream().filter(this::belongsToCurrentClinic).collect(Collectors.toCollection(ArrayList::new));
		}

		if (result instanceof Collection<?> collection) {
			return collection.stream().filter(this::belongsToCurrentClinic).collect(Collectors.toList());
		}

		if (result instanceof TenantAware tenantAware) {
			Integer entityClinicId = tenantAware.getClinicId();
			if (entityClinicId != null && !entityClinicId.equals(TenantContext.getCurrentClinicId())) {
				return null;
			}
		}

		return result;
	}

	private boolean belongsToCurrentClinic(Object entity) {
		Integer currentClinicId = TenantContext.getCurrentClinicId();
		if (currentClinicId == null) {
			return true;
		}

		if (entity instanceof TenantAware tenantAware) {
			Integer entityClinicId = tenantAware.getClinicId();
			return entityClinicId == null || entityClinicId.equals(currentClinicId);
		}

		return true;
	}

}
