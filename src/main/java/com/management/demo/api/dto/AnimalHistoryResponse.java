package com.management.demo.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnimalHistoryResponse(
        AnimalResponse animal,
        OwnerResponse owner,
        List<ServiceRecordResponse> records,
        BigDecimal totalCost,
        BigDecimal totalGain,
        BigDecimal totalProfit
) {}
