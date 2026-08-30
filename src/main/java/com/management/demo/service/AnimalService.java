package com.management.demo.service;

import com.management.demo.api.dto.*;
import com.management.demo.domain.entity.Animal;
import com.management.demo.domain.repository.AnimalRepository;
import com.management.demo.domain.repository.OwnerRepository;
import com.management.demo.domain.repository.ServiceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final OwnerRepository ownerRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final ServiceRecordService serviceRecordService;

    @Transactional
    public AnimalResponse create(AnimalRequest request) {
        var owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found: " + request.ownerId()));

        Animal animal = Animal.builder()
                .name(request.name())
                .species(request.species())
                .breed(request.breed())
                .birthDate(request.birthDate())
                .owner(owner)
                .build();

        return toResponse(animalRepository.save(animal));
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> findByOwnerId(Long ownerId) {
        if (!ownerRepository.existsById(ownerId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found: " + ownerId);
        return animalRepository.findByOwnerId(ownerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimalResponse findById(Long id) {
        return animalRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found: " + id));
    }

    @Transactional(readOnly = true)
    public AnimalHistoryResponse getHistory(Long animalId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found: " + animalId));

        List<ServiceRecordResponse> records = serviceRecordRepository.findByAnimalIdWithDetails(animalId)
                .stream()
                .map(serviceRecordService::toResponse)
                .toList();

        BigDecimal totalCost = records.stream()
                .map(ServiceRecordResponse::cost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGain = records.stream()
                .map(ServiceRecordResponse::gainAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = totalGain.subtract(totalCost);

        var owner = animal.getOwner();
        var ownerResponse = new OwnerResponse(owner.getId(), owner.getName(), owner.getPhone(), owner.getEmail());

        return new AnimalHistoryResponse(toResponse(animal), ownerResponse, records, totalCost, totalGain, totalProfit);
    }

    AnimalResponse toResponse(Animal animal) {
        return new AnimalResponse(
                animal.getId(), animal.getName(), animal.getSpecies(),
                animal.getBreed(), animal.getBirthDate(),
                animal.getOwner().getId(), animal.getOwner().getName()
        );
    }
}
