package com.example.yourprojectname.service;

import com.example.yourprojectname.model.Prescription;
import com.example.yourprojectname.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    // -------------------------------------------------------------------------

    /**
     * 1. Saves a prescription to the MongoDB database.
     *
     * @param prescription The prescription object to be saved.
     * @return A response message indicating the result of the save operation (201 or 500).
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        try {
            // Attempt to save the prescription
            prescriptionRepository.save(prescription);
            
            // Return 201 Created status on success
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Prescription saved successfully."),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the error and return 500 Internal Server Error
            System.err.println("Error saving prescription: " + e.getMessage());
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Failed to save prescription due to an internal error."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------

    /**
     * 2. Retrieves the prescriptions associated with a specific appointment ID.
     *
     * @param appointmentId The appointment ID whose associated prescriptions are to be retrieved.
     * @return A response containing the list of prescriptions or an error message (200 or 500).
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        try {
            // Fetch the prescriptions by appointment ID
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);

            // Return 200 OK status with the results (may be empty list if none found)
            Map<String, Object> response = Collections.singletonMap("prescriptions", prescriptions);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            // Log the error and return 500 Internal Server Error
            System.err.println("Error retrieving prescription for appointment ID " + appointmentId + ": " + e.getMessage());
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Failed to retrieve prescription due to an internal error."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
