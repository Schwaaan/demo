package com.management.demo.domain.repository;

import com.management.demo.domain.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {}
