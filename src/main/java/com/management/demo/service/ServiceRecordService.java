package com.management.demo.service;

import com.management.demo.api.dto.*;
import com.management.demo.domain.entity.ServiceRecord;
import com.management.demo.domain.entity.ServiceRecordEmployee;
import com.management.demo.domain.repository.AnimalRepository;
import com.management.demo.domain.repository.EmployeeRepository;
import com.management.demo.domain.repository.ServiceRecordRepository;
import com.management.demo.domain.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceRecordService {

    private final ServiceRecordRepository serviceRecordRepository;
    private final AnimalRepository animalRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ServiceRecordResponse create(ServiceRecordRequest request) {
        var animal = animalRepository.findById(request.animalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found: " + request.animalId()));

        ServiceRecord record = ServiceRecord.builder()
                .date(request.date())
                .notes(request.notes())
                .gainAmount(request.gainAmount())
                .animal(animal)
                .build();

        List<ServiceRecordEmployee> employeeLinks = request.employees().stream()
                .map(e -> {
                    var emp = employeeRepository.findById(e.employeeId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + e.employeeId()));
                    var svcType = serviceTypeRepository.findById(e.serviceTypeId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceType not found: " + e.serviceTypeId()));
                    return ServiceRecordEmployee.builder()
                            .serviceRecord(record)
                            .employee(emp)
                            .serviceType(svcType)
                            .build();
                })
                .toList();

        record.getServiceRecordEmployee().addAll(employeeLinks);

        return toResponse(serviceRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> findAll(LocalDateTime startDate, LocalDateTime endDate) {
        var records = (startDate != null && endDate != null)
                ? serviceRecordRepository.findByDateBetween(startDate, endDate)
                : serviceRecordRepository.findAll();
        return records.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ServiceRecordResponse findById(Long id) {
        return serviceRecordRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceRecord not found: " + id));
    }

    ServiceRecordResponse toResponse(ServiceRecord sr) {
        List<EmployeeInRecordResponse> serviceRecordEmployee = sr.getServiceRecordEmployee().stream()
                .map(e -> new EmployeeInRecordResponse(
                        e.getEmployee().getId(),
                        e.getEmployee().getName(),
                        e.getServiceType().getId(),
                        e.getServiceType().getName()
                ))
                .toList();

        var cost = sr.getServiceRecordEmployee().stream()
                .map(e -> e.getServiceType().getCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var profit = sr.getGainAmount().subtract(cost);

        return new ServiceRecordResponse(
                sr.getId(),
                sr.getDate(),
                sr.getNotes(),
                sr.getGainAmount(),
                cost,
                profit,
                sr.getAnimal().getId(),
                sr.getAnimal().getName(),
                sr.getAnimal().getOwner().getId(),
                sr.getAnimal().getOwner().getName(),
                serviceRecordEmployee
        );
    }
}
