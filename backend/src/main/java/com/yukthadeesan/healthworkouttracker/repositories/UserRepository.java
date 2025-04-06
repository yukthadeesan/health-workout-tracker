package com.yukthadeesan.healthworkouttracker.repositories;

import com.yukthadeesan.healthworkouttracker.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // This method will be automatically implemented by Spring Data JPA
    // It allows you to find a user by their username
    User findByUsername(String username);

    // Optional: Add more custom query methods if needed
    // For example, to check if a username exists
    boolean existsByUsername(String username);
}