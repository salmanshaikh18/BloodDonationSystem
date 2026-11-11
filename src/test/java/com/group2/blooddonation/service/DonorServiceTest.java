package com.group2.blooddonation.service;

import static com.group2.blooddonation.model.BloodGroup.AB_NEGATIVE;
import static com.group2.blooddonation.model.BloodGroup.A_NEGATIVE;
import static com.group2.blooddonation.model.BloodGroup.A_POSITIVE;
import static com.group2.blooddonation.model.BloodGroup.B_POSITIVE;
import static com.group2.blooddonation.model.BloodGroup.O_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;

import com.group2.blooddonation.dto.DashboardStats;
import com.group2.blooddonation.dto.DonorSearchCriteria;
import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.model.Donor;
import com.group2.blooddonation.model.Gender;
import com.group2.blooddonation.repository.DonorRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs the real service against an in-memory database. Every test starts with the
 * same five donors and its changes are rolled back afterwards.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DonorServiceTest {

    @Autowired
    private DonorService donorService;

    @Autowired
    private DonorRepository donorRepository;

    @BeforeEach
    void addDonors() {
        save("Asha", Gender.FEMALE, A_POSITIVE, "9000000001", "Mumbai", null, true);    // same group, ready
        save("Bala", Gender.MALE, O_NEGATIVE, "9000000002", "Pune", 200, true);         // compatible, ready
        save("Chetan", Gender.MALE, A_POSITIVE, "9000000003", "Mumbai", 30, true);      // same group, donated recently
        save("Divya", Gender.FEMALE, A_NEGATIVE, "9000000004", "Mumbai", null, false);  // compatible, not available
        save("Esha", Gender.FEMALE, B_POSITIVE, "9000000005", "Mumbai", null, true);    // not compatible with A+
    }

    @Test
    void findsReadyCompatibleDonorsWithSameGroupFirst() {
        assertThat(namesOf(donorService.findSuitableDonors(criteria(A_POSITIVE, true, true, null))))
                .containsExactly("Asha", "Bala");
    }

    @Test
    void listsDonorsWhoCantDonateTodayWhenAsked() {
        assertThat(namesOf(donorService.findSuitableDonors(criteria(A_POSITIVE, true, false, null))))
                .containsExactly("Asha", "Chetan", "Bala", "Divya");
    }

    @Test
    void showsOnlyTheSameGroupWhenCompatibleIsOff() {
        assertThat(namesOf(donorService.findSuitableDonors(criteria(A_POSITIVE, false, true, null))))
                .containsExactly("Asha");
    }

    @Test
    void filtersByCityIgnoringCase() {
        assertThat(namesOf(donorService.findSuitableDonors(criteria(A_POSITIVE, true, true, "mumbai"))))
                .containsExactly("Asha");
    }

    @Test
    void womenWait120DaysAndMen90DaysBetweenDonations() {
        Donor woman = save("Farah", Gender.FEMALE, AB_NEGATIVE, "9000000006", "Delhi", 100, true);
        Donor man = save("Gopal", Gender.MALE, AB_NEGATIVE, "9000000007", "Delhi", 100, true);

        assertThat(woman.isEligibleToDonate()).isFalse();
        assertThat(woman.getNextEligibleDate()).isEqualTo(LocalDate.now().plusDays(20));
        assertThat(man.isEligibleToDonate()).isTrue();
    }

    @Test
    void dashboardCountsEveryBloodGroup() {
        DashboardStats stats = donorService.getDashboardStats();

        assertThat(stats.totalDonors()).isEqualTo(5);
        assertThat(stats.availableDonors()).isEqualTo(4);
        assertThat(stats.readyDonors()).isEqualTo(3);
        assertThat(stats.donorsPerBloodGroup()).hasSize(8);
        assertThat(stats.donorsPerBloodGroup().get(A_POSITIVE)).isEqualTo(2);
        assertThat(stats.donorsPerBloodGroup().get(AB_NEGATIVE)).isZero();
    }

    @Test
    void storesBloodGroupAsItsLabel() {
        assertThat(donorRepository.findByBloodGroup(O_NEGATIVE, Pageable.unpaged()).getContent())
                .extracting(Donor::getFullName)
                .containsExactly("Bala");
    }

    // ----- Donors list pages -----

    @Test
    void listsTenDonorsPerPageSortedByNameIgnoringCapitals() {
        save("aaron", Gender.MALE, O_NEGATIVE, "9100000000", "Pune", null, true);
        for (int i = 1; i <= 11; i++) {
            save(String.format("Zed %02d", i), Gender.MALE, B_POSITIVE, String.format("91000000%02d", i), "Pune", null, true);
        }

        Page<Donor> firstPage = donorService.findPage(null, 1);
        assertThat(firstPage.getTotalElements()).isEqualTo(17);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(namesOf(firstPage.getContent()))
                .hasSize(10)
                .startsWith("aaron", "Asha", "Bala", "Chetan", "Divya", "Esha", "Zed 01");

        Page<Donor> secondPage = donorService.findPage(null, 2);
        assertThat(namesOf(secondPage.getContent())).hasSize(7).endsWith("Zed 11");
    }

    @Test
    void pagesCanBeFilteredByBloodGroup() {
        Page<Donor> page = donorService.findPage(A_POSITIVE, 1);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(namesOf(page.getContent())).containsExactly("Asha", "Chetan");
    }

    @Test
    void outOfRangePageNumbersShowTheNearestPage() {
        assertThat(donorService.findPage(null, 0).getNumber()).isZero();
        assertThat(donorService.findPage(null, -5).getNumber()).isZero();
        assertThat(donorService.findPage(null, 99).getContent()).hasSize(5);
        assertThat(donorService.findPage(null, Integer.MAX_VALUE).getContent()).hasSize(5);
    }

    // ----- Helpers -----

    private Donor save(String name, Gender gender, BloodGroup group, String phone, String city,
                       Integer daysSinceDonation, boolean available) {
        Donor donor = new Donor();
        donor.setFullName(name);
        donor.setAge(30);
        donor.setGender(gender);
        donor.setBloodGroup(group);
        donor.setPhone(phone);
        donor.setCity(city);
        donor.setLastDonationDate(daysSinceDonation == null ? null : LocalDate.now().minusDays(daysSinceDonation));
        donor.setAvailable(available);
        return donorRepository.save(donor);
    }

    private static DonorSearchCriteria criteria(BloodGroup group, boolean includeCompatible,
                                                boolean onlyReady, String city) {
        DonorSearchCriteria criteria = new DonorSearchCriteria();
        criteria.setBloodGroup(group);
        criteria.setIncludeCompatible(includeCompatible);
        criteria.setOnlyReady(onlyReady);
        criteria.setCity(city);
        return criteria;
    }

    private static List<String> namesOf(List<Donor> donors) {
        return donors.stream().map(Donor::getFullName).toList();
    }
}
