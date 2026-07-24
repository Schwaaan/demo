package com.management.demo.api.controller;

import com.management.demo.api.dto.ServiceRecordRequest;
import com.management.demo.api.dto.ServiceRecordResponse;
import com.management.demo.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/service-records")
@RequiredArgsConstructor
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;

    @GetMapping
    public List<ServiceRecordResponse> findAll(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        return serviceRecordService.findAll(startDate, endDate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceRecordResponse create(@RequestBody ServiceRecordRequest request) {
        return serviceRecordService.create(request);
    }

    @GetMapping("/{id}")
    public ServiceRecordResponse findById(@PathVariable Long id) {
        return serviceRecordService.findById(id);
    }
}
