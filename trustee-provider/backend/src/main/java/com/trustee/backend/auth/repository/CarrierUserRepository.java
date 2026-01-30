package com.trustee.backend.auth.repository;

import com.trustee.backend.auth.entity.CarrierUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarrierUserRepository extends JpaRepository<CarrierUser, Long> {
    Optional<CarrierUser> findByPhoneNumber(String phoneNumber);
}
