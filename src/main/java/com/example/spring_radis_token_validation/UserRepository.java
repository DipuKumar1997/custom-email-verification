package com.example.spring_radis_token_validation;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<Users,Long> {
    Optional<Users> findById(@NonNull Long id);

    Users findByEmail(String email);
}
