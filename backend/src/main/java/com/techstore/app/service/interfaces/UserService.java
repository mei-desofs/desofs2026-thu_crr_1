package com.techstore.app.service.interfaces;

import com.techstore.app.domain.user.User;

public interface UserService {

    /**
     * Registers a new user with the provided Supabase user ID, email, and role.
     * @param supabaseUserId The unique identifier from Supabase for the user.
     * @param email The email address of the user.
     * @param role The role assigned to the user (e.g., "customer", "manager", "carrier").
     * @return The registered User object.
     */
    User registerUser(String supabaseUserId, String email, String role);
}
