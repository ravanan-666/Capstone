package com.djmart.dto;

import com.djmart.model.Role;
import com.djmart.model.User;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Public response DTO for user information.
 * SECURITY: Absolutely never contains password, password_hash, or authentication secrets.
 */
public class UserResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String email;
    private Role role;
    private Timestamp createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, Role role, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    /**
     * Factory method safely constructing a UserResponse from a User domain model.
     * Guaranteed to strip all password hashes and secrets.
     */
    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
