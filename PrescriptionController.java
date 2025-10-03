package com.example.yourprojectname.controller;

import com.example.yourprojectname.model.Prescription;
import com.example.yourprojectname.service.PrescriptionService;
import com.example.yourprojectname.service.Service; // Central validation/coordination service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for handling Prescription operations (saving and retrieving).
 */
@RestController
@RequestMapping("${api.path}" + "prescription") // Base URL: e.g., /api/v1/prescription
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService, Service service) {
        this.prescriptionService = prescriptionService;
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // 1. Save Prescription (Doctor Access)
    // -------------------------------------------------------------------------

    /**
     * Saves a new prescription after validating the doctor's token.
     * Endpoint: POST /api/v1/prescription/{token}
     *
     * @param token The doctor's authentication token.
     * @param prescription The prescription object to be saved.
     * @return Success or error message.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable String token,
            @RequestBody Prescription prescription) {

        // 1. Validate Token (Doctor only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "doctor");
        if (validationError != null) {
            return validationError;
        }

        // 2. Save Prescription
        return prescriptionService.savePrescription(prescription);
    }

    // -------------------------------------------------------------------------
    // 2. Get Prescription by Appointment ID (Doctor Access)
    // -------------------------------------------------------------------------

    /**
     * Retrieves prescriptions associated with a specific appointment ID.
     * Endpoint: GET /api/v1/prescription/{appointmentId}/{token}
     *
     * @param appointmentId The ID of the appointment.
     * @param token The doctor's authentication token.
     * @return Prescription details or an error message.
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        // 1. Validate Token (Doctor only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "doctor");
        if (validationError != null) {
            return new ResponseEntity(validationError.getBody(), validationError.getStatusCode());
        }

        // 2. Retrieve Prescription
        // The service layer handles the retrieval logic and potential "not found" scenarios.
        return prescriptionService.getPrescription(appointmentId);
    }
}
