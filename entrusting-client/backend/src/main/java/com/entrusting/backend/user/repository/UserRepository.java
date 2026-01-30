package com.entrusting.backend.user.repository;

import com.entrusting.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    java.util.List<User> findByPhoneNumber(String phoneNumber);
    java.util.List<User> findByMarketingAgreedTrue();
}
