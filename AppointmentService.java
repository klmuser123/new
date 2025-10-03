package com.example.yourprojectname.service;

import com.example.yourprojectname.dto.AppointmentDTO;
import com.example.yourprojectname.model.Appointment;
import com.example.yourprojectname.model.Patient;
import com.example.yourprojectname.repository.AppointmentRepository;
import com.example.yourprojectname.repository.DoctorRepository;
import com.example.yourprojectname.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    // Declare necessary repositories and services
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService; // For token validation/extraction

    @Autowired
    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    // -------------------------------------------------------------------------------------------------
    // Helper/Validation Method (Assumed)
    // -------------------------------------------------------------------------------------------------
    
    /**
     * Dummy method representing complex validation logic (e.g., checking doctor availability).
     * In a real app, this would check time slots, doctor ID existence, etc.
     * Returns an empty map on success, or an error map on failure.
     */
    private Map<String, String> validateAppointment(Appointment appointment) {
        // Simple check: Doctor must exist
        if (!doctorRepository.existsById(appointment.getDoctorId())) {
            return Collections.singletonMap("error", "Invalid Doctor ID.");
        }
        
        // Simple check: Patient must exist
        if (!patientRepository.existsById(appointment.getPatientId())) {
            return Collections.singletonMap("error", "Invalid Patient ID.");
        }

        // Check for existing appointments for the same doctor at the same time slot (1-hour window assumed)
        LocalDateTime start = appointment.getAppointmentTime();
        LocalDateTime end = start.plusMinutes(59).plusSeconds(59); 
        
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
            appointment.getDoctorId(), start.minusSeconds(1), end.plusSeconds(1) // Check for overlaps
        );

        // Filter out the current appointment itself during an update
        if (existingAppointments.stream().anyMatch(a -> !a.getId().equals(appointment.getId()))) {
             return Collections.singletonMap("error", "Time slot already booked for this doctor.");
        }

        return Collections.emptyMap(); // Valid
    }


    // -------------------------------------------------------------------------------------------------
    // Core Business Methods
    // -------------------------------------------------------------------------------------------------

    /**
     * Books a new appointment after basic validation.
     * * @param appointment The Appointment object to book.
     * @return 1 if successful, 0 if there's an error.
     */
    public int bookAppointment(Appointment appointment) {
        // Perform initial validation before saving
        Map<String, String> validationResult = validateAppointment(appointment);

        if (validationResult.isEmpty()) {
            try {
                appointmentRepository.save(appointment);
                return 1; // Success
            } catch (Exception e) {
                // Log exception and return failure
                System.err.println("Error booking appointment: " + e.getMessage());
                return 0; // Error
            }
        }
        // Validation failed
        return 0;
    }

    /**
     * Updates an existing appointment, ensuring data integrity and availability.
     * * @param appointment The appointment object with updated fields.
     * @return A response message indicating success or failure.
     */
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        
        // 1. Check if the appointment exists
        Appointment existingAppointment = appointmentRepository.findById(appointment.getId()).orElse(null);

        if (existingAppointment == null) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Appointment not found."),
                    HttpStatus.NOT_FOUND);
        }

        // 2. Validate the update (e.g., check for time conflicts, valid IDs)
        Map<String, String> validationErrors = validateAppointment(appointment);

        if (!validationErrors.isEmpty()) {
            return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
        }

        // 3. Update fields (assuming the input 'appointment' DTO fields are mapped to the entity)
        // In a real application, you'd carefully map only editable fields
        existingAppointment.setDoctorId(appointment.getDoctorId());
        existingAppointment.setAppointmentTime(appointment.getAppointmentTime());
        existingAppointment.setStatus(appointment.getStatus());
        // ... set other fields

        try {
            appointmentRepository.save(existingAppointment);
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Appointment updated successfully."),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Failed to save updated appointment."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancels an existing appointment.
     * Assumes token contains user ID (patient ID) for authorization.
     * * @param id The ID of the appointment to cancel.
     * @param token The authorization token.
     * @return A response message indicating success or failure.
     */
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        
        Appointment appointmentToCancel = appointmentRepository.findById(id).orElse(null);

        if (appointmentToCancel == null) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Appointment not found."),
                    HttpStatus.NOT_FOUND);
        }

        // 1. Authorization Check: Get user ID from token
        // Assuming tokenService.validateToken returns user details if valid
        Map<String, String> tokenInfo = tokenService.validateToken(token, "patient"); // Check if patient
        
        if (tokenInfo.isEmpty() || !tokenInfo.containsKey("userId")) {
             return new ResponseEntity<>(
                    Collections.singletonMap("error", "Invalid or expired token."),
                    HttpStatus.UNAUTHORIZED);
        }

        Long authenticatedPatientId = Long.parseLong(tokenInfo.get("userId"));

        // 2. Authorization: Ensure the patient cancelling is the one who booked it
        if (!appointmentToCancel.getPatientId().equals(authenticatedPatientId)) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Unauthorized to cancel this appointment."),
                    HttpStatus.FORBIDDEN);
        }

        // 3. Delete the appointment
        try {
            appointmentRepository.delete(appointmentToCancel);
            return new ResponseEntity<>(
                    Collections.singletonMap("message", "Appointment cancelled successfully."),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Failed to delete appointment."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a list of appointments for a specific doctor on a specific date,
     * with optional filtering by patient name.
     * * @param pname Patient name to filter by (can be null/empty).
     * @param date The date for appointments.
     * @param token The authorization token (used to identify the doctor).
     * @return A map containing the list of AppointmentDTOs or an error message.
     */
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();

        // 1. Authorization: Extract Doctor ID from the token
        Map<String, String> tokenInfo = tokenService.validateToken(token, "doctor");
        
        if (tokenInfo.isEmpty() || !tokenInfo.containsKey("userId")) {
            response.put("error", "Invalid or unauthorized token.");
            return response;
        }

        Long doctorId = Long.parseLong(tokenInfo.get("userId"));

        // 2. Define Time Range for the entire day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> appointments;

        // 3. Fetch appointments based on filtering criteria
        if (pname != null && !pname.trim().isEmpty()) {
            // Filter by doctor ID, partial patient name, and time range
            appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctorId, pname, startOfDay, endOfDay);
        } else {
            // Filter only by doctor ID and time range
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctorId, startOfDay, endOfDay);
        }

        // 4. Convert Appointment entities to AppointmentDTOs
        List<AppointmentDTO> dtoList = appointments.stream()
            .map(this::convertToDto) // Helper method to map entity to DTO
            .collect(Collectors.toList());

        response.put("appointments", dtoList);
        return response;
    }

    /**
     * Helper method to convert an Appointment entity to an AppointmentDTO.
     * In a real application, this would fetch Doctor/Patient names from repositories.
     */
    private AppointmentDTO convertToDto(Appointment appointment) {
        // Dummy data for doctorName, patientName, etc., as we don't have the entity structure here.
        // In a real scenario, you'd use patientRepository.findById(appointment.getPatientId()) etc.
        Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(new Patient());
        String patientFullName = patient.getName() != null ? patient.getName() : "Unknown Patient";
        String doctorFullName = doctorRepository.findById(appointment.getDoctorId())
                                .map(d -> d.getFirstName() + " " + d.getLastName()).orElse("Unknown Doctor");

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
}
