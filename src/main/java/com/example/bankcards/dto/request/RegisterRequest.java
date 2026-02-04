package com.example.bankcards.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank
    @NotNull
    @Size(min = 3, max = 20, message = "Username должен быть от 3 до 20 символов")
    private String username;

    @NotBlank(message = "Email не должен быть пустым")
    @NotNull
    @Email(message = "Некорректный формат Email")
    private String email;

    @NotBlank
    @NotNull
    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    private String password;
}
