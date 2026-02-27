package com.itq.document_station.repository;

import com.itq.document_station.model.Register;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisterRepository extends JpaRepository<Register, Long> {
    Optional<Register> findByDocId(Long id);
}
