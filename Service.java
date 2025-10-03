package com.example.yourprojectname.service;

import com.example.yourprojectname.dto.Login;
import com.example.yourprojectname.model.Admin;
import com.example.yourprojectname.model.Appointment;
import com.example.yourprojectname.model.Patient;
import com.example.yourprojectname.repository.AdminRepository;
import com.example.yourprojectname.repository.DoctorRepository;
import com.example.yourprojectname.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service("centralService") // Give it a distinct name if 'Service' conflicts with TokenService, etc.
public class Service {

    // --- Repositories and Services ---
    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    // DUMMY ADMIN CREDENTIALS for validation (REPLACE WITH REAL HASHING/STORAGE)
    private static final String DUMMY_ADMIN_USERNAME = "admin";
    private static final String DUMMY_ADMIN_PASS = "admin123";

    @Autowired
    public Service(
            TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // -------------------------------------------------------------------------
    // --- Authentication and Token Validation ---
    // -------------------------------------------------------------------------

    /**
     * Checks the validity of a token for a given user.
     *
     * @param token The token to be validated.
     * @param user The user role ("admin", "doctor", "patient").
     * @return An error response if the token is invalid or expired.
     */
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        // Assuming tokenService.validateToken returns an error map if invalid, or an empty map if valid.
        Map<String, String> validationResult = tokenService.validateToken(token, user);

        if (!validationResult.isEmpty()) {
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "Token is invalid or expired."),
                    HttpStatus.UNAUTHORIZED);
        }
        return null; // Return null (or an empty successful response) if valid
    }

    /**
     * Validates the login credentials of an admin.
     *
     * @param receivedAdmin The admin credentials (username and password).
     * @return A generated token if the admin is authenticated.
     */
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();

        // 1. Find admin by username
        Admin storedAdmin = adminRepository.findByUsername(receivedAdmin.getUsername());

        if (storedAdmin == null) {
            response.put("error", "Invalid username or password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 2. Verify password (DUMMY IMPLEMENTATION - REPLACE WITH PASSWORD HASHING)
        boolean passwordMatches = receivedAdmin.getPassword().equals(storedAdmin.getPassword());

        if (!passwordMatches) {
            response.put("error", "Invalid username or password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 3. Generate token
        String token = tokenService.generateToken(storedAdmin.getId(), "admin"); // Assuming generateToken exists
        response.put("token", token);
        response.put("role", "admin");
        response.put("adminId", String.valueOf(storedAdmin.getId()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Validates a patient's login credentials.
     *
     * @param login The login credentials (email and password).
     * @return A generated token if the login is valid.
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();

        // 1. Find patient by email
        Patient patient = patientRepository.findByEmail(login.getIdentifier());

        if (patient == null) {
            response.put("error", "Invalid email or password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 2. Verify password (DUMMY IMPLEMENTATION - REPLACE WITH PASSWORD HASHING)
        boolean passwordMatches = login.getPassword().equals(patient.getPassword());

        if (!passwordMatches) {
            response.put("error", "Invalid email or password.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 3. Generate token
        String token = tokenService.generateToken(patient.getId(), "patient");
        response.put("token", token);
        response.put("role", "patient");
        response.put("patientId", String.valueOf(patient.getId()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // --- Doctor and Patient Filtering/Validation ---
    // -------------------------------------------------------------------------

    /**
     * Filters doctors based on name, specialty, and available time.
     *
     * @param name The name of the doctor.
     * @param specialty The specialty of the doctor.
     * @param time The available time of the doctor ("AM" or "PM").
     * @return A map containing the list of matching doctors.
     */
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        // Determine which DoctorService method to call based on input
        if (name != null && specialty != null && time != null) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        } else if (name != null && specialty != null) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (name != null && time != null) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        } else if (specialty != null && time != null) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        } else if (specialty != null) {
            return doctorService.filterDoctorBySpecility(specialty);
        } else if (time != null) {
            return doctorService.filterDoctorsByTime(time);
        } else if (name != null) {
            return doctorService.findDoctorByName(name); // Assuming findDoctorByName handles general name search
        } else {
            // Default: return all doctors
            return Collections.singletonMap("doctors", doctorService.getDoctors());
        }
    }

    /**
     * Validates whether an appointment is available based on the doctor's schedule.
     *
     * @param appointment The appointment to validate (containing doctorId and appointmentTime).
     * @return 1 if valid, 0 if unavailable, -1 if doctor doesn't exist.
     */
    public int validateAppointment(Appointment appointment) {
        // 1. Check if the doctor exists
        if (!doctorRepository.existsById(appointment.getDoctorId())) {
            return -1; // Doctor doesn't exist
        }

        // 2. Check doctor's availability for the specific time slot
        Long doctorId = appointment.getDoctorId();
        LocalTime appointmentTime = appointment.getAppointmentTime().toLocalTime();
        LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();

        // Get available slots for the day
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, appointmentDate);
        String requiredSlot = appointmentTime.toString();

        // 3. Check if the required time slot is in the available list
        if (availableSlots.contains(requiredSlot)) {
            return 1; // Appointment time is valid (available)
        } else {
            return 0; // Appointment time is unavailable (already booked or not a working slot)
        }
    }

    /**
     * Checks whether a patient exists based on their email or phone number.
     *
     * @param patient The patient object with email/phone details.
     * @return true if the patient *does not* exist (safe to register), false if the patient *exists* already.
     */
    public boolean validatePatient(Patient patient) {
        // Use findByEmailOrPhone to check for duplicates
        Patient existingPatient = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());

        // If a patient is found, they already exist (return false)
        return existingPatient == null;
    }

    /**
     * Filters patient appointments based on certain criteria, such as condition and doctor name.
     *
     * @param condition The condition ("past" or "future").
     * @param name The doctor's name.
     * @param token The authentication token to identify the patient.
     * @return The filtered list of patient appointments.
     */
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Long patientId;

        // 1. Authorization: Extract Patient ID from token
        try {
            patientId = tokenService.extractUserId(token); // Assuming this extracts the patient ID
            if (patientId == null) {
                 return new ResponseEntity<>(Collections.singletonMap("error", "Unauthorized access."), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("error", "Invalid or expired token."), HttpStatus.UNAUTHORIZED);
        }

        // 2. Determine which PatientService method to call
        if (condition != null && name != null) {
            // Filter by both condition and doctor name
            return patientService.filterByDoctorAndCondition(condition, name, patientId);
        } else if (condition != null) {
            // Filter only by condition (past/future)
            return patientService.filterByCondition(condition, patientId);
        } else if (name != null) {
            // Filter only by doctor name
            return patientService.filterByDoctor(name, patientId);
        } else {
            // Default: return all patient appointments (assuming getPatientAppointment handles this when filters are null/empty)
            return patientService.getPatientAppointment(patientId, token);
        }
    }
}
