package com.management.demo.api.controller;

import com.management.demo.api.dto.ServiceRecordRequest;
import com.management.demo.api.dto.ServiceRecordResponse;
import com.management.demo.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-records")
@RequiredArgsConstructor
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;

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
