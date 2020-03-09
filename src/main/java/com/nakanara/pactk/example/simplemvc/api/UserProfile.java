package com.nakanara.pactk.example.simplemvc.api;

public class UserProfile {
    private String name;
    private String email;

    public UserProfile(){

    }
    public UserProfile(String name, String email) {
        setName(name);
        setEmail(email);
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
}
