package com.management.demo.api.controller;

import com.management.demo.api.dto.OwnerRequest;
import com.management.demo.api.dto.OwnerResponse;
import com.management.demo.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @PostMapping
    public ResponseEntity<OwnerResponse> create(@RequestBody OwnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ownerService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OwnerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ownerService.findById(id));
    }
}
