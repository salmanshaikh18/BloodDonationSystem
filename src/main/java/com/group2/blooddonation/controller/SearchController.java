package com.group2.blooddonation.controller;

import com.group2.blooddonation.dto.DonorSearchCriteria;
import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.service.DonorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * "Find donors": suitable donors for a patient's blood group.
 */
@Controller
public class SearchController {

    private final DonorService donorService;

    public SearchController(DonorService donorService) {
        this.donorService = donorService;
    }

    @GetMapping("/search")
    public String search(@ModelAttribute("criteria") DonorSearchCriteria criteria,
                         BindingResult result,
                         Model model) {
        model.addAttribute("bloodGroups", BloodGroup.values());

        // A bad link like ?bloodGroup=XYZ just shows the empty search form
        if (!result.hasErrors() && criteria.getBloodGroup() != null) {
            model.addAttribute("results", donorService.findSuitableDonors(criteria));
        }
        return "search/search";
    }
}
