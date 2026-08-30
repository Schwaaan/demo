package com.management.demo.api.dto;

import java.math.BigDecimal;

public record ServiceTypeResponse(Long id, String name, String description, BigDecimal cost) {}
