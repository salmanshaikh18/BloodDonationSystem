package com.group2.blooddonation.repository;

import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.model.Donor;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for donors. Spring Data JPA writes the SQL for each method
 * from its name, for example {@code findByBloodGroupIn} becomes
 * {@code SELECT ... FROM donors WHERE blood_group IN (...)}.
 */
public interface DonorRepository extends JpaRepository<Donor, Long> {

    List<Donor> findAllByOrderByFullNameAsc();

    List<Donor> findByBloodGroupOrderByFullNameAsc(BloodGroup bloodGroup);

    List<Donor> findByBloodGroupIn(Collection<BloodGroup> bloodGroups);

    List<Donor> findTop5ByOrderByCreatedAtDesc();

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
