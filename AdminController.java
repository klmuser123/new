package com.example.yourprojectname.controller;

import com.example.yourprojectname.model.Admin;
import com.example.yourprojectname.service.Service; // Import the central Service class
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for handling Admin login operations and issuing tokens.
 */
@RestController
@RequestMapping("${api.path}" + "admin") // Base URL: e.g., /api/v1/admin
public class AdminController {

    // Autowire the central Service class for business logic
    private final Service service;

    @Autowired
    public AdminController(Service service) {
        this.service = service;
    }

    /**
     * Handles POST requests for admin login validation.
     * Endpoint: POST /api/v1/admin/login
     *
     * @param receivedAdmin The Admin object containing username and password from the request body.
     * @return ResponseEntity containing a JWT token or an error message.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin receivedAdmin) {
        
        // Call the validateAdmin method from the Service class
        return service.validateAdmin(receivedAdmin);
    }
}
