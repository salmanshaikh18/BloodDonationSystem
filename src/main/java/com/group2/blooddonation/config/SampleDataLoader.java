package com.group2.blooddonation.config;

import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.model.Donor;
import com.group2.blooddonation.model.Gender;
import com.group2.blooddonation.repository.DonorRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

/**
 * Adds made-up donors when the app starts with an empty database, so every page
 * has something to show. On by default only on your laptop
 * ({@code app.sample-data.enabled} in application-local.properties).
 */
@Component
@ConditionalOnBooleanProperty("app.sample-data.enabled")
public class SampleDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleDataLoader.class);

    private final DonorRepository donorRepository;

    public SampleDataLoader(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (donorRepository.count() > 0) {
            return;
        }
        List<Donor> donors = List.of(
                donor("Aarav Sharma", 28, Gender.MALE, BloodGroup.O_POSITIVE, "9876500001", "Mumbai", 200, true),
                donor("Priya Patel", 24, Gender.FEMALE, BloodGroup.A_POSITIVE, "9876500002", "Pune", 150, true),
                donor("Rohan Mehta", 35, Gender.MALE, BloodGroup.B_POSITIVE, "9876500003", "Mumbai", 30, true),
                donor("Sneha Iyer", 31, Gender.FEMALE, BloodGroup.AB_POSITIVE, "9876500004", "Chennai", null, true),
                donor("Vikram Singh", 42, Gender.MALE, BloodGroup.O_NEGATIVE, "9876500005", "Delhi", 120, true),
                donor("Ananya Gupta", 22, Gender.FEMALE, BloodGroup.A_NEGATIVE, "9876500006", "Mumbai", 60, true),
                donor("Karan Joshi", 29, Gender.MALE, BloodGroup.B_NEGATIVE, "9876500007", "Pune", 100, false),
                donor("Fatima Khan", 27, Gender.FEMALE, BloodGroup.O_POSITIVE, "9876500008", "Hyderabad", 400, true),
                donor("Arjun Nair", 38, Gender.MALE, BloodGroup.AB_NEGATIVE, "9876500009", "Bengaluru", 95, true),
                donor("Meera Reddy", 33, Gender.FEMALE, BloodGroup.B_POSITIVE, "9876500010", "Hyderabad", null, true),
                donor("Rahul Verma", 45, Gender.MALE, BloodGroup.A_POSITIVE, "9876500011", "Delhi", 10, true),
                donor("Neha Kulkarni", 26, Gender.FEMALE, BloodGroup.O_NEGATIVE, "9876500012", "Mumbai", 130, true));
        donorRepository.saveAll(donors);
        log.info("Added {} sample donors", donors.size());
    }

    /** {@code daysSinceDonation} null means the donor has never donated. */
    private static Donor donor(String name, int age, Gender gender, BloodGroup bloodGroup, String phone,
                               String city, Integer daysSinceDonation, boolean available) {
        Donor donor = new Donor();
        donor.setFullName(name);
        donor.setAge(age);
        donor.setGender(gender);
        donor.setBloodGroup(bloodGroup);
        donor.setPhone(phone);
        donor.setCity(city);
        donor.setLastDonationDate(daysSinceDonation == null ? null : LocalDate.now().minusDays(daysSinceDonation));
        donor.setAvailable(available);
        return donor;
    }
}
