package com.group2.blooddonation.controller;

import com.group2.blooddonation.dto.DonorForm;
import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.model.Donor;
import com.group2.blooddonation.model.Gender;
import com.group2.blooddonation.service.DonorService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Donor pages: list, register, view, edit, availability, record donation and delete.
 *
 * <p>Every form submission ends with a redirect (Post/Redirect/Get),
 * so refreshing the page never submits a form twice.
 */
@Controller
@RequestMapping("/donors")
public class DonorController {

    private static final String FORM_VIEW = "donors/form";
    private static final String SUCCESS_MESSAGE = "successMessage";

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    // Values every donor page can use in its JSP
    @ModelAttribute("bloodGroups")
    public BloodGroup[] bloodGroups() {
        return BloodGroup.values();
    }

    @ModelAttribute("genders")
    public Gender[] genders() {
        return Gender.values();
    }

    @ModelAttribute("today")
    public LocalDate today() {
        return LocalDate.now();
    }

    // ----- List and view -----

    /** The donors list, 10 per page: /donors, /donors?page=2, /donors?bloodGroup=O_POSITIVE&page=2 */
    @GetMapping
    public String list(@RequestParam(required = false) BloodGroup bloodGroup,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        Page<Donor> donorPage = donorService.findPage(bloodGroup, page);
        model.addAttribute("donorPage", donorPage);
        model.addAttribute("donors", donorPage.getContent());
        model.addAttribute("selectedBloodGroup", bloodGroup);
        return "donors/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("donor", donorService.getById(id));
        return "donors/details";
    }

    // ----- Register -----

    @GetMapping("/new")
    public String showRegisterForm(Model model) {
        model.addAttribute("donorForm", new DonorForm());
        return FORM_VIEW;
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("donorForm") DonorForm form,
                           BindingResult result,
                           RedirectAttributes redirect) {
        rejectIfPhoneTaken(form, null, result);
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        Donor donor = donorService.register(form);
        redirect.addFlashAttribute(SUCCESS_MESSAGE, donor.getFullName() + " has been registered as a donor.");
        return "redirect:/donors/" + donor.getId();
    }

    // ----- Edit -----

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("donorForm", DonorForm.from(donorService.getById(id)));
        model.addAttribute("donorId", id);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("donorForm") DonorForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        donorService.getById(id); // shows the 404 page if the donor was deleted meanwhile
        rejectIfPhoneTaken(form, id, result);
        if (result.hasErrors()) {
            model.addAttribute("donorId", id);
            return FORM_VIEW;
        }
        donorService.update(id, form);
        redirect.addFlashAttribute(SUCCESS_MESSAGE, "Donor details updated.");
        return "redirect:/donors/" + id;
    }

    // ----- Quick actions on the donor page -----

    @PostMapping("/{id}/availability")
    public String changeAvailability(@PathVariable Long id,
                                     @RequestParam boolean available,
                                     RedirectAttributes redirect) {
        Donor donor = donorService.changeAvailability(id, available);
        String status = available ? "available" : "not available";
        redirect.addFlashAttribute(SUCCESS_MESSAGE, donor.getFullName() + " is now marked as " + status + ".");
        return "redirect:/donors/" + id;
    }

    @PostMapping("/{id}/donations")
    public String recordDonation(@PathVariable Long id, RedirectAttributes redirect) {
        Donor donor = donorService.recordDonation(id, LocalDate.now());
        redirect.addFlashAttribute(SUCCESS_MESSAGE, "Donation recorded for today.");
        return "redirect:/donors/" + donor.getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        Donor donor = donorService.delete(id);
        redirect.addFlashAttribute(SUCCESS_MESSAGE, donor.getFullName() + " has been removed.");
        return "redirect:/donors";
    }

    /** Two donors can't share a mobile number. */
    private void rejectIfPhoneTaken(DonorForm form, Long donorId, BindingResult result) {
        if (!result.hasFieldErrors("phone") && donorService.isPhoneTaken(form.getPhone(), donorId)) {
            result.rejectValue("phone", "duplicate", "Another donor already uses this mobile number");
        }
    }
}
