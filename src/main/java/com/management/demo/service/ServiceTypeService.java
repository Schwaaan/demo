package com.management.demo.service;

import com.management.demo.api.dto.ServiceTypeRequest;
import com.management.demo.api.dto.ServiceTypeResponse;
import com.management.demo.domain.entity.ServiceType;
import com.management.demo.domain.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    @Transactional
    public ServiceTypeResponse create(ServiceTypeRequest request) {
        ServiceType serviceType = ServiceType.builder()
                .name(request.name())
                .description(request.description())
                .cost(request.cost())
                .build();
        return toResponse(serviceTypeRepository.save(serviceType));
    }

    @Transactional(readOnly = true)
    public List<ServiceTypeResponse> findAll() {
        return serviceTypeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ServiceType findEntityById(Long id) {
        return serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceType not found: " + id));
    }

    private ServiceTypeResponse toResponse(ServiceType st) {
        return new ServiceTypeResponse(st.getId(), st.getName(), st.getDescription(), st.getCost());
    }
}
