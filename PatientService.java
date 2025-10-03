package com.example.yourprojectname.service;

import com.example.yourprojectname.dto.AppointmentDTO;
import com.example.yourprojectname.model.Appointment;
import com.example.yourprojectname.model.Doctor;
import com.example.yourprojectname.model.Patient;
import com.example.yourprojectname.repository.AppointmentRepository;
import com.example.yourprojectname.repository.DoctorRepository;
import com.example.yourprojectname.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    @Autowired
    public PatientService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    // -------------------------------------------------------------------------
    // --- Private Helper Methods ---
    // -------------------------------------------------------------------------

    /**
     * Helper method to convert an Appointment entity to an AppointmentDTO.
     */
    private AppointmentDTO convertToDto(Appointment appointment) {
        // Fetch patient and doctor details required for the DTO
        Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(new Patient());
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(new Doctor());
        
        String patientFullName = patient.getName() != null ? patient.getName() : "Unknown Patient";
        String doctorFullName = doctor.getFirstName() != null ? doctor.getFirstName() + " " + doctor.getLastName() : "Unknown Doctor";

        return new AppointmentDTO(
            appointment.getId(),
            appointment.getDoctorId(),
            doctorFullName,
            appointment.getPatientId(),
            patientFullName,
            patient.getEmail(),
            patient.getPhone(),
            patient.getAddress(),
            appointment.getAppointmentTime(),
            appointment.getStatus()
        );
    }

    private ResponseEntity<Map<String, Object>> createResponse(List<Appointment> appointments) {
        List<AppointmentDTO> dtoList = appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        Map<String, Object> response = Collections.singletonMap("appointments", dtoList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = Collections.singletonMap("error", message);
        return new ResponseEntity<>(error, status);
    }

    // -------------------------------------------------------------------------
    // --- Core Methods ---
    // -------------------------------------------------------------------------

    /**
     * 1. Saves a new patient to the database.
     * @param patient The patient object to be saved.
     * @return 1 on success, 0 on failure.
     */
    public int createPatient(Patient patient) {
        try {
            // NOTE: In a real app, hash the password here before saving
            patientRepository.save(patient);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error saving patient: " + e.getMessage());
            return 0; // Failure
        }
    }

    /**
     * 2. Retrieves a list of appointments for a specific patient, ensuring authorization.
     * @param id The patient's ID.
     * @param token The JWT token containing the authenticated user ID.
     * @return A response containing a list of AppointmentDTOs or an error.
     */
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        // 1. Authorization: Verify ID from token matches requested ID
        try {
            Long authenticatedId = tokenService.extractUserId(token); // Assuming this extracts the patient ID
            
            if (authenticatedId == null || !authenticatedId.equals(id)) {
                return createErrorResponse("Unauthorized access to patient appointments.", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return createErrorResponse("Invalid or expired token.", HttpStatus.UNAUTHORIZED);
        }

        // 2. Retrieve all appointments for the patient
        List<Appointment> appointments = appointmentRepository.findByPatientId(id);

        // 3. Convert and return
        return createResponse(appointments);
    }

    /**
     * 3. Filters appointments by condition (past or future) for a specific patient.
     * @param condition The condition to filter by ("past" or "future").
     * @param id The patient’s ID.
     * @return The filtered appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        int status;
        String lowerCondition = condition.toLowerCase();

        if (lowerCondition.equals("past")) {
            status = 1; // Assuming 1 means past/completed
        } else if (lowerCondition.equals("future")) {
            status = 0; // Assuming 0 means future/scheduled
        } else {
            return createErrorResponse("Invalid condition specified. Use 'past' or 'future'.", HttpStatus.BAD_REQUEST);
        }

        // Retrieve appointments for a patient by status, ordered by time
        List<Appointment> appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);

        return createResponse(appointments);
    }

    /**
     * 4. Filters the patient's appointments by doctor's name.
     * @param name The name of the doctor.
     * @param patientId The ID of the patient.
     * @return The filtered appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        // Use the repository method for filtering by doctor name and patient ID
        List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);

        return createResponse(appointments);
    }

    /**
     * 5. Filters the patient's appointments by doctor's name and appointment condition.
     * @param condition The condition to filter by ("past" or "future").
     * @param name The name of the doctor.
     * @param patientId The ID of the patient.
     * @return The filtered appointments or an error message.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        int status;
        String lowerCondition = condition.toLowerCase();

        if (lowerCondition.equals("past")) {
            status = 1; // Assuming 1 means past/completed
        } else if (lowerCondition.equals("future")) {
            status = 0; // Assuming 0 means future/scheduled
        } else {
            return createErrorResponse("Invalid condition specified. Use 'past' or 'future'.", HttpStatus.BAD_REQUEST);
        }
        
        // Use the repository method combining doctor name, patient ID, and status
        List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);

        return createResponse(appointments);
    }

    /**
     * 6. Fetches the patient's details based on the provided JWT token (email).
     * @param token The JWT token containing the patient's email.
     * @return The patient's details or an error message.
     */
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        String email;
        try {
            // 1. Extract email from token
            email = tokenService.extractUserEmail(token); // Assuming this extracts the patient email
        } catch (Exception e) {
            return createErrorResponse("Invalid or expired token.", HttpStatus.UNAUTHORIZED);
        }

        if (email == null) {
            return createErrorResponse("Token does not contain user email.", HttpStatus.UNAUTHORIZED);
        }

        // 2. Retrieve patient by email
        Patient patient = patientRepository.findByEmail(email);

        if (patient == null) {
            return createErrorResponse("Patient not found.", HttpStatus.NOT_FOUND);
        }

        // 3. Return patient details
        Map<String, Object> response = new HashMap<>();
        response.put("patient", patient);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
