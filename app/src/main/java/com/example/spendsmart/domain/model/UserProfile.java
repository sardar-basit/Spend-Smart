package com.example.spendsmart.domain.model;

public class UserProfile {
    public final long id;
    public final String name;
    public final String email;

    public UserProfile(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
