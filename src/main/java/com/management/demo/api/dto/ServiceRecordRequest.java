package com.management.demo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ServiceRecordRequest(
        Long animalId,
        Long serviceTypeId,
        BigDecimal gainAmount,
        LocalDate date,
        String notes,
        List<EmployeeInRecordRequest> employees
) {}
