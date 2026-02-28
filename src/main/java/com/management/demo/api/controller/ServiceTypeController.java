package com.management.demo.api.controller;

import com.management.demo.api.dto.ServiceTypeRequest;
import com.management.demo.api.dto.ServiceTypeResponse;
import com.management.demo.service.ServiceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @PostMapping
    public ResponseEntity<ServiceTypeResponse> create(@RequestBody ServiceTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceTypeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ServiceTypeResponse>> findAll() {
        return ResponseEntity.ok(serviceTypeService.findAll());
    }
}
