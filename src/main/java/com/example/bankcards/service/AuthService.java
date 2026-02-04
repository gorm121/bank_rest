package com.example.bankcards.service;


import com.example.bankcards.config.JwtTokenProvider;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegisterRequest;
import com.example.bankcards.dto.response.LoginResponse;
import com.example.bankcards.dto.response.RegisterResponse;
import com.example.bankcards.dto.response.UserDetailResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();
        String password = request.getPassword();

        if (userRepository.existsByUsernameOrEmail(username, email)) {
            log.warn("Registration attempt with existing username or email: username={}, email={}", username, email);
//            throw new UserAlreadyExistsException("Username or Email already exists");
            throw new RuntimeException("Username or Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
        log.info("User registered successfully: userId={}, username={}, email={}", user.getId(), username, email);
        return RegisterResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login attempt with non-existent email: {}", email);
                    return new UsernameNotFoundException("User not found");
                });
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Failed login attempt: invalid password for email={}", email);
            throw new BadCredentialsException("Invalid email or password");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email, password
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateJwtToken(authentication);
        log.info("User logged in successfully: userId={}, email={}", user.getId(), email);

        return LoginResponse.builder()
                .accessToken(token)
                .user(new UserDetailResponse(user))
                .build();
    }
}

