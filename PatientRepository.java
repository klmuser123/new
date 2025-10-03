package com.example.yourprojectname.repository; // Replace with your actual repository package name

import com.example.yourprojectname.model.Patient; // Assuming your Patient entity is in this package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Patient entities.
 * Extends JpaRepository to inherit standard CRUD operations.
 * The entity type is Patient and the ID type is Long.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Finds a patient by their unique email address.
     * Uses Spring Data method naming convention.
     *
     * @param email The email address of the patient.
     * @return The Patient entity matching the email, or null if not found.
     */
    Patient findByEmail(String email);

    /**
     * Finds a patient using either their email address OR their phone number.
     * Uses Spring Data method naming convention for compound queries.
     *
     * @param email The email address to search for.
     * @param phone The phone number to search for.
     * @return The Patient entity matching either the email or the phone number, or null if not found.
     */
    Patient findByEmailOrPhone(String email, String phone);
}
