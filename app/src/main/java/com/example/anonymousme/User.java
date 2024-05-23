package com.example.anonymousme;

public class User {
    private String edumailString;
    private String username;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String edumailString, String username) {
        this.edumailString = edumailString;
        this.username = username;
    }

    public String getEdumailString() {

        return edumailString;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        return;
    }
}
