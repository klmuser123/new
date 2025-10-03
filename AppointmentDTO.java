package com.example.yourprojectname.dto; // Replace with your actual DTO package name

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object (DTO) for representing appointment data.
 * Used to structure and simplify data exchange between the backend and frontend.
 */
public class AppointmentDTO {

    // --- Core Fields ---
    private final Long id;
    private final Long doctorId;
    private final String doctorName;
    private final Long patientId;
    private final String patientName;
    private final String patientEmail;
    private final String patientPhone;
    private final String patientAddress;
    private final LocalDateTime appointmentTime;
    private final int status;

    // --- Calculated/Formatted Fields ---
    private final LocalDate appointmentDate;
    private final LocalTime appointmentTimeOnly;
    private final LocalDateTime endTime;

    /**
     * Constructor to initialize all core appointment fields and automatically
     * calculate derivative time fields (appointmentDate, appointmentTimeOnly, endTime).
     */
    public AppointmentDTO(
            Long id, 
            Long doctorId, 
            String doctorName, 
            Long patientId, 
            String patientName, 
            String patientEmail, 
            String patientPhone, 
            String patientAddress, 
            LocalDateTime appointmentTime, 
            int status) {
        
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.appointmentTime = appointmentTime;
        this.status = status;

        // Auto-calculation of derivative fields
        this.appointmentDate = appointmentTime.toLocalDate();
        this.appointmentTimeOnly = appointmentTime.toLocalTime();
        this.endTime = appointmentTime.plusHours(1); // Calculated as appointmentTime + 1 hour
    }

    // --- Getter Methods ---

    public Long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTimeOnly() {
        return appointmentTimeOnly;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
