package com.example.yourprojectname.controller;

import com.example.yourprojectname.dto.Login;
import com.example.yourprojectname.model.Patient;
import com.example.yourprojectname.service.PatientService;
import com.example.yourprojectname.service.Service; // Central validation/coordination service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * REST Controller for handling Patient operations, including registration, login, 
 * fetching details, and managing appointments.
 */
@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    @Autowired
    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    // --- Helper for consistent error response creation ---
    private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(Collections.singletonMap("error", message), status);
    }

    // -------------------------------------------------------------------------
    // 1. Get Patient Details
    // -------------------------------------------------------------------------

    /**
     * Fetches the details of the authenticated patient.
     * Endpoint: GET /patient/{token}
     */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable String token) {

        // 1. Validate Token (Patient only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "patient");
        if (validationError != null) {
            return new ResponseEntity(validationError.getBody(), validationError.getStatusCode());
        }

        // 2. Fetch Patient Details
        // The service layer handles fetching details using the email/ID extracted from the token.
        return patientService.getPatientDetails(token);
    }

    // -------------------------------------------------------------------------
    // 2. Create a New Patient (Registration)
    // -------------------------------------------------------------------------

    /**
     * Registers a new patient after checking for existing records.
     * Endpoint: POST /patient
     */
    @PostMapping()
    public ResponseEntity<Map<String, String>> createNewPatient(@RequestBody Patient patient) {

        // 1. Validate if patient exists by email or phone
        // true = patient does NOT exist (safe to register)
        boolean validationResult = service.validatePatient(patient);

        if (!validationResult) {
            return createErrorResponse(
                    "Patient with email id or phone no already exist",
                    HttpStatus.CONFLICT);
        }

        // 2. Create Patient
        int creationResult = patientService.createPatient(patient);

        if (creationResult == 1) {
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Signup successful"),
                    HttpStatus.CREATED);
        } else {
            return createErrorResponse(
                    "Internal server error: Failed to create patient record",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // 3. Patient Login
    // -------------------------------------------------------------------------

    /**
     * Validates patient credentials and issues a JWT token.
     * Endpoint: POST /patient/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(@RequestBody Login login) {
        // Validation and token generation are handled entirely within the central Service class
        return service.validatePatientLogin(login);
    }

    // -------------------------------------------------------------------------
    // 4. Get Patient Appointments (All)
    // -------------------------------------------------------------------------

    /**
     * Fetches all appointments for the specified patient ID, after token validation.
     * Endpoint: GET /patient/{id}/{token}
     */
    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointments(
            @PathVariable Long id,
            @PathVariable String token) {

        // 1. Validate Token (Patient only)
        // Note: The service.validateToken logic should ensure the ID in the path matches the ID in the token
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "patient");
        if (validationError != null) {
            return new ResponseEntity(validationError.getBody(), validationError.getStatusCode());
        }

        // 2. Fetch Appointments
        // patientService.getPatientAppointment will perform the secondary ID match check.
        return patientService.getPatientAppointment(id, token);
    }

    // -------------------------------------------------------------------------
    // 5. Filter Patient Appointments
    // -------------------------------------------------------------------------

    /**
     * Filters patient appointments by condition ("past", "future") and doctor name.
     * Endpoint: GET /patient/filter/{condition}/{name}/{token}
     *
     * @param condition The condition to filter appointments by (e.g., "past", "future").
     * @param name The doctor's name to filter appointments by (use "all" or "none" if not filtering by name).
     * @param token The authentication token.
     * @return Filtered list of patient appointments or an error message.
     */
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointments(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        // 1. Validate Token (Patient only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "patient");
        if (validationError != null) {
            return new ResponseEntity(validationError.getBody(), validationError.getStatusCode());
        }

        // 2. Normalize parameters for filtering
        String filterCondition = condition.equalsIgnoreCase("all") || condition.equalsIgnoreCase("none") ? null : condition;
        String filterName = name.equalsIgnoreCase("all") || name.equalsIgnoreCase("none") ? null : name;

        // 3. Filter Appointments (Central service handles combining filters)
        return service.filterPatient(filterCondition, filterName, token);
    }
}
