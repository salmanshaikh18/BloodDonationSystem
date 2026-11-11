package com.group2.blooddonation.repository;

import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.model.Donor;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for donors. Spring Data JPA writes the SQL for each method
 * from its name, for example {@code findByBloodGroupIn} becomes
 * {@code SELECT ... FROM donors WHERE blood_group IN (...)}.
 *
 * <p>{@code findAll(Pageable)} for the unfiltered donors list comes from {@link JpaRepository}.
 */
public interface DonorRepository extends JpaRepository<Donor, Long> {

    /** One page of donors with this blood group. The page number, size and order come from {@code pageable}. */
    Page<Donor> findByBloodGroup(BloodGroup bloodGroup, Pageable pageable);

    List<Donor> findByBloodGroupIn(Collection<BloodGroup> bloodGroups);

    List<Donor> findTop5ByOrderByCreatedAtDesc();

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
