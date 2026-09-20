package com.djmart.service.impl;

import com.djmart.dao.UserDAO;
import com.djmart.dto.PageResponse;
import com.djmart.dto.UserResponse;
import com.djmart.exception.AuthenticationException;
import com.djmart.exception.ResourceNotFoundException;
import com.djmart.model.User;
import com.djmart.service.UserService;
import com.djmart.util.PasswordUtil;
import com.djmart.util.ValidationErrors;
import com.djmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business service implementation for user profile management.
 */
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserResponse findById(Long id) {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateId(id, "userId", errors);
        errors.throwIfHasErrors("Invalid user ID");

        User user = userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        return UserResponse.fromUser(user);
    }

    @Override
    public UserResponse findByEmail(String email) {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateEmail(email, errors);
        errors.throwIfHasErrors("Invalid email parameter");

        User user = userDAO.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return UserResponse.fromUser(user);
    }

    @Override
    public UserResponse updateProfile(Long userId, String name) {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateId(userId, "userId", errors);
        ValidationUtil.validateRequired(name, "name", errors);
        if (name != null && (name.trim().length() < 2 || name.trim().length() > 100)) {
            errors.addError("name", "Name must be between 2 and 100 characters");
        }
        errors.throwIfHasErrors("Profile update validation failed");

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setName(name.trim());
        userDAO.update(user);
        LOGGER.info("Updated profile name for user ID: {}", userId);

        return UserResponse.fromUser(user);
    }

    @Override
    public boolean changePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword) {
        ValidationErrors errors = new ValidationErrors();
        ValidationUtil.validateId(userId, "userId", errors);
        ValidationUtil.validateRequired(currentPassword, "currentPassword", errors);
        ValidationUtil.validatePassword(newPassword, errors);
        ValidationUtil.validatePasswordMatch(newPassword, confirmNewPassword, errors);
        errors.throwIfHasErrors("Password change validation failed");

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!PasswordUtil.checkPassword(currentPassword, user.getPasswordHash())) {
            LOGGER.warn("Password change rejected: Incorrect current password for user ID: {}", userId);
            throw new AuthenticationException("Current password is incorrect");
        }

        String newHashed = PasswordUtil.hashPassword(newPassword);
        boolean updated = userDAO.updatePassword(userId, newHashed);
        LOGGER.info("Password successfully updated for user ID: {}", userId);
        return updated;
    }

    @Override
    public PageResponse<UserResponse> findAllUsers(int page, int size) {
        int[] pagination = ValidationUtil.validatePagination(page, size);
        int safePage = pagination[0];
        int safeSize = pagination[1];
        int offset = (safePage - 1) * safeSize;

        List<User> users = userDAO.findAll(offset, safeSize);
        long total = userDAO.count();

        List<UserResponse> dtos = users.stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());

        return new PageResponse<>(dtos, safePage, safeSize, total);
    }
}
