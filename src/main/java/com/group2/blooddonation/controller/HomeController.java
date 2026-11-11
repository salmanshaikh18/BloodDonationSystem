package com.group2.blooddonation.controller;

import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.service.DonorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The home page: totals, donors per blood group and a quick search.
 */
@Controller
public class HomeController {

    private final DonorService donorService;

    public HomeController(DonorService donorService) {
        this.donorService = donorService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("stats", donorService.getDashboardStats());
        model.addAttribute("recentDonors", donorService.findRecentlyRegistered());
        model.addAttribute("bloodGroups", BloodGroup.values());
        return "home";
    }
}
