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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for donors: registering, updating, searching and the home page numbers.
 * Controllers call this class; this class calls the repository.
 */
@Service
@Transactional(readOnly = true)
public class DonorService {

    /** How many donors each page of the donors list shows. */
    public static final int PAGE_SIZE = 10;

    /** Alphabetical by name, ignoring capital letters. The id breaks ties so pages never overlap. */
    private static final Sort BY_NAME = Sort.by(Sort.Order.asc("fullName").ignoreCase(), Sort.Order.asc("id"));

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    // ----- Reading -----

    /**
     * One page of the donors list, sorted by name. Page numbers start at 1, as shown on screen.
     * Only one blood group is listed when {@code bloodGroup} is given.
     * A number below 1 shows the first page, and a number past the end shows the last page.
     */
    public Page<Donor> findPage(BloodGroup bloodGroup, int pageNumber) {
        // Spring Data counts pages from 0. The upper limit keeps the row offset within an int.
        int pageIndex = Math.min(Math.max(pageNumber, 1), Integer.MAX_VALUE / PAGE_SIZE) - 1;
        Page<Donor> page = fetchPage(bloodGroup, pageIndex);
        if (page.getTotalPages() > 0 && page.getNumber() >= page.getTotalPages()) {
            page = fetchPage(bloodGroup, page.getTotalPages() - 1);
        }
        return page;
    }

    private Page<Donor> fetchPage(BloodGroup bloodGroup, int pageIndex) {
        Pageable pageable = PageRequest.of(pageIndex, PAGE_SIZE, BY_NAME);
        if (bloodGroup == null) {
            return donorRepository.findAll(pageable);
        }
        return donorRepository.findByBloodGroup(bloodGroup, pageable);
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
