package com.management.demo.service;

import com.management.demo.api.dto.EmployeeRequest;
import com.management.demo.api.dto.EmployeeResponse;
import com.management.demo.domain.entity.Employee;
import com.management.demo.domain.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .name(request.name())
                .specialty(request.specialty())
                .position(request.position())
                .build();
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return employeeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id));
    }

    private EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(e.getId(), e.getName(), e.getSpecialty(), e.getPosition());
    }
}
