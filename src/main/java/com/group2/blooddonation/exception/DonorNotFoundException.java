package com.group2.blooddonation.exception;

/**
 * Thrown when no donor exists with the requested id. Shown to the user as a 404 page.
 */
public class DonorNotFoundException extends RuntimeException {

    public DonorNotFoundException(Long id) {
        super("No donor found with id " + id);
    }
}
