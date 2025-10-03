package com.example.yourprojectname.controller; 

import com.example.yourprojectname.service.TokenService; // Import the service interface
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Controller class for handling dashboard requests based on user roles and validating tokens.
 * Annotate the class with @Controller to indicate it returns views.
 */
@Controller
public class DashboardController {

    // Autowire the required service for token validation logic
    private final TokenService tokenService;

    @Autowired
    public DashboardController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    // -------------------------------------------------------------------------

    /**
     * Handles requests to the admin dashboard.
     * Annotate with @GetMapping("/adminDashboard/{token}").
     *
     * @param token The user's token accepted as a @PathVariable.
     * @return The "admin/adminDashboard" view if valid, or a redirect to login otherwise.
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        
        // Call validateToken(token, "admin") and check if the returned map is empty.
        Map<String, String> validationResult = tokenService.validateToken(token, "admin");

        if (validationResult.isEmpty()) {
            // If empty: Token is valid -> return the admin/adminDashboard view.
            return "admin/adminDashboard";
        } else {
            // If not empty: Redirect to login page at http://localhost:8080.
            return "redirect:/"; 
        }
    }
    
    // -------------------------------------------------------------------------

    /**
     * Handles requests to the doctor dashboard.
     * Annotate with @GetMapping("/doctorDashboard/{token}").
     *
     * @param token The user's token accepted as a @PathVariable.
     * @return The "doctor/doctorDashboard" view if valid, or a redirect to login otherwise.
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        
        // Call validateToken(token, "doctor") and apply the same logic as adminDashboard.
        Map<String, String> validationResult = tokenService.validateToken(token, "doctor");

        if (validationResult.isEmpty()) {
            // If empty: Token is valid -> return the doctor/doctorDashboard view.
            return "doctor/doctorDashboard";
        } else {
            // If not empty: Redirect to login page at http://localhost:8080.
            return "redirect:/";
        }
    }
}
