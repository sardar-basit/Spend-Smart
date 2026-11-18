package com.example.spendsmart.domain.model;

public class AuthResult {
    public final boolean success;
    public final UserProfile user;
    public final String errorMessage;

    private AuthResult(boolean success, UserProfile user, String errorMessage) {
        this.success = success;
        this.user = user;
        this.errorMessage = errorMessage;
    }

    public static AuthResult success(UserProfile user) {
        return new AuthResult(true, user, null);
    }

    public static AuthResult error(String message) {
        return new AuthResult(false, null, message);
    }
}
