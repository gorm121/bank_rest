package com.example.bankcards.controller;


import com.example.bankcards.service.RsaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private RsaService rsaService;

    @InjectMocks
    private SecurityController securityController;

    @Test
    void getPublicKey_Success() throws Exception {
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...";
        when(rsaService.getPublicKeyBase64()).thenReturn(publicKey);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(securityController).build();

        mockMvc.perform(get("/api/security/public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicKey").value(publicKey));
    }

    @Test
    void getPublicKey_EmptyKey_ReturnsKey() throws Exception {
        when(rsaService.getPublicKeyBase64()).thenReturn("");

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(securityController).build();

        mockMvc.perform(get("/api/security/public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicKey").value(""));
    }
}