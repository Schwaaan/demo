package com.management.demo.domain.repository;

import com.management.demo.domain.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    List<ServiceRecord> findByDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT sr FROM ServiceRecord sr " +
           "JOIN FETCH sr.animal a " +
           "JOIN FETCH a.owner " +
           "LEFT JOIN FETCH sr.serviceRecordEmployee sre " +
           "LEFT JOIN FETCH sre.employee " +
           "LEFT JOIN FETCH sre.serviceType " +
           "WHERE a.id = :animalId " +
           "ORDER BY sr.date DESC")
    List<ServiceRecord> findByAnimalIdWithDetails(@Param("animalId") Long animalId);
}
