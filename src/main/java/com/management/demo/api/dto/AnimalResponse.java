package com.management.demo.api.dto;

import java.time.LocalDate;

public record AnimalResponse(Long id, String name, String species, String breed, LocalDate birthDate, Long ownerId, String ownerName) {}
