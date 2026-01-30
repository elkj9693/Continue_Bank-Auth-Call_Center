package com.entrusting.backend.ars.repository;

import com.entrusting.backend.ars.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByCustomerRefOrderByIdAsc(String customerRef);

    Optional<Card> findByCardRef(String cardRef);
}
