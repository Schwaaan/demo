package com.management.demo.domain.repository;

import com.management.demo.domain.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {}
