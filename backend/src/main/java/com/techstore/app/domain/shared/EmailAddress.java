package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode
@Getter
public class EmailAddress {

    @NotBlank
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    protected EmailAddress() {}

    public EmailAddress(String email) {
        if (!isValid(email)) {
            throw new BusinessException("Invalid email address");
        }
        this.email = email;
    }

    private boolean isValid(String email) {
        return email != null && email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );
    }
}