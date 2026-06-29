package com.learn.auth.repositary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.auth.entities.User;
import java.util.Optional;


public interface UserRepositary extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
	
}
