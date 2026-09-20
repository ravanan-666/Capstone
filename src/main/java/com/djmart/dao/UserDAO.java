package com.djmart.dao;

import com.djmart.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entities.
 */
public interface UserDAO {

    /**
     * Finds a user by primary key ID.
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by their unique email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Creates a new user record in the database.
     *
     * @param user user entity to persist
     * @return persisted user entity with generated ID and timestamp
     */
    User create(User user);

    /**
     * Updates an existing user's profile information (name, role).
     */
    boolean update(User user);

    /**
     * Updates a user's password hash.
     */
    boolean updatePassword(Long userId, String newPasswordHash);

    /**
     * Returns a paginated list of all users (for admin management).
     */
    List<User> findAll(int offset, int limit);

    /**
     * Returns total user count.
     */
    long count();

    /**
     * Deletes a user by ID.
     */
    boolean delete(Long id);
}
