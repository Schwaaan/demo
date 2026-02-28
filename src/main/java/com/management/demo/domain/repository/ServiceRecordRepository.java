package com.management.demo.domain.repository;

import com.management.demo.domain.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    @Query("SELECT DISTINCT sr FROM ServiceRecord sr " +
           "JOIN FETCH sr.serviceType " +
           "JOIN FETCH sr.animal a " +
           "JOIN FETCH a.owner " +
           "LEFT JOIN FETCH sr.employees sre " +
           "LEFT JOIN FETCH sre.employee " +
           "WHERE a.id = :animalId " +
           "ORDER BY sr.date DESC")
    List<ServiceRecord> findByAnimalIdWithDetails(@Param("animalId") Long animalId);
}
