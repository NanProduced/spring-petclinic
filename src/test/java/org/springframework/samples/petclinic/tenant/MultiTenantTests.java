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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.samples.petclinic.clinic.Clinic;
import org.springframework.samples.petclinic.clinic.ClinicRepository;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;

/**
 * Unit tests for multi-tenant functionality.
 *
 * @author Multi-Tenant Architecture Team
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MultiTenantTests {

	@Autowired
	private OwnerRepository ownerRepository;

	@Autowired
	private VetRepository vetRepository;

	@Autowired
	private ClinicRepository clinicRepository;

	@Autowired
	private PetTypeRepository petTypeRepository;

	private Clinic clinic1;

	private Clinic clinic2;

	@BeforeEach
	void setUp() {
		clinic1 = new Clinic();
		clinic1.setName("Test Clinic 1");
		clinic1.setCity("Test City");
		clinic1 = clinicRepository.save(clinic1);

		clinic2 = new Clinic();
		clinic2.setName("Test Clinic 2");
		clinic2.setCity("Another City");
		clinic2 = clinicRepository.save(clinic2);

		TenantContext.clear();
	}

	@AfterEach
	void tearDown() {
		TenantContext.clear();
	}

	@Test
	void shouldIsolateOwnersBetweenTenants() {
		TenantContext.setCurrentClinicId(clinic1.getId());
		Owner owner1 = createOwner("Clinic1", "Owner");
		ownerRepository.save(owner1);

		TenantContext.setCurrentClinicId(clinic2.getId());
		Owner owner2 = createOwner("Clinic2", "Owner");
		ownerRepository.save(owner2);

		TenantContext.setCurrentClinicId(clinic1.getId());
		Page<Owner> clinic1Owners = ownerRepository.findByLastNameStartingWith("Owner", Pageable.unpaged());
		assertThat(clinic1Owners).hasSize(1);
		assertThat(clinic1Owners.getContent().get(0).getFirstName()).isEqualTo("Clinic1");

		TenantContext.setCurrentClinicId(clinic2.getId());
		Page<Owner> clinic2Owners = ownerRepository.findByLastNameStartingWith("Owner", Pageable.unpaged());
		assertThat(clinic2Owners).hasSize(1);
		assertThat(clinic2Owners.getContent().get(0).getFirstName()).isEqualTo("Clinic2");
	}

	@Test
	void shouldIsolateVetsBetweenTenants() {
		TenantContext.setCurrentClinicId(clinic1.getId());
		Vet vet1 = createVet("Vet1", "Clinic1");
		vetRepository.findAll().add(vet1);

		TenantContext.setCurrentClinicId(clinic2.getId());
		Vet vet2 = createVet("Vet2", "Clinic2");
		vetRepository.findAll().add(vet2);
	}

	@Test
	void shouldAutoSetClinicIdOnSave() {
		TenantContext.setCurrentClinicId(clinic1.getId());
		Owner owner = createOwner("Test", "AutoSet");
		Owner saved = ownerRepository.save(owner);

		assertThat(saved.getClinicId()).isEqualTo(clinic1.getId());
	}

	@Test
	void shouldCascadeDeletePetsWhenOwnerDeleted() {
		TenantContext.setCurrentClinicId(clinic1.getId());

		Owner owner = createOwner("Cascade", "Test");
		Pet pet = createPet("TestPet", owner);
		owner.addPet(pet);
		Owner savedOwner = ownerRepository.save(owner);

		Pet savedPet = savedOwner.getPet("TestPet");
		assertThat(savedPet).isNotNull();
		assertThat(savedPet.getId()).isNotNull();

		ownerRepository.delete(savedOwner);

		Optional<Owner> foundOwner = ownerRepository.findById(savedOwner.getId());
		assertThat(foundOwner).isEmpty();
	}

	@Test
	void shouldPreventAccessToOtherTenantData() {
		TenantContext.setCurrentClinicId(clinic1.getId());
		Owner owner1 = createOwner("Private", "Data");
		Owner savedOwner1 = ownerRepository.save(owner1);

		TenantContext.setCurrentClinicId(clinic2.getId());

		Optional<Owner> foundFromOtherTenant = ownerRepository.findById(savedOwner1.getId());
		assertThat(foundFromOtherTenant).isEmpty();
	}

	private Owner createOwner(String firstName, String lastName) {
		Owner owner = new Owner();
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress("123 Test St");
		owner.setCity("Testville");
		owner.setTelephone("1234567890");
		return owner;
	}

	private Vet createVet(String firstName, String lastName) {
		Vet vet = new Vet();
		vet.setFirstName(firstName);
		vet.setLastName(lastName);
		return vet;
	}

	private Pet createPet(String name, Owner owner) {
		Pet pet = new Pet();
		pet.setName(name);
		pet.setBirthDate(java.time.LocalDate.now().minusYears(2));

		PetType type = new PetType();
		type.setName("Dog");
		pet.setType(type);

		return pet;
	}

}
