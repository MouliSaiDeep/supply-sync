package com.supplysync.service;

import com.supplysync.dto.request.LoginRequest;
import com.supplysync.dto.request.RegisterRequest;
import com.supplysync.dto.request.TokenRefreshRequest;
import com.supplysync.dto.response.LoginResponse;
import com.supplysync.dto.response.RegisterResponse;
import com.supplysync.dto.response.TokenRefreshResponse;

public interface AuthService {
    /**
     * Registers a new user.
     * @param request the registration details
     * @return the registration response with tokens
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Authenticates a user and returns JWT tokens.
     * @param request the login credentials
     * @param ipAddress the client's IP address
     * @return the login response with tokens
     */
    LoginResponse login(LoginRequest request, String ipAddress);

    /**
     * Refreshes the access token using a valid refresh token.
     * @param request the token refresh request containing the refresh token
     * @return the token refresh response with new tokens
     */
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

    /**
     * Logs out the user by blacklisting the current access token.
     * @param token the access token to blacklist
     */
    void logout(String token);
}
