package com.group2.blooddonation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.model.Donor;
import com.group2.blooddonation.model.Gender;
import com.group2.blooddonation.repository.DonorRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends real HTTP requests to the controllers (without a browser) and checks
 * which page is shown, what data it gets and where forms redirect.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DonorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DonorRepository donorRepository;

    @Test
    void homePageShowsTheNumbers() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("stats", "recentDonors"));
    }

    @Test
    void registersAValidDonorAndOpensTheirPage() throws Exception {
        mockMvc.perform(post("/donors")
                        .param("fullName", "  Kavya Rao ")
                        .param("age", "27")
                        .param("gender", "FEMALE")
                        .param("bloodGroup", "B_NEGATIVE")
                        .param("phone", "+91 98765 43210")
                        .param("city", "Pune")
                        .param("available", "true"))
                .andExpect(redirectedUrlPattern("/donors/*"))
                .andExpect(flash().attribute("successMessage", "Kavya Rao has been registered as a donor."));

        List<Donor> donors = donorRepository.findAll();
        assertThat(donors).hasSize(1);
        assertThat(donors.get(0).getPhone()).isEqualTo("9876543210");
        assertThat(donors.get(0).getBloodGroup()).isEqualTo(BloodGroup.B_NEGATIVE);
    }

    @Test
    void showsErrorsForAnIncompleteForm() throws Exception {
        mockMvc.perform(post("/donors").param("age", "16").param("phone", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("donors/form"))
                .andExpect(model().attributeHasFieldErrors("donorForm",
                        "fullName", "age", "gender", "bloodGroup", "phone", "city"));

        assertThat(donorRepository.count()).isZero();
    }

    @Test
    void rejectsAMobileNumberThatIsAlreadyUsed() throws Exception {
        saveDonor("9876543210");

        mockMvc.perform(post("/donors")
                        .param("fullName", "Someone Else")
                        .param("age", "30")
                        .param("gender", "MALE")
                        .param("bloodGroup", "O_POSITIVE")
                        .param("phone", "9876543210")
                        .param("city", "Delhi"))
                .andExpect(view().name("donors/form"))
                .andExpect(model().attributeHasFieldErrorCode("donorForm", "phone", "duplicate"));
    }

    @Test
    void unknownDonorShowsTheNotFoundPage() throws Exception {
        mockMvc.perform(get("/donors/999999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));

        mockMvc.perform(get("/donors/abc"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void marksADonorAsNotAvailable() throws Exception {
        Donor donor = saveDonor("9876543211");

        mockMvc.perform(post("/donors/{id}/availability", donor.getId()).param("available", "false"))
                .andExpect(redirectedUrl("/donors/" + donor.getId()));

        assertThat(donorRepository.findById(donor.getId())).get()
                .extracting(Donor::isAvailable).isEqualTo(false);
    }

    @Test
    void deletesADonor() throws Exception {
        Donor donor = saveDonor("9876543212");

        mockMvc.perform(post("/donors/{id}/delete", donor.getId()))
                .andExpect(redirectedUrl("/donors"));

        assertThat(donorRepository.existsById(donor.getId())).isFalse();
    }

    @Test
    void donorsListShowsTenDonorsPerPage() throws Exception {
        for (int i = 0; i < 12; i++) {
            saveDonor("98765432" + (20 + i));
        }

        mockMvc.perform(get("/donors"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("donors", hasSize(10)))
                .andExpect(model().attribute("donorPage", hasProperty("totalPages", is(2))));

        mockMvc.perform(get("/donors").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("donors", hasSize(2)))
                .andExpect(model().attribute("donorPage", hasProperty("number", is(1))));
    }

    @Test
    void textPageNumberShowsTheNotFoundPage() throws Exception {
        mockMvc.perform(get("/donors").param("page", "abc"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void searchListsSuitableDonors() throws Exception {
        saveDonor("9876543213");

        mockMvc.perform(get("/search").param("bloodGroup", "AB_POSITIVE"))
                .andExpect(status().isOk())
                .andExpect(view().name("search/search"))
                .andExpect(model().attribute("results", hasSize(1)));
    }

    @Test
    void searchIgnoresAnUnknownBloodGroup() throws Exception {
        mockMvc.perform(get("/search").param("bloodGroup", "XYZ"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("results"));
    }

    private Donor saveDonor(String phone) {
        Donor donor = new Donor();
        donor.setFullName("Test Donor");
        donor.setAge(30);
        donor.setGender(Gender.MALE);
        donor.setBloodGroup(BloodGroup.O_POSITIVE);
        donor.setPhone(phone);
        donor.setCity("Mumbai");
        return donorRepository.save(donor);
    }
}
