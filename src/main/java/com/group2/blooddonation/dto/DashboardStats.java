package com.group2.blooddonation.dto;

import com.group2.blooddonation.model.BloodGroup;
import java.util.Map;

/**
 * Numbers shown on the home page.
 *
 * @param totalDonors         every registered donor
 * @param availableDonors     donors who said they are willing to donate
 * @param readyDonors         available donors who are also allowed to donate today
 * @param donorsPerBloodGroup how many donors each blood group has (all 8 groups, in order)
 */
public record DashboardStats(
        long totalDonors,
        long availableDonors,
        long readyDonors,
        Map<BloodGroup, Long> donorsPerBloodGroup) {
}
