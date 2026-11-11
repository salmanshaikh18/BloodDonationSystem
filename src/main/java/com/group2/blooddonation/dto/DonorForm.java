package com.group2.blooddonation.dto;

import com.group2.blooddonation.model.BloodGroup;
import com.group2.blooddonation.model.Donor;
import com.group2.blooddonation.model.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * The fields of the Register and Edit donor forms, with their validation rules.
 *
 * <p>Kept separate from the {@link Donor} entity so a submitted form can only
 * change these fields, never the id or timestamps.
 */
public class DonorForm {

    @NotBlank(message = "Enter the donor's full name")
    @Size(max = 100, message = "Name can be at most 100 characters")
    private String fullName;

    @NotNull(message = "Enter the age")
    @Min(value = 18, message = "Donors must be at least 18 years old")
    @Max(value = 65, message = "Donors must be 65 years old or younger")
    private Integer age;

    @NotNull(message = "Select a gender")
    private Gender gender;

    @NotNull(message = "Select a blood group")
    private BloodGroup bloodGroup;

    @NotBlank(message = "Enter a mobile number")
    @Pattern(regexp = "[6-9]\\d{9}", message = "Enter a valid 10-digit mobile number")
    private String phone;

    @Email(message = "Enter a valid email address")
    @Size(max = 120, message = "Email can be at most 120 characters")
    private String email;

    @NotBlank(message = "Enter the city")
    @Size(max = 60, message = "City can be at most 60 characters")
    private String city;

    @PastOrPresent(message = "The last donation date can't be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lastDonationDate;

    private boolean available = true;

    /** Fills a form with a donor's current details, for the Edit page. */
    public static DonorForm from(Donor donor) {
        DonorForm form = new DonorForm();
        form.setFullName(donor.getFullName());
        form.setAge(donor.getAge());
        form.setGender(donor.getGender());
        form.setBloodGroup(donor.getBloodGroup());
        form.setPhone(donor.getPhone());
        form.setEmail(donor.getEmail());
        form.setCity(donor.getCity());
        form.setLastDonationDate(donor.getLastDonationDate());
        form.setAvailable(donor.isAvailable());
        return form;
    }

    /** Copies the form's values onto a donor before saving. */
    public void applyTo(Donor donor) {
        donor.setFullName(fullName);
        donor.setAge(age);
        donor.setGender(gender);
        donor.setBloodGroup(bloodGroup);
        donor.setPhone(phone);
        donor.setEmail(email);
        donor.setCity(city);
        donor.setLastDonationDate(lastDonationDate);
        donor.setAvailable(available);
    }

    /** Trims spaces and turns an empty value into null. */
    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // ----- Getters and setters (setters tidy up what the user typed) -----

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = clean(fullName);
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
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

    /** Accepts "98765 43210", "98765-43210" or "+91 98765 43210" and keeps the 10 digits. */
    public void setPhone(String phone) {
        String value = clean(phone);
        if (value != null) {
            value = value.replaceAll("[\\s-]", "");
            if (value.startsWith("+91")) {
                value = value.substring(3);
            }
        }
        this.phone = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String value = clean(email);
        this.email = value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = clean(city);
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
}
