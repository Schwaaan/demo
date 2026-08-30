package com.management.demo.api.controller;

import com.management.demo.api.dto.AnimalResponse;
import com.management.demo.api.dto.OwnerRequest;
import com.management.demo.api.dto.OwnerResponse;
import com.management.demo.service.AnimalService;
import com.management.demo.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;
    private final AnimalService animalService;

    @GetMapping
    public ResponseEntity<List<OwnerResponse>> findAll() {
        return ResponseEntity.ok(ownerService.findAll());
    }

    @PostMapping
    public ResponseEntity<OwnerResponse> create(@RequestBody OwnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ownerService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OwnerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ownerService.findById(id));
    }

    @GetMapping("/{id}/animals")
    public ResponseEntity<List<AnimalResponse>> findAnimals(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.findByOwnerId(id));
    }
}
