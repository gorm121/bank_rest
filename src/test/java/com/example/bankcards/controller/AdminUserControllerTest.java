package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.enums.UserRole;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllUsers_Success() throws Exception {
        UserDto user1 = UserDto.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .role(UserRole.USER)
                .build();

        UserDto user2 = UserDto.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .role(UserRole.ADMIN)
                .build();

        List<UserDto> content = Arrays.asList(user1, user2);

        Page<UserDto> usersPage = new PageImpl<>(content, PageRequest.of(0, 10), content.size());

        Pageable pageable = PageRequest.of(0, 10);

        when(userService.getAllUsers(pageable)).thenReturn(usersPage);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].username").value("user1"))
                .andExpect(jsonPath("$.content[0].role").value("USER"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].username").value("user2"))
                .andExpect(jsonPath("$.content[1].role").value("ADMIN"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getUser_Success() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        when(userService.getUser(eq(1L))).thenReturn(user);
        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void changeUserRole_Success() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.ADMIN)
                .build();

        when(userService.changeUserRole(eq(1L), eq(UserRole.ADMIN))).thenReturn(user);

        mockMvc.perform(put("/api/admin/users/1/role")
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void blockUser_Success() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        when(userService.blockUser(eq(1L))).thenReturn(user);

        mockMvc.perform(post("/api/admin/users/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void unblockUser_Success() throws Exception {
        UserDto user = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        when(userService.unblockUser(eq(1L))).thenReturn(user);

        mockMvc.perform(post("/api/admin/users/1/unblock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void changeUserRole_InvalidRole_BadRequest() throws Exception {
        mockMvc.perform(put("/api/admin/users/1/role")
                        .param("role", "INVALID_ROLE"))
                .andExpect(status().isBadRequest());
    }
}
