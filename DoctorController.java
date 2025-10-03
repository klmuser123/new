package com.example.yourprojectname.controller;

import com.example.yourprojectname.dto.Login;
import com.example.yourprojectname.model.Doctor;
import com.example.yourprojectname.service.DoctorService;
import com.example.yourprojectname.service.Service; // Central validation/coordination service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for handling Doctor operations, including CRUD, login, and filtering.
 */
@RestController
@RequestMapping("${api.path}" + "doctor") // Base URL: e.g., /api/v1/doctor
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    @Autowired
    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    // --- Helper for consistent error response creation ---
    private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(Collections.singletonMap("error", message), status);
    }

    // -------------------------------------------------------------------------
    // 1. Get Doctor Availability
    // -------------------------------------------------------------------------

    /**
     * Fetches the available time slots for a specific doctor on a given date.
     * Endpoint: GET /doctor/availability/{user}/{doctorId}/{date}/{token}
     */
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String token) {

        // Validate Token for the requesting user (admin, patient, or doctor)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, user);
        if (validationError != null) {
            return new ResponseEntity(validationError.getBody(), validationError.getStatusCode());
        }

        // Fetch availability
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);

        Map<String, Object> response = new HashMap<>();
        response.put("doctorId", doctorId);
        response.put("date", date.toString());
        response.put("availableSlots", availableSlots);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // 2. Get List of Doctors
    // -------------------------------------------------------------------------

    /**
     * Fetches a list of all doctors.
     * Endpoint: GET /doctor
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        List<Doctor> doctors = doctorService.getDoctors();
        return new ResponseEntity<>(Collections.singletonMap("doctors", doctors), HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // 3. Add New Doctor (Admin Access)
    // -------------------------------------------------------------------------

    /**
     * Adds a new doctor to the system. Requires Admin token validation.
     * Endpoint: POST /doctor/{token}
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        // 1. Validate Token (Admin only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "admin");
        if (validationError != null) {
            return validationError;
        }

        // 2. Save Doctor
        int result = doctorService.saveDoctor(doctor);

        if (result == 1) {
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Doctor added to db"),
                    HttpStatus.CREATED);
        } else if (result == -1) {
            return createErrorResponse("Doctor already exists", HttpStatus.CONFLICT);
        } else { // result == 0
            return createErrorResponse("Some internal error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // 4. Doctor Login
    // -------------------------------------------------------------------------

    /**
     * Validates doctor credentials and issues a JWT token.
     * Endpoint: POST /doctor/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        // Validation and token generation are handled entirely within DoctorService
        return doctorService.validateDoctor(login);
    }

    // -------------------------------------------------------------------------
    // 5. Update Doctor Details (Admin Access)
    // -------------------------------------------------------------------------

    /**
     * Updates an existing doctor's details. Requires Admin token validation.
     * Endpoint: PUT /doctor/{token}
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        // 1. Validate Token (Admin only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "admin");
        if (validationError != null) {
            return validationError;
        }

        // 2. Update Doctor
        int result = doctorService.updateDoctor(doctor);

        if (result == 1) {
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Doctor updated"),
                    HttpStatus.OK);
        } else if (result == -1) {
            return createErrorResponse("Doctor not found", HttpStatus.NOT_FOUND);
        } else { // result == 0
            return createErrorResponse("Some internal error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // 6. Delete Doctor (Admin Access)
    // -------------------------------------------------------------------------

    /**
     * Deletes a doctor and all associated appointments. Requires Admin token validation.
     * Endpoint: DELETE /doctor/{id}/{token}
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @PathVariable String token) {

        // 1. Validate Token (Admin only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "admin");
        if (validationError != null) {
            return validationError;
        }

        // 2. Delete Doctor
        int result = doctorService.deleteDoctor(id);

        if (result == 1) {
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Doctor deleted successfully"),
                    HttpStatus.OK);
        } else if (result == -1) {
            return createErrorResponse("Doctor not found with id " + id, HttpStatus.NOT_FOUND);
        } else { // result == 0
            return createErrorResponse("Some internal error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // 7. Filter Doctors
    // -------------------------------------------------------------------------

    /**
     * Filters doctors based on name, time (AM/PM), and specialty.
     * Placeholder values "all" or "none" are expected for unused path variables.
     * Endpoint: GET /doctor/filter/{name}/{time}/{speciality}
     */
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public Map<String, Object> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {

        // Replace "all" or "none" with null before passing to service layer
        String filterName = name.equalsIgnoreCase("all") || name.equalsIgnoreCase("none") ? null : name;
        String filterTime = time.equalsIgnoreCase("all") || time.equalsIgnoreCase("none") ? null : time;
        String filterSpecialty = speciality.equalsIgnoreCase("all") || speciality.equalsIgnoreCase("none") ? null : speciality;

        // The central service handles the complex filtering logic based on null/present parameters
        return service.filterDoctor(filterName, filterSpecialty, filterTime);
    }
}
