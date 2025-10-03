package com.example.yourprojectname.service;

import com.example.yourprojectname.dto.Login;
import com.example.yourprojectname.model.Appointment;
import com.example.yourprojectname.model.Doctor;
import com.example.yourprojectname.repository.AppointmentRepository;
import com.example.yourprojectname.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    // Dummy data for available slots (replace with actual logic, perhaps from a Doctor entity field)
    private static final List<LocalTime> ALL_SLOTS = List.of(
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0)
    );
    private static final LocalTime AM_END = LocalTime.of(12, 0);

    @Autowired
    public DoctorService(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // -------------------------------------------------------------------------
    // --- Availability Methods ---
    // -------------------------------------------------------------------------

    /**
     * Fetches the available 1-hour slots for a specific doctor on a given date.
     * Assumes ALL_SLOTS represents the doctor's standard working hours.
     */
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        // 1. Get all booked appointments for the doctor on the specified date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, startOfDay, endOfDay);

        // 2. Extract booked times (assuming appointments are 1 hour long)
        Set<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(a -> a.getAppointmentTime().toLocalTime())
                .collect(Collectors.toSet());

        // 3. Filter out booked slots from ALL_SLOTS
        return ALL_SLOTS.stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .map(LocalTime::toString) // Convert LocalTime to String format (e.g., "08:00")
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // --- CRUD Operations ---
    // -------------------------------------------------------------------------

    /**
     * Saves a new doctor to the database. Checks for existing email before saving.
     *
     * @return 1 for success, -1 if the doctor already exists, 0 for internal errors.
     */
    public int saveDoctor(Doctor doctor) {
        // Check if doctor already exists by email
        if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
            return -1; // Doctor already exists
        }
        try {
            // NOTE: In a real app, hash the password here before saving
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error saving doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    /**
     * Updates the details of an existing doctor.
     *
     * @return 1 for success, -1 if doctor not found, 0 for internal errors.
     */
    public int updateDoctor(Doctor doctor) {
        if (!doctorRepository.existsById(doctor.getId())) {
            return -1; // Doctor not found
        }
        try {
            // NOTE: Only update editable fields; password should be handled separately
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error updating doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    /**
     * Retrieves a list of all doctors.
     */
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Deletes a doctor by ID, ensuring associated appointments are deleted first.
     *
     * @return 1 for success, -1 if doctor not found, 0 for internal errors.
     */
    @Transactional
    public int deleteDoctor(long id) {
        if (!doctorRepository.existsById(id)) {
            return -1; // Doctor not found
        }
        try {
            // Delete all associated appointments first to maintain data integrity
            appointmentRepository.deleteAllByDoctorId(id);
            // Then delete the doctor
            doctorRepository.deleteById(id);
            return 1; // Success
        } catch (Exception e) {
            System.err.println("Error deleting doctor or associated appointments: " + e.getMessage());
            return 0; // Internal error
        }
    }

    // -------------------------------------------------------------------------
    // --- Authentication ---
    // -------------------------------------------------------------------------

    /**
     * Validates a doctor's login credentials and generates a token upon success.
     */
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();

        // 1. Find the doctor by email (identifier)
        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());

        if (doctor == null) {
            response.put("error", "Invalid credentials.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 2. Verify password (DUMMY IMPLEMENTATION)
        // NOTE: Replace this with proper password hashing/verification (e.g., using BCryptPasswordEncoder)
        boolean passwordMatches = login.getPassword().equals(doctor.getPassword());

        if (!passwordMatches) {
            response.put("error", "Invalid credentials.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 3. Generate token
        String token = tokenService.generateToken(doctor.getId(), "doctor"); // Assuming TokenService has generateToken
        response.put("token", token);
        response.put("role", "doctor");
        response.put("doctorId", String.valueOf(doctor.getId()));
        response.put("message", "Login successful.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // --- Search & Filter Methods ---
    // -------------------------------------------------------------------------

    /**
     * Finds doctors by their name (partial match).
     */
    public Map<String, Object> findDoctorByName(String name) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        return Collections.singletonMap("doctors", doctors);
    }

    /**
     * Filters doctors by name, specialty, and availability (AM/PM).
     */
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        // 1. Filter by Name and Specialty using custom repository query
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);

        // 2. Filter by Time (AM/PM)
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);

        return Collections.singletonMap("doctors", filteredDoctors);
    }

    /**
     * Filters doctors by name and their availability (AM/PM).
     */
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        // 1. Filter by Name (using a custom repository method or finding all and filtering)
        List<Doctor> doctors = doctorRepository.findByNameLike(name); // Assuming this is case-insensitive

        // 2. Filter by Time (AM/PM)
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);

        return Collections.singletonMap("doctors", filteredDoctors);
    }

    /**
     * Filters doctors by name and specialty.
     */
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        // Directly use the repository method
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return Collections.singletonMap("doctors", doctors);
    }

    /**
     * Filters doctors by specialty and their availability (AM/PM).
     */
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        // 1. Filter by Specialty
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);

        // 2. Filter by Time (AM/PM)
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);

        return Collections.singletonMap("doctors", filteredDoctors);
    }

    /**
     * Filters doctors only by specialty.
     */
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return Collections.singletonMap("doctors", doctors);
    }

    /**
     * Filters doctors only by their availability (AM/PM).
     */
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
        return Collections.singletonMap("doctors", filteredDoctors);
    }

    // -------------------------------------------------------------------------
    // --- Private Helper Filter ---
    // -------------------------------------------------------------------------

    /**
     * Private helper method to filter a list of doctors based on AM/PM availability.
     * NOTE: This requires a field on the Doctor entity to store availability (e.g., 'availableTimeStart' and 'availableTimeEnd').
     * For demonstration, this uses a simplified check against the ALL_SLOTS dummy.
     */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        String timeOfDay = amOrPm.toUpperCase();

        if (!timeOfDay.equals("AM") && !timeOfDay.equals("PM")) {
            return doctors; // Return unfiltered if criteria is invalid
        }

        return doctors.stream().filter(doctor -> {
            // Simplistic check: filter based on whether the doctor has any AM or PM slots
            
            // NOTE: In a real application, you would check the doctor's actual availability schedule fields
            // For this implementation, we check if they have any potential slot in that period.
            boolean hasAmSlot = ALL_SLOTS.stream().anyMatch(t -> t.isBefore(AM_END));
            boolean hasPmSlot = ALL_SLOTS.stream().anyMatch(t -> t.isAfter(AM_END));

            if (timeOfDay.equals("AM")) {
                return hasAmSlot;
            } else { // PM
                return hasPmSlot;
            }
        }).collect(Collectors.toList());
    }
}
