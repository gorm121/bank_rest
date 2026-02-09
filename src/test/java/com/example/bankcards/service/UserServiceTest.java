package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.UserRole;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        user2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
    }

    @Test
    void getAllUsers_Success() {
        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        Page<UserDto> result = userService.getAllUsers(any(Pageable.class));

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        assertEquals("user1", result.getContent().getFirst().getUsername());
        assertEquals(UserRole.USER, result.getContent().getFirst().getRole());

        assertEquals("user2", result.getContent().getFirst().getUsername());
        assertEquals(UserRole.ADMIN, result.getContent().getFirst().getRole());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUser_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        UserDto result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals("user1", result.getUsername());
        assertEquals("user1@example.com", result.getEmail());
        assertEquals(UserRole.USER, result.getRole());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUser_NotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.getUser(999L);
        });

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void changeUserRole_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        UserDto result = userService.changeUserRole(1L, UserRole.ADMIN);

        assertNotNull(result);
        assertEquals(UserRole.ADMIN, user1.getRole());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void blockUser_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        UserDto result = userService.blockUser(1L);

        assertNotNull(result);
        assertFalse(user1.getEnabled());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void unblockUser_Success() {
        user1.setEnabled(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        UserDto result = userService.unblockUser(1L);

        assertNotNull(result);
        assertTrue(user1.getEnabled());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }
}