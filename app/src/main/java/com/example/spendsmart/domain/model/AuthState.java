package com.example.spendsmart.domain.model;

public class AuthState {
    public enum Status {
        LOADING,
        LOGGED_OUT,
        LOGGED_IN
    }

    public final Status status;
    public final UserProfile user;

    private AuthState(Status status, UserProfile user) {
        this.status = status;
        this.user = user;
    }

    public static AuthState loading() {
        return new AuthState(Status.LOADING, null);
    }

    public static AuthState loggedOut() {
        return new AuthState(Status.LOGGED_OUT, null);
    }

    public static AuthState loggedIn(UserProfile user) {
        return new AuthState(Status.LOGGED_IN, user);
    }

    public boolean isLoggedIn() {
        return status == Status.LOGGED_IN;
    }
}
