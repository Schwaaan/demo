package com.management.demo.api.dto;

import java.math.BigDecimal;

public record ServiceTypeRequest(String name, String description, BigDecimal cost) {}
