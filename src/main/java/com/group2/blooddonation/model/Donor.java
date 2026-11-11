package com.group2.blooddonation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A blood donor. Each object is one row of the {@code donors} table.
 */
@Entity
@Table(name = "donors")
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Convert(converter = BloodGroupConverter.class)
    @Column(name = "blood_group", nullable = false, length = 3)
    private BloodGroup bloodGroup;

    @Column(nullable = false, length = 10, unique = true)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(nullable = false, length = 60)
    private String city;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(nullable = false)
    private boolean available = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ----- Donation rules (worked out from the fields, not stored) -----

    /** First date the donor may give blood again, or null if they have never donated. */
    public LocalDate getNextEligibleDate() {
        if (lastDonationDate == null) {
            return null;
        }
        return lastDonationDate.plusDays(gender.getDonationGapDays());
    }

    /** True when enough days have passed since the last donation. */
    public boolean isEligibleToDonate() {
        LocalDate nextEligibleDate = getNextEligibleDate();
        return nextEligibleDate == null || !LocalDate.now().isBefore(nextEligibleDate);
    }

    /** True when the donor is willing to donate and is allowed to donate today. */
    public boolean isReadyToDonate() {
        return available && isEligibleToDonate();
    }

    // ----- Getters and setters -----

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
