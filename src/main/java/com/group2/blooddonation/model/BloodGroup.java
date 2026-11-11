package com.group2.blooddonation.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The eight ABO/Rh blood groups and the red-cell compatibility rules between them.
 */
public enum BloodGroup {

    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    /** Patient's blood group -> donor blood groups the patient can safely receive. */
    private static final Map<BloodGroup, Set<BloodGroup>> CAN_RECEIVE_FROM = new EnumMap<>(BloodGroup.class);

    static {
        CAN_RECEIVE_FROM.put(O_NEGATIVE, EnumSet.of(O_NEGATIVE));
        CAN_RECEIVE_FROM.put(O_POSITIVE, EnumSet.of(O_NEGATIVE, O_POSITIVE));
        CAN_RECEIVE_FROM.put(A_NEGATIVE, EnumSet.of(O_NEGATIVE, A_NEGATIVE));
        CAN_RECEIVE_FROM.put(A_POSITIVE, EnumSet.of(O_NEGATIVE, O_POSITIVE, A_NEGATIVE, A_POSITIVE));
        CAN_RECEIVE_FROM.put(B_NEGATIVE, EnumSet.of(O_NEGATIVE, B_NEGATIVE));
        CAN_RECEIVE_FROM.put(B_POSITIVE, EnumSet.of(O_NEGATIVE, O_POSITIVE, B_NEGATIVE, B_POSITIVE));
        CAN_RECEIVE_FROM.put(AB_NEGATIVE, EnumSet.of(O_NEGATIVE, A_NEGATIVE, B_NEGATIVE, AB_NEGATIVE));
        CAN_RECEIVE_FROM.put(AB_POSITIVE, EnumSet.allOf(BloodGroup.class));
    }

    private final String label;

    BloodGroup(String label) {
        this.label = label;
    }

    /** The usual way to write the group, for example "AB+". */
    public String getLabel() {
        return label;
    }

    /** Donor groups a patient with this blood group can receive from. */
    public Set<BloodGroup> getCompatibleDonors() {
        return Collections.unmodifiableSet(CAN_RECEIVE_FROM.get(this));
    }

    /** Patient groups a donor with this blood group can give to. */
    public Set<BloodGroup> getCanDonateTo() {
        Set<BloodGroup> patients = EnumSet.noneOf(BloodGroup.class);
        CAN_RECEIVE_FROM.forEach((patient, donors) -> {
            if (donors.contains(this)) {
                patients.add(patient);
            }
        });
        return Collections.unmodifiableSet(patients);
    }

    /** Finds a group from its label, for example "O-" returns {@link #O_NEGATIVE}. */
    public static BloodGroup fromLabel(String label) {
        for (BloodGroup group : values()) {
            if (group.label.equals(label)) {
                return group;
            }
        }
        throw new IllegalArgumentException("Unknown blood group: " + label);
    }
}
