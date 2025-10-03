package com.example.yourprojectname.repository; // Replace with your actual repository package name

import com.example.yourprojectname.model.Admin; // Assuming your Admin entity is in this package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Admin entities.
 * Extends JpaRepository to inherit standard CRUD operations.
 * The entity type is Admin and the ID type is Long.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Custom query method to find an Admin entity by its username.
     * Spring Data JPA automatically generates the necessary query implementation.
     * * @param username The username of the Admin to find.
     * @return The Admin entity matching the username, or null if not found.
     */
    Admin findByUsername(String username);
}
