package com.techstore.app.controller;

import com.techstore.app.service.interfaces.BackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backup")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/products")
    public ResponseEntity<String> backupProducts(@RequestParam String command) {
        return ResponseEntity.ok(backupService.execute(command));
    }
}
