package com.group2.blooddonation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Turns errors thrown by any controller into friendly pages.
 * Other errors fall back to error/404.jsp and error/500.jsp.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** Unknown donor id, or an id that is not a number (e.g. /donors/abc). */
    @ExceptionHandler({DonorNotFoundException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() {
        return "error/404";
    }
}
