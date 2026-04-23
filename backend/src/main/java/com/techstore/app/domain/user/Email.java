package com.techstore.app.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Value object representing a user's email address with validation logic to ensure it follows a standard email format.
 * The email must contain an '@' symbol and a valid domain.
 */
@Getter
@NoArgsConstructor
@Embeddable
public class Email {

    private static final String REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Column(name = "email", length = 180, nullable = false, unique = true)
    private String email;

    public Email(String email) {
        if(!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format. Email must contain an '@' symbol and a valid domain.");
        }
        this.email = email;
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(REGEX);
    }
}
