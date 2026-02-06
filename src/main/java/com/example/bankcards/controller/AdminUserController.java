package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.enums.UserRole;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long userId,
                                                  @RequestParam UserRole role) {
        return ResponseEntity.ok(userService.changeUserRole(userId, role));
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<UserDto> blockUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.blockUser(userId));
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<UserDto> unblockUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.unblockUser(userId));
    }
}
