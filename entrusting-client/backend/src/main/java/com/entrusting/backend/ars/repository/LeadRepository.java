package com.entrusting.backend.ars.repository;

import com.entrusting.backend.ars.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, String> {
    List<Lead> findByStatus(String status);

    List<Lead> findByStatusNot(String status);

    List<Lead> findByCustomerRef(String customerRef);
}
