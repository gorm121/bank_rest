package com.example.bankcards.controller;

import com.example.bankcards.dto.response.PublicKeyResponse;
import com.example.bankcards.service.RsaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {

    private final RsaService rsaService;

    @GetMapping("/public-key")
    public ResponseEntity<PublicKeyResponse> getPublicKey() {
        String publicKey = rsaService.getPublicKeyBase64();
        return ResponseEntity.ok(new PublicKeyResponse(publicKey));
    }
}
