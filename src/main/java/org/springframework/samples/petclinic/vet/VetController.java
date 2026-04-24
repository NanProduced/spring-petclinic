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
package org.springframework.samples.petclinic.vet;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

	private static final String VIEWS_VET_CREATE_OR_UPDATE_FORM = "vets/createOrUpdateVetForm";

	private final VetRepository vetRepository;

	private final SpecialtyRepository specialtyRepository;

	public VetController(VetRepository vetRepository, SpecialtyRepository specialtyRepository) {
		this.vetRepository = vetRepository;
		this.specialtyRepository = specialtyRepository;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("specialties")
	public List<Specialty> populateSpecialties() {
		return specialtyRepository.findAll();
	}

	@GetMapping("/vets.html")
	public String showVetList(@RequestParam(defaultValue = "1") int page, Model model) {
		Vets vets = new Vets();
		Page<Vet> paginated = findPaginated(page);
		vets.getVetList().addAll(paginated.toList());
		return addPaginationModel(page, paginated, model);
	}

	private String addPaginationModel(int page, Page<Vet> paginated, Model model) {
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return "vets/vetList";
	}

	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return vetRepository.findAll(pageable);
	}

	@GetMapping({ "/vets" })
	public @ResponseBody Vets showResourcesVetList() {
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetRepository.findAll());
		return vets;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/vets/new")
	public String initCreationForm(Model model) {
		model.addAttribute("vet", new Vet());
		return VIEWS_VET_CREATE_OR_UPDATE_FORM;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/vets/new")
	public String processCreationForm(@Valid Vet vet, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the veterinarian.");
			return VIEWS_VET_CREATE_OR_UPDATE_FORM;
		}
		this.vetRepository.save(vet);
		redirectAttributes.addFlashAttribute("message", "New Veterinarian Created");
		return "redirect:/vets.html";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/vets/{vetId}/edit")
	public String initUpdateVetForm(@PathVariable("vetId") int vetId, Model model) {
		Vet vet = this.vetRepository.findById(vetId)
			.orElseThrow(() -> new IllegalArgumentException("Veterinarian not found with id: " + vetId));
		model.addAttribute("vet", vet);
		return VIEWS_VET_CREATE_OR_UPDATE_FORM;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/vets/{vetId}/edit")
	public String processUpdateVetForm(@Valid Vet vet, BindingResult result, @PathVariable("vetId") int vetId,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the veterinarian.");
			return VIEWS_VET_CREATE_OR_UPDATE_FORM;
		}
		vet.setId(vetId);
		this.vetRepository.save(vet);
		redirectAttributes.addFlashAttribute("message", "Veterinarian Values Updated");
		return "redirect:/vets.html";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/vets/{vetId}/delete")
	public String deleteVet(@PathVariable("vetId") int vetId, RedirectAttributes redirectAttributes) {
		Vet vet = this.vetRepository.findById(vetId)
			.orElseThrow(() -> new IllegalArgumentException("Veterinarian not found with id: " + vetId));
		this.vetRepository.delete(vet);
		redirectAttributes.addFlashAttribute("message", "Veterinarian Deleted");
		return "redirect:/vets.html";
	}

}
