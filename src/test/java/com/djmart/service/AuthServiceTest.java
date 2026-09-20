package com.djmart.service;

import com.djmart.dao.UserDAO;
import com.djmart.dto.LoginRequest;
import com.djmart.dto.RegisterRequest;
import com.djmart.dto.UserResponse;
import com.djmart.exception.AuthenticationException;
import com.djmart.exception.ConflictException;
import com.djmart.exception.ValidationException;
import com.djmart.model.Role;
import com.djmart.model.User;
import com.djmart.service.impl.AuthServiceImpl;
import com.djmart.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userDAO);
    }

    @Test
    @DisplayName("Register: Successfully registers new user and hashes password")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest(
                "Jane Developer",
                "jane@example.com",
                "Password@123",
                "Password@123",
                "BUYER"
        );

        when(userDAO.findByEmail("jane@example.com")).thenReturn(Optional.empty());

        User savedUser = new User(
                10L,
                "Jane Developer",
                "jane@example.com",
                PasswordUtil.hashPassword("Password@123"),
                Role.BUYER,
                Timestamp.from(Instant.now())
        );
        when(userDAO.create(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Jane Developer", response.getName());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals(Role.BUYER, response.getRole());

        verify(userDAO).findByEmail("jane@example.com");
        verify(userDAO).create(any(User.class));
    }

    @Test
    @DisplayName("Register: Duplicate email throws ConflictException")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Existing User",
                "existing@example.com",
                "Password@123",
                "Password@123",
                "BUYER"
        );

        when(userDAO.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userDAO, never()).create(any());
    }

    @Test
    @DisplayName("Register: Invalid password or mismatch throws ValidationException")
    void testRegisterValidationFailures() {
        // Password mismatch
        RegisterRequest mismatch = new RegisterRequest(
                "Test User", "test@example.com", "Password@123", "WrongPassword#123", "BUYER"
        );
        assertThrows(ValidationException.class, () -> authService.register(mismatch));

        // Weak password (too short)
        RegisterRequest weak = new RegisterRequest(
                "Test User", "test@example.com", "Pass@1", "Pass@1", "BUYER"
        );
        assertThrows(ValidationException.class, () -> authService.register(weak));

        // Invalid email format
        RegisterRequest invalidEmail = new RegisterRequest(
                "Test User", "not-an-email", "Password@123", "Password@123", "BUYER"
        );
        assertThrows(ValidationException.class, () -> authService.register(invalidEmail));
    }

    @Test
    @DisplayName("Login: Valid credentials returns UserResponse")
    void testLoginSuccess() {
        String rawPassword = "Password@123";
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        User user = new User(1L, "Admin User", "admin@djmart.com", hashedPassword, Role.ADMIN, Timestamp.from(Instant.now()));
        when(userDAO.findByEmail("admin@djmart.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("admin@djmart.com", rawPassword);
        UserResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("Admin User", response.getName());
        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    @DisplayName("Login: Incorrect password throws AuthenticationException")
    void testLoginIncorrectPassword() {
        User user = new User(1L, "Admin User", "admin@djmart.com", PasswordUtil.hashPassword("CorrectPassword#1"), Role.ADMIN, null);
        when(userDAO.findByEmail("admin@djmart.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("admin@djmart.com", "WrongPassword#2");
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Login: Non-existent email throws AuthenticationException")
    void testLoginUserNotFound() {
        when(userDAO.findByEmail("ghost@djmart.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("ghost@djmart.com", "Password@123");
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }
}
