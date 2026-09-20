package com.djmart.service.impl;

import com.djmart.dao.UserDAO;
import com.djmart.dto.LoginRequest;
import com.djmart.dto.RegisterRequest;
import com.djmart.dto.UserResponse;
import com.djmart.exception.AuthenticationException;
import com.djmart.exception.ConflictException;
import com.djmart.model.Role;
import com.djmart.model.User;
import com.djmart.service.AuthService;
import com.djmart.util.PasswordUtil;
import com.djmart.util.ValidationErrors;
import com.djmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Business service implementation for user registration and authentication.
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserDAO userDAO;

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        // 1. Validate input
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateRequired(request.getName(), "name", errors);
        if (request.getName() != null && (request.getName().trim().length() < 2 || request.getName().trim().length() > 100)) {
            errors.addError("name", "Name must be between 2 and 100 characters");
        }
        ValidationUtil.validateEmail(request.getEmail(), errors);
        ValidationUtil.validatePassword(request.getPassword(), errors);
        ValidationUtil.validatePasswordMatch(request.getPassword(), request.getConfirmPassword(), errors);

        // Role validation (Admin role can only be assigned via database seed per specification F1)
        Role role = Role.BUYER;
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            String roleStr = request.getRole().trim().toUpperCase();
            if ("SELLER".equals(roleStr)) {
                role = Role.SELLER;
            } else if ("BUYER".equals(roleStr)) {
                role = Role.BUYER;
            } else {
                errors.addError("role", "Public registration only permits BUYER or SELLER roles");
            }
        }

        errors.throwIfHasErrors("Registration validation failed");

        // 2. Prevent duplicate email registration
        Optional<User> existing = userDAO.findByEmail(request.getEmail().trim());
        if (existing.isPresent()) {
            LOGGER.warn("Registration failed: Email {} is already registered", request.getEmail());
            throw new ConflictException("Email address is already registered");
        }

        // 3. Cryptographic salted password hash with jBCrypt
        String hashedPassword = PasswordUtil.hashPassword(request.getPassword());

        // 4. Persist user entity
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(hashedPassword);
        user.setRole(role);

        User created = userDAO.create(user);
        LOGGER.info("Successfully registered new user: {} with role {}", created.getEmail(), created.getRole());

        // 5. Return safe response stripping all passwords
        return UserResponse.fromUser(created);
    }

    @Override
    public UserResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }

        // 1. Validate inputs
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateRequired(request.getEmail(), "email", errors);
        ValidationUtil.validateRequired(request.getPassword(), "password", errors);
        errors.throwIfHasErrors("Email and password are required for login");

        // 2. Lookup user by email
        User user = userDAO.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> {
                    LOGGER.warn("Authentication failed: User with email {} not found", request.getEmail());
                    return new AuthenticationException("Invalid email or password");
                });

        // 3. Verify salted BCrypt password hash
        boolean passwordMatches = PasswordUtil.checkPassword(request.getPassword(), user.getPasswordHash());
        if (!passwordMatches) {
            LOGGER.warn("Authentication failed: Invalid password candidate for email {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        LOGGER.info("User {} successfully authenticated with role {}", user.getEmail(), user.getRole());
        return UserResponse.fromUser(user);
    }
}
