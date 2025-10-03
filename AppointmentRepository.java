package com.example.yourprojectname.repository; // Replace with your actual repository package name

import com.example.yourprojectname.model.Appointment; // Assuming your Appointment entity is in this package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing Appointment entities.
 * Extends JpaRepository to inherit standard CRUD operations.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // --- Advanced Search and Filtering ---

    /**
     * Retrieve appointments for a specific doctor within a given time range.
     * Uses LEFT JOIN FETCH to eagerly load related Doctor and Availability data 
     * in a single query, optimizing performance and avoiding the N+1 problem.
     * * @param doctorId The ID of the doctor.
     * @param start The start date and time of the range (inclusive).
     * @param end The end date and time of the range (exclusive).
     * @return A list of appointments.
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "WHERE a.doctorId = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Filter appointments by doctor ID, partial patient name (case-insensitive), and time range.
     * Uses Spring Data method naming convention for filtering on the Patient's name attribute.
     * * @param doctorId The ID of the doctor.
     * @param patientName The partial name of the patient to search for.
     * @param start The start date and time of the range.
     * @param end The end date and time of the range.
     * @return A list of appointments.
     */
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            Long doctorId,
            String patientName,
            LocalDateTime start,
            LocalDateTime end);

    // --- Deletion Operations ---

    /**
     * Delete all appointments related to a specific doctor ID.
     * Requires @Modifying and @Transactional annotations.
     *
     * @param doctorId The ID of the doctor whose appointments are to be deleted.
     */
    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);

    // --- Patient-Specific Queries ---

    /**
     * Find all appointments for a specific patient using method name convention.
     *
     * @param patientId The ID of the patient.
     * @return A list of appointments.
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Retrieve appointments for a patient by status, ordered ascending by appointment time.
     * Uses Spring Data method naming convention.
     *
     * @param patientId The ID of the patient.
     * @param status The status of the appointment (e.g., 0 for scheduled).
     * @return A list of appointments ordered by time.
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    // --- Custom Filtering with Joins and Case-Insensitive Matching ---

    /**
     * Search appointments by partial doctor name (case-insensitive) and patient ID.
     * Uses @Query with LOWER and CONCAT for robust partial name matching.
     * Assuming doctor's name is stored in 'd.firstName' and 'd.lastName' (modify if different).
     *
     * @param doctorName The partial name of the doctor.
     * @param patientId The ID of the patient.
     * @return A list of matching appointments.
     */
    @Query("SELECT a FROM Appointment a " +
           "JOIN a.doctor d " +
           "WHERE a.patientId = :patientId " +
           "AND LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :doctorName, '%'))")
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId);


    /**
     * Filter appointments by partial doctor name, patient ID, and specific status.
     * Extends the previous query with an additional status filter.
     *
     * @param doctorName The partial name of the doctor.
     * @param patientId The ID of the patient.
     * @param status The status of the appointment.
     * @return A list of matching appointments.
     */
    @Query("SELECT a FROM Appointment a " +
           "JOIN a.doctor d " +
           "WHERE a.patientId = :patientId " +
           "AND a.status = :status " +
           "AND LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :doctorName, '%'))")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status);
}
