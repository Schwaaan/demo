package com.management.demo.api.controller;

import com.management.demo.api.dto.AnimalHistoryResponse;
import com.management.demo.api.dto.AnimalRequest;
import com.management.demo.api.dto.AnimalResponse;
import com.management.demo.service.AnimalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnimalResponse create(@RequestBody AnimalRequest request) {
        return animalService.create(request);
    }

    @GetMapping("/{id}")
    public AnimalResponse findById(@PathVariable Long id) {
        return animalService.findById(id);
    }

    @GetMapping("/{id}/history")
    public AnimalHistoryResponse getHistory(@PathVariable Long id) {
        return animalService.getHistory(id);
    }
}
