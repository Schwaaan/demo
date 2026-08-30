package com.management.demo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ServiceRecordRequest(
        Long animalId,
        BigDecimal gainAmount,
        LocalDateTime date,
        String notes,
        List<EmployeeInRecordRequest> employees
) {}
