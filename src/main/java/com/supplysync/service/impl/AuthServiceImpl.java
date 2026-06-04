package com.supplysync.service.impl;

import com.supplysync.dto.request.LoginRequest;
import com.supplysync.dto.request.RegisterRequest;
import com.supplysync.dto.request.TokenRefreshRequest;
import com.supplysync.dto.response.LoginResponse;
import com.supplysync.dto.response.RegisterResponse;
import com.supplysync.dto.response.TokenRefreshResponse;
import com.supplysync.entity.User;
import com.supplysync.exception.DuplicateResourceException;
import com.supplysync.exception.InvalidOperationException;
import com.supplysync.exception.ResourceNotFoundException;
import com.supplysync.exception.TooManyRequestsException;
import com.supplysync.repository.UserRepository;
import com.supplysync.security.JwtService;
import com.supplysync.security.LoginRateLimiter;
import com.supplysync.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginRateLimiter rateLimiter;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registering user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new DuplicateResourceException("RESOURCE_CONFLICT", "Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("RESOURCE_CONFLICT", "Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .isActive(true)
                .isDeleted(false)
                .build();

        User savedUser = userRepository.save(user);
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        log.info("User registered successfully: {}", request.getUsername());

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        log.info("Login attempt for username: {} from IP: {}", request.getUsername(), ipAddress);

        if (!rateLimiter.isAllowed(ipAddress)) {
            log.warn("Login rate limit exceeded for IP: {}", ipAddress);
            throw new TooManyRequestsException("TOO_MANY_LOGIN_ATTEMPTS");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for username: {}", request.getUsername());
            rateLimiter.recordFailedAttempt(ipAddress);
            throw e;
        } catch (org.springframework.security.core.AuthenticationException e) {
            rateLimiter.recordFailedAttempt(ipAddress);
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getUsername()));

        if (!user.getIsActive()) {
            log.warn("User account is inactive: {}", request.getUsername());
            throw new InvalidOperationException("INVALID_OPERATION", "User account is inactive");
        }

        rateLimiter.resetFailedAttempts(ipAddress);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in successfully: {}", request.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }



    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // Check blacklist
        try {
            Boolean blacklisted = redisTemplate.hasKey("blacklist:" + refreshToken);
            if (Boolean.TRUE.equals(blacklisted)) throw new InvalidOperationException("INVALID_OPERATION", "Refresh token has been invalidated");
        } catch (InvalidOperationException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis unavailable for blacklist check: {}", e.getMessage());
        }

        String username = jwtService.extractUsername(refreshToken);
        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for token"));
            if (jwtService.isTokenValid(refreshToken, user)) {
                // Blacklist the old refresh token to prevent reuse/replay attacks
                try {
                    long remainingTtl = jwtService.getRemainingExpirationMs(refreshToken);
                    if (remainingTtl > 0) {
                        redisTemplate.opsForValue().set(
                                "blacklist:" + refreshToken,
                                "true",
                                remainingTtl,
                                TimeUnit.MILLISECONDS
                        );
                    }
                } catch (Exception e) {
                    log.error("Failed to blacklist old refresh token in Redis: {}", e.getMessage());
                }

                return TokenRefreshResponse.builder()
                        .accessToken(jwtService.generateAccessToken(user))
                        .refreshToken(jwtService.generateRefreshToken(user))
                        .build();
            }
        }
        throw new InvalidOperationException("INVALID_OPERATION", "Invalid refresh token");
    }

    @Override
    public void logout(String token) {
        log.info("Logging out user, blacklisting token");
        try {
            long remainingTtl = jwtService.getRemainingExpirationMs(token);
            if (remainingTtl > 0) {
                redisTemplate.opsForValue().set(
                        "blacklist:" + token,
                        "true",
                        remainingTtl,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token in Redis: {}", e.getMessage());
            // Fail-safe: log but do not fail logout request if Redis is down
        }
    }
}
