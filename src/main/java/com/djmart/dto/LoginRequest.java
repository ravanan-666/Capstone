package com.djmart.dto;

import java.io.Serializable;

/**
 * DTO for user login authentication requests.
 */
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        // SECURITY: Never print passwords in toString or logs!
        return "LoginRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}
