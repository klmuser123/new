package com.example.yourprojectname.controller;

import com.example.yourprojectname.model.Appointment;
import com.example.yourprojectname.service.AppointmentService;
import com.example.yourprojectname.service.Service; // Central validation/coordination service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

/**
 * REST Controller for managing all CRUD operations related to appointments.
 */
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // --- GET: Retrieve Appointments (Doctor Access) ---
    // -------------------------------------------------------------------------

    /**
     * Retrieves a list of appointments for a doctor on a specific date, optionally filtered by patient name.
     * Endpoint: GET /appointments/{date}/{patientName}/{token}
     *
     * @param date The date of appointments.
     * @param patientName The name of the patient to filter by (can be "all" or any string).
     * @param token The doctor's authorization token.
     * @return List of AppointmentDTOs or an error response.
     */
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token) {

        // 1. Validate Token (Doctor only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "doctor");
        if (validationError != null) {
            // Returns Unauthorized (401) or other error from validateToken
            return new ResponseEntity(validationError.getBody(), validationError.getStatusCode());
        }

        // 2. Adjust patientName for null/empty search if passed as "all"
        String pname = patientName.equalsIgnoreCase("all") ? null : patientName;

        // 3. Fetch Appointments
        Map<String, Object> result = appointmentService.getAppointment(pname, date, token);

        // Check if the service returned a business error (e.g., doctor ID not found in token)
        if (result.containsKey("error")) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // --- POST: Book Appointment (Patient Access) ---
    // -------------------------------------------------------------------------

    /**
     * Books a new appointment after validating the token and the time slot.
     * Endpoint: POST /appointments/{token}
     *
     * @param appointment The Appointment object to book.
     * @param token The patient's authorization token.
     * @return Success or error message.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {

        // 1. Validate Token (Patient only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "patient");
        if (validationError != null) {
            return validationError;
        }

        // 2. Validate Appointment Availability
        int validationResult = service.validateAppointment(appointment);

        if (validationResult == -1) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Doctor not found."),
                    HttpStatus.BAD_REQUEST);
        }
        if (validationResult == 0) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Appointment time is unavailable for the doctor."),
                    HttpStatus.CONFLICT);
        }
        // validationResult == 1 means valid, proceed

        // 3. Book Appointment
        int bookingResult = appointmentService.bookAppointment(appointment);

        if (bookingResult == 1) {
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Appointment booked successfully."),
                    HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Failed to book appointment due to service error."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // --- PUT: Update Appointment (Patient Access) ---
    // -------------------------------------------------------------------------

    /**
     * Updates an existing appointment.
     * Endpoint: PUT /appointments/{token}
     *
     * @param appointment The Appointment object with updated details.
     * @param token The patient's authorization token.
     * @return Success or error message.
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {

        // 1. Validate Token (Patient only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "patient");
        if (validationError != null) {
            return validationError;
        }

        // NOTE: Additional business logic for re-validating the time slot must be handled
        // within appointmentService.updateAppointment() or checked here explicitly.
        
        // 2. Update Appointment
        return appointmentService.updateAppointment(appointment);
    }

    // -------------------------------------------------------------------------
    // --- DELETE: Cancel Appointment (Patient Access) ---
    // -------------------------------------------------------------------------

    /**
     * Cancels an existing appointment.
     * Endpoint: DELETE /appointments/{id}/{token}
     *
     * @param id The ID of the appointment to cancel.
     * @param token The patient's authorization token.
     * @return Success or error message.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token) {

        // 1. Validate Token (Patient only)
        ResponseEntity<Map<String, String>> validationError = service.validateToken(token, "patient");
        if (validationError != null) {
            return validationError;
        }

        // 2. Cancel Appointment (Service handles patient ID authorization check)
        return appointmentService.cancelAppointment(id, token);
    }
}
