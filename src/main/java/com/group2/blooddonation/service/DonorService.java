package com.group2.blooddonation.service;

import com.group2.blooddonation.dto.DashboardStats;
import com.group2.blooddonation.dto.DonorForm;
import com.group2.blooddonation.dto.DonorSearchCriteria;
import com.group2.blooddonation.exception.DonorNotFoundException;
import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.model.Donor;
import com.group2.blooddonation.repository.DonorRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for donors: registering, updating, searching and the home page numbers.
 * Controllers call this class; this class calls the repository.
 */
@Service
@Transactional(readOnly = true)
public class DonorService {

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    // ----- Reading -----

    /** All donors sorted by name, or only one blood group when {@code bloodGroup} is given. */
    public List<Donor> findAll(BloodGroup bloodGroup) {
        if (bloodGroup == null) {
            return donorRepository.findAllByOrderByFullNameAsc();
        }
        return donorRepository.findByBloodGroupOrderByFullNameAsc(bloodGroup);
    }

    public List<Donor> findRecentlyRegistered() {
        return donorRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public Donor getById(Long id) {
        return donorRepository.findById(id).orElseThrow(() -> new DonorNotFoundException(id));
    }

    /** True if another donor already uses this mobile number. */
    public boolean isPhoneTaken(String phone, Long excludeDonorId) {
        if (excludeDonorId == null) {
            return donorRepository.existsByPhone(phone);
        }
        return donorRepository.existsByPhoneAndIdNot(phone, excludeDonorId);
    }

    /**
     * Donors who can give blood to a patient, best matches first:
     * same blood group before compatible groups, then donors ready today, then by name.
     */
    public List<Donor> findSuitableDonors(DonorSearchCriteria criteria) {
        BloodGroup patientGroup = criteria.getBloodGroup();
        Set<BloodGroup> donorGroups = criteria.isIncludeCompatible()
                ? patientGroup.getCompatibleDonors()
                : EnumSet.of(patientGroup);
        String city = criteria.getCity() == null ? null : criteria.getCity().toLowerCase(Locale.ROOT);

        Comparator<Donor> bestMatchFirst = Comparator
                .comparing((Donor donor) -> donor.getBloodGroup() != patientGroup)
                .thenComparing(donor -> !donor.isReadyToDonate())
                .thenComparing(Donor::getFullName, String.CASE_INSENSITIVE_ORDER);

        return donorRepository.findByBloodGroupIn(donorGroups).stream()
                .filter(donor -> city == null || donor.getCity().toLowerCase(Locale.ROOT).contains(city))
                .filter(donor -> !criteria.isOnlyReady() || donor.isReadyToDonate())
                .sorted(bestMatchFirst)
                .toList();
    }

    /** Totals for the home page, including a count for every blood group. */
    public DashboardStats getDashboardStats() {
        List<Donor> donors = donorRepository.findAll();

        Map<BloodGroup, Long> donorsPerBloodGroup = new EnumMap<>(BloodGroup.class);
        for (BloodGroup group : BloodGroup.values()) {
            donorsPerBloodGroup.put(group, 0L);
        }
        for (Donor donor : donors) {
            donorsPerBloodGroup.merge(donor.getBloodGroup(), 1L, Long::sum);
        }

        long availableDonors = donors.stream().filter(Donor::isAvailable).count();
        long readyDonors = donors.stream().filter(Donor::isReadyToDonate).count();
        return new DashboardStats(donors.size(), availableDonors, readyDonors, donorsPerBloodGroup);
    }

    // ----- Changing -----

    @Transactional
    public Donor register(DonorForm form) {
        Donor donor = new Donor();
        form.applyTo(donor);
        return donorRepository.save(donor);
    }

    @Transactional
    public Donor update(Long id, DonorForm form) {
        Donor donor = getById(id);
        form.applyTo(donor);
        return donor;
    }

    @Transactional
    public Donor changeAvailability(Long id, boolean available) {
        Donor donor = getById(id);
        donor.setAvailable(available);
        return donor;
    }

    @Transactional
    public Donor recordDonation(Long id, LocalDate donationDate) {
        Donor donor = getById(id);
        donor.setLastDonationDate(donationDate);
        return donor;
    }

    @Transactional
    public Donor delete(Long id) {
        Donor donor = getById(id);
        donorRepository.delete(donor);
        return donor;
    }
}
