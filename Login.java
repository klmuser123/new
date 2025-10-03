package com.example.yourprojectname.dto; // Replace with your actual DTO package name

/**
 * Data Transfer Object (DTO) to encapsulate user credentials for login requests.
 * Used to receive the request body containing the user's identifier and password.
 */
public class Login {

    // The unique identifier (email or username) of the user
    private String identifier; 

    // The password provided by the user
    private String password; 

    /**
     * Default constructor (required for deserialization by frameworks like Spring).
     */
    public Login() {
        // Empty constructor
    }

    /**
     * Parameterized constructor for convenience.
     */
    public Login(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    // --- Getter and Setter Methods ---

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
