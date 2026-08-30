package com.management.demo.service;

import com.management.demo.api.dto.OwnerRequest;
import com.management.demo.api.dto.OwnerResponse;
import com.management.demo.domain.entity.Owner;
import com.management.demo.domain.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;

    @Transactional
    public OwnerResponse create(OwnerRequest request) {
        Owner owner = Owner.builder()
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .build();
        Owner saved = ownerRepository.save(owner);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OwnerResponse> findAll() {
        return ownerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OwnerResponse findById(Long id) {
        return ownerRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found: " + id));
    }

    private OwnerResponse toResponse(Owner owner) {
        return new OwnerResponse(owner.getId(), owner.getName(), owner.getPhone(), owner.getEmail());
    }
}
