package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    private String tokenType = "Bearer";
    private UserDetailResponse user;
}
