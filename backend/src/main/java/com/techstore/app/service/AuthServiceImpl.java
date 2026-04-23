package com.techstore.app.service;

import com.techstore.app.client.SupabaseAuthClient;
import com.techstore.app.dto.auth.InviteSignupRequest;
import com.techstore.app.service.interfaces.AuthService;
import com.techstore.app.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${supabase.webhook-secret}")
    private String webhookSecret;

    private final UserService userService;

    private final SupabaseAuthClient supabaseAuthClient;

    public AuthServiceImpl(UserService userService, SupabaseAuthClient supabaseAuthClient) {
        this.userService = userService;
        this.supabaseAuthClient = supabaseAuthClient;
    }

    public void inviteUser(InviteSignupRequest inviteSignupRequest) {
        supabaseAuthClient.inviteUser(inviteSignupRequest.email(), inviteSignupRequest.role());
    }

    @Override
    public boolean confirmInvite(String secret, Map<String, Object> payload) {

        if (!webhookSecret.equals(secret)) {
            return false;
        }

        Map<String, Object> record = (Map<String, Object>) payload.get("record");
        Map<String, Object> oldRecord = (Map<String, Object>) payload.get("old_record");

        String emailConfirmedAt = (String) record.get("email_confirmed_at");
        String oldEmailConfirmedAt = (String) oldRecord.get("email_confirmed_at");

        if (emailConfirmedAt == null || oldEmailConfirmedAt != null) {
            return true;
        }

        String supabaseUserId = (String) record.get("id");
        String email = (String) record.get("email");
        Map<String, Object> metadata = (Map<String, Object>) record.get("raw_user_meta_data");
        String role = (String) metadata.get("role");

        if (role != null) {
            userService.registerUser(supabaseUserId, email, role);
        }

        return true;
    }
}
