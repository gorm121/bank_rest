package com.example.bankcards.dto.response;


import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PublicKeyResponse {
    private String publicKey;
}
