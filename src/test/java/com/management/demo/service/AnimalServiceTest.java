package com.management.demo.service;

import com.management.demo.api.dto.AnimalRequest;
import com.management.demo.api.dto.AnimalResponse;
import com.management.demo.domain.entity.Animal;
import com.management.demo.domain.entity.Owner;
import com.management.demo.domain.repository.AnimalRepository;
import com.management.demo.domain.repository.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock private AnimalRepository animalRepository;
    @Mock private OwnerRepository ownerRepository;
    @InjectMocks private AnimalService animalService;

    @Test
    void shouldCreateAnimal() {
        Owner owner = Owner.builder().id(1L).name("Joao").build();
        AnimalRequest request = new AnimalRequest("Rex", "Cachorro", "Labrador", null, 1L);
        Animal saved = Animal.builder().id(1L).name("Rex").species("Cachorro").breed("Labrador").owner(owner).build();

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(animalRepository.save(any())).thenReturn(saved);

        AnimalResponse response = animalService.create(request);

        assertThat(response.name()).isEqualTo("Rex");
        assertThat(response.ownerName()).isEqualTo("Joao");
        assertThat(response.ownerId()).isEqualTo(1L);
    }
}
