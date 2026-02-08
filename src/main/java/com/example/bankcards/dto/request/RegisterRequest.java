package com.example.bankcards.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank
    @NotNull
    @Size(min = 3, max = 20, message = "Username должен быть от 3 до 20 символов")
    String username;

    @NotBlank(message = "Email не должен быть пустым")
    @NotNull
    @Email(message = "Некорректный формат Email")
    String email;

    @NotBlank
    @NotNull
    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    String password;
}
