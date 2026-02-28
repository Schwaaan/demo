package com.management.demo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ServiceRecordResponse(
        Long id,
        LocalDate date,
        String notes,
        BigDecimal gainAmount,
        BigDecimal cost,
        BigDecimal profit,
        Long animalId,
        String animalName,
        ServiceTypeResponse serviceType,
        List<EmployeeInRecordResponse> employees
) {}
