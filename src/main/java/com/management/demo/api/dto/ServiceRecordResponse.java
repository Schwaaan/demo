package com.management.demo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ServiceRecordResponse(
        Long id,
        LocalDateTime date,
        String notes,
        BigDecimal gainAmount,
        BigDecimal cost,
        BigDecimal profit,
        Long animalId,
        String animalName,
        Long ownerId,
        String ownerName,
        List<EmployeeInRecordResponse> serviceRecordEmployee
) {}
