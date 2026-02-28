package com.management.demo.api.dto;

import java.time.LocalDate;

public record AnimalRequest(String name, String species, String breed, LocalDate birthDate, Long ownerId) {}
