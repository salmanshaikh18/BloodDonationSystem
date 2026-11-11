package com.group2.blooddonation.model;

/**
 * Donor gender, with the minimum gap between two whole-blood donations.
 *
 * <p>India's national guidelines (NBTC) ask for at least 90 days for men and
 * 120 days for women. We use the longer gap for "Other" to stay on the safe side.
 */
public enum Gender {

    MALE("Male", 90),
    FEMALE("Female", 120),
    OTHER("Other", 120);

    private final String label;
    private final int donationGapDays;

    Gender(String label, int donationGapDays) {
        this.label = label;
        this.donationGapDays = donationGapDays;
    }

    public String getLabel() {
        return label;
    }

    public int getDonationGapDays() {
        return donationGapDays;
    }
}
