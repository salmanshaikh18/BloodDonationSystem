package com.group2.blooddonation.model;

import static com.group2.blooddonation.model.BloodGroup.AB_POSITIVE;
import static com.group2.blooddonation.model.BloodGroup.A_NEGATIVE;
import static com.group2.blooddonation.model.BloodGroup.A_POSITIVE;
import static com.group2.blooddonation.model.BloodGroup.O_NEGATIVE;
import static com.group2.blooddonation.model.BloodGroup.O_POSITIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BloodGroupTest {

    @Test
    void abPositivePatientsCanReceiveFromEveryGroup() {
        assertThat(AB_POSITIVE.getCompatibleDonors()).containsExactlyInAnyOrder(BloodGroup.values());
    }

    @Test
    void oNegativePatientsCanReceiveOnlyFromONegative() {
        assertThat(O_NEGATIVE.getCompatibleDonors()).containsExactly(O_NEGATIVE);
    }

    @Test
    void aPositivePatientsCanReceiveFromAAndOGroups() {
        assertThat(A_POSITIVE.getCompatibleDonors())
                .containsExactlyInAnyOrder(A_POSITIVE, A_NEGATIVE, O_POSITIVE, O_NEGATIVE);
    }

    @Test
    void oNegativeDonorsCanGiveToEveryone() {
        assertThat(O_NEGATIVE.getCanDonateTo()).containsExactlyInAnyOrder(BloodGroup.values());
    }

    @Test
    void abPositiveDonorsCanGiveOnlyToAbPositive() {
        assertThat(AB_POSITIVE.getCanDonateTo()).containsExactly(AB_POSITIVE);
    }

    @Test
    void labelsConvertBothWays() {
        for (BloodGroup group : BloodGroup.values()) {
            assertThat(BloodGroup.fromLabel(group.getLabel())).isEqualTo(group);
        }
        assertThatThrownBy(() -> BloodGroup.fromLabel("C+")).isInstanceOf(IllegalArgumentException.class);
    }
}
