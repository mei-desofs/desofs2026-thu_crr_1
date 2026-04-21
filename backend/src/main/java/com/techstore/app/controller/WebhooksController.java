package com.techstore.app.controller;

import com.techstore.app.dto.auth.InviteSignupRequest;
import com.techstore.app.service.interfaces.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class WebhooksController {

    private final AuthService authService;

    public WebhooksController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth")
    public ResponseEntity<Void> handleAuthWebhook(
            @RequestHeader("x-webhook-secret") String secret,
            @RequestBody  Map<String, Object> payload) {
        if(!authService.confirmInvite(secret, payload)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok().build();
    }
}


