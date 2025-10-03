package com.example.yourprojectname.repository; // Replace with your actual repository package name

import com.example.yourprojectname.model.Doctor; // Assuming your Doctor entity is in this package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Doctor entities.
 * Extends JpaRepository to inherit standard CRUD operations.
 * The entity type is Doctor and the ID type is Long.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Finds a doctor by their unique email address using Spring Data naming convention.
     *
     * @param email The email address of the doctor.
     * @return The Doctor entity matching the email, or null if not found.
     */
    Doctor findByEmail(String email);

    /**
     * Finds doctors by partial match on their full name (first name and last name combined).
     * Uses @Query with LIKE and CONCAT for flexible pattern matching.
     * Assumes Doctor entity has 'firstName' and 'lastName' fields.
     *
     * @param name The partial name string to search for.
     * @return A list of doctors whose combined name matches the pattern.
     */
    @Query("SELECT d FROM Doctor d " +
           "WHERE CONCAT(d.firstName, ' ', d.lastName) LIKE %:name%")
    List<Doctor> findByNameLike(@Param("name") String name);

    /**
     * Filters doctors by partial name match and exact specialty, both case-insensitive.
     * Uses @Query with LOWER and LIKE for robust case-insensitive partial and exact matching.
     *
     * @param name The partial name string to search for.
     * @param specialty The specialty to filter by.
     * @return A list of matching doctors.
     */
    @Query("SELECT d FROM Doctor d " +
           "WHERE LOWER(d.specialty) = LOWER(:specialty) " +
           "AND LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
            @Param("name") String name,
            @Param("specialty") String specialty);

    /**
     * Finds doctors by specialty, ignoring case, using Spring Data naming convention.
     *
     * @param specialty The specialty to search for.
     * @return A list of doctors with the matching specialty.
     */
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}
