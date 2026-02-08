package com.example.bankcards.dto;

import com.example.bankcards.enums.UserRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    String username;
    String email;
    String cardHolder;
    UserRole role;
}
