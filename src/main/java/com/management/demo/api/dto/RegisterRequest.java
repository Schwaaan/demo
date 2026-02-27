package com.management.demo.api.dto;

public record RegisterRequest(String username, String email, String name, String password, String role) {}
