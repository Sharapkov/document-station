package com.itq.document_station.repository;

import com.itq.document_station.enumeration.StatusEnum;
import com.itq.document_station.model.Doc;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface DocRepository extends JpaRepository<Doc, Long>, JpaSpecificationExecutor<Doc> {

    @Query("SELECT d FROM Doc d " +
            "LEFT JOIN FETCH d.user " +
            "LEFT JOIN FETCH d.histories h " +
            "LEFT JOIN FETCH h.user " +
            "WHERE d.id = :id")
    Optional<Doc> findByIdWithHistory(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doc d " +
            "WHERE d.id IN :ids " +
            "ORDER BY d.id")
    List<Doc> findAllByIdWithLock(@Param("ids") List<Long> ids);

    List<Doc> findByStatusOrderByIdAsc(StatusEnum status, Pageable pageable);

    default void customSaveAll(List<Doc> docs) {}
}
