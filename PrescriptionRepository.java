package com.example.yourprojectname.repository; // Replace with your actual repository package name

import com.example.yourprojectname.model.Prescription; // Assuming your Prescription model is in this package
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Prescription entities stored in MongoDB.
 * Extends MongoRepository to inherit standard MongoDB CRUD operations.
 * The entity type is Prescription and the ID type (MongoDB default) is String.
 */
@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    /**
     * Finds prescriptions associated with a specific appointment ID.
     * Uses Spring Data MongoDB method naming convention, where 'appointmentId' 
     * is assumed to be a field within the Prescription document.
     *
     * @param appointmentId The ID of the Appointment associated with the prescriptions.
     * @return A list of Prescription entities matching the appointment ID.
     */
    List<Prescription> findByAppointmentId(Long appointmentId);
}
