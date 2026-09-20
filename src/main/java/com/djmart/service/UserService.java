package com.djmart.service;

import com.djmart.dto.PageResponse;
import com.djmart.dto.UserResponse;

/**
 * Service interface managing user profiles and administration.
 */
public interface UserService {

    /**
     * Finds a user by ID and maps to a safe UserResponse DTO.
     */
    UserResponse findById(Long id);

    /**
     * Finds a user by email and maps to a safe UserResponse DTO.
     */
    UserResponse findByEmail(String email);

    /**
     * Updates user profile details (e.g., display name).
     */
    UserResponse updateProfile(Long userId, String name);

    /**
     * Changes user password after verifying the existing password.
     */
    boolean changePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword);

    /**
     * Returns a paginated list of all registered users (for admin management).
     */
    PageResponse<UserResponse> findAllUsers(int page, int size);
}
