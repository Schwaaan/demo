package com.management.demo.service;

import com.management.demo.api.dto.OwnerRequest;
import com.management.demo.api.dto.OwnerResponse;
import com.management.demo.domain.entity.Owner;
import com.management.demo.domain.repository.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock private OwnerRepository ownerRepository;
    @InjectMocks private OwnerService ownerService;

    @Test
    void shouldCreateOwner() {
        OwnerRequest request = new OwnerRequest("Joao Silva", "51999999999", "joao@email.com");
        Owner saved = Owner.builder().id(1L).name("Joao Silva").phone("51999999999").email("joao@email.com").build();
        when(ownerRepository.save(any())).thenReturn(saved);

        OwnerResponse response = ownerService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Joao Silva");
    }

    @Test
    void findAll_withOwners_returnsListOfOwnerResponse() {
        Owner owner = Owner.builder().id(1L).name("Joao Silva").phone("51999999999").email("joao@email.com").build();
        when(ownerRepository.findAll()).thenReturn(List.of(owner));

        List<OwnerResponse> result = ownerService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Joao Silva");
    }

    @Test
    void findAll_withNoOwners_returnsEmptyList() {
        when(ownerRepository.findAll()).thenReturn(List.of());

        List<OwnerResponse> result = ownerService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowWhenOwnerNotFound() {
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ownerService.findById(99L))
                .isInstanceOf(ResponseStatusException.class);
    }
}
