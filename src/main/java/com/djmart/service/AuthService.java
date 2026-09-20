package com.djmart.service;

import com.djmart.dto.LoginRequest;
import com.djmart.dto.RegisterRequest;
import com.djmart.dto.UserResponse;

/**
 * Service interface managing user authentication and registration workflows.
 */
public interface AuthService {

    /**
     * Registers a new user with validation and salted BCrypt password hashing.
     * Prevents duplicate email registration.
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticates user credentials and returns safe UserResponse.
     */
    UserResponse login(LoginRequest request);
}
