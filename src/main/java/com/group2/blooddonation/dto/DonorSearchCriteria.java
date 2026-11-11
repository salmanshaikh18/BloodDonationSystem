package com.group2.blooddonation.dto;

import com.group2.blooddonation.model.BloodGroup;

/**
 * Filters on the "Find donors" page.
 */
public class DonorSearchCriteria {

    /** The patient's blood group. No search runs until this is chosen. */
    private BloodGroup bloodGroup;

    /** Optional: only donors whose city contains this text. */
    private String city;

    /** Also list donors of other groups the patient can safely receive. */
    private boolean includeCompatible = true;

    /** Only donors who are available and allowed to donate today. */
    private boolean onlyReady = true;

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = (city == null || city.isBlank()) ? null : city.strip();
    }

    public boolean isIncludeCompatible() {
        return includeCompatible;
    }

    public void setIncludeCompatible(boolean includeCompatible) {
        this.includeCompatible = includeCompatible;
    }

    public boolean isOnlyReady() {
        return onlyReady;
    }

    public void setOnlyReady(boolean onlyReady) {
        this.onlyReady = onlyReady;
    }
}
