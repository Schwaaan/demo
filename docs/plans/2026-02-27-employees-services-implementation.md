# Funcionarios, Servicos e Historico de Atendimentos — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implementar modulo de funcionarios, tipos de servico, animais com donos e historico financeiro de atendimentos em arquitetura DDD.

**Architecture:** Cinco modulos novos (Owner, Animal, Employee, ServiceType, ServiceRecord) seguindo o padrao DDD existente. ServiceRecord vincula Animal + ServiceType + N funcionarios via ServiceRecordEmployee (M:N). O endpoint GET /api/animals/{id}/history agrega custo, ganho e lucro. Uma classe base `Auditable` elimina repeticao dos campos de auditoria.

**Tech Stack:** Spring Boot 4.0.3, Spring Data JPA, H2, Lombok, Java 25

Comando Maven para rodar no projeto:
```bash
JAVA_HOME="/c/Users/user/.jdks/openjdk-25.0.2" PATH="/c/Users/user/.jdks/openjdk-25.0.2/bin:$PATH" /c/dev/infra/apache-maven-3.9.9/bin/mvn.cmd
```

---

## Task 1: Classe base Auditable + modulo Owner

**Files:**
- Create: `src/main/java/com/management/demo/domain/entity/Auditable.java`
- Create: `src/main/java/com/management/demo/domain/entity/Owner.java`
- Create: `src/main/java/com/management/demo/domain/repository/OwnerRepository.java`
- Create: `src/main/java/com/management/demo/api/dto/OwnerRequest.java`
- Create: `src/main/java/com/management/demo/api/dto/OwnerResponse.java`
- Create: `src/main/java/com/management/demo/service/OwnerService.java`
- Create: `src/main/java/com/management/demo/api/controller/OwnerController.java`
- Create: `src/test/java/com/management/demo/service/OwnerServiceTest.java`

**Step 1: Criar classe base Auditable**

```java
// src/main/java/com/management/demo/domain/entity/Auditable.java
package com.management.demo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
```

**Step 2: Criar entidade Owner**

```java
// src/main/java/com/management/demo/domain/entity/Owner.java
package com.management.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "owners")
public class Owner extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String email;
}
```

**Step 3: Criar OwnerRepository**

```java
// src/main/java/com/management/demo/domain/repository/OwnerRepository.java
package com.management.demo.domain.repository;

import com.management.demo.domain.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {}
```

**Step 4: Criar DTOs**

```java
// src/main/java/com/management/demo/api/dto/OwnerRequest.java
package com.management.demo.api.dto;

public record OwnerRequest(String name, String phone, String email) {}
```

```java
// src/main/java/com/management/demo/api/dto/OwnerResponse.java
package com.management.demo.api.dto;

public record OwnerResponse(Long id, String name, String phone, String email) {}
```

**Step 5: Escrever o teste do OwnerService**

```java
// src/test/java/com/management/demo/service/OwnerServiceTest.java
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
    void shouldThrowWhenOwnerNotFound() {
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.findById(99L))
                .isInstanceOf(ResponseStatusException.class);
    }
}
```

**Step 6: Rodar o teste para confirmar que falha**

```bash
<maven-cmd> test -Dtest=OwnerServiceTest -q 2>&1 | grep -E "FAIL|ERROR|BUILD"
```
Expected: FAIL — OwnerService not found

**Step 7: Criar OwnerService**

```java
// src/main/java/com/management/demo/service/OwnerService.java
package com.management.demo.service;

import com.management.demo.api.dto.OwnerRequest;
import com.management.demo.api.dto.OwnerResponse;
import com.management.demo.domain.entity.Owner;
import com.management.demo.domain.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerResponse create(OwnerRequest request) {
        Owner owner = Owner.builder()
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .build();
        Owner saved = ownerRepository.save(owner);
        return toResponse(saved);
    }

    public OwnerResponse findById(Long id) {
        return ownerRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found: " + id));
    }

    private OwnerResponse toResponse(Owner owner) {
        return new OwnerResponse(owner.getId(), owner.getName(), owner.getPhone(), owner.getEmail());
    }
}
```

**Step 8: Rodar o teste para confirmar que passa**

```bash
<maven-cmd> test -Dtest=OwnerServiceTest -q 2>&1 | grep -E "Tests run|BUILD"
```
Expected: Tests run: 2, Failures: 0 — BUILD SUCCESS

**Step 9: Criar OwnerController**

```java
// src/main/java/com/management/demo/api/controller/OwnerController.java
package com.management.demo.api.controller;

import com.management.demo.api.dto.OwnerRequest;
import com.management.demo.api.dto.OwnerResponse;
import com.management.demo.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @PostMapping
    public ResponseEntity<OwnerResponse> create(@RequestBody OwnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ownerService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OwnerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ownerService.findById(id));
    }
}
```

**Step 10: Verificar compilacao**

```bash
<maven-cmd> compile -q 2>&1 | grep -E "ERROR|BUILD"
```
Expected: BUILD SUCCESS

---

## Task 2: Modulo Animal

**Files:**
- Create: `src/main/java/com/management/demo/domain/entity/Animal.java`
- Create: `src/main/java/com/management/demo/domain/repository/AnimalRepository.java`
- Create: `src/main/java/com/management/demo/api/dto/AnimalRequest.java`
- Create: `src/main/java/com/management/demo/api/dto/AnimalResponse.java`
- Create: `src/main/java/com/management/demo/service/AnimalService.java`
- Create: `src/main/java/com/management/demo/api/controller/AnimalController.java`
- Create: `src/test/java/com/management/demo/service/AnimalServiceTest.java`

**Step 1: Criar entidade Animal**

```java
// src/main/java/com/management/demo/domain/entity/Animal.java
package com.management.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "animals")
public class Animal extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String species;

    private String breed;

    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;
}
```

**Step 2: Criar AnimalRepository**

```java
// src/main/java/com/management/demo/domain/repository/AnimalRepository.java
package com.management.demo.domain.repository;

import com.management.demo.domain.entity.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    List<Animal> findByOwnerId(Long ownerId);
}
```

**Step 3: Criar DTOs**

```java
// src/main/java/com/management/demo/api/dto/AnimalRequest.java
package com.management.demo.api.dto;

import java.time.LocalDate;

public record AnimalRequest(String name, String species, String breed, LocalDate birthDate, Long ownerId) {}
```

```java
// src/main/java/com/management/demo/api/dto/AnimalResponse.java
package com.management.demo.api.dto;

import java.time.LocalDate;

public record AnimalResponse(Long id, String name, String species, String breed, LocalDate birthDate, Long ownerId, String ownerName) {}
```

**Step 4: Escrever o teste do AnimalService**

```java
// src/test/java/com/management/demo/service/AnimalServiceTest.java
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
    }
}
```

**Step 5: Rodar o teste para confirmar que falha**

```bash
<maven-cmd> test -Dtest=AnimalServiceTest -q 2>&1 | grep -E "FAIL|ERROR|BUILD"
```

**Step 6: Criar AnimalService**

```java
// src/main/java/com/management/demo/service/AnimalService.java
package com.management.demo.service;

import com.management.demo.api.dto.AnimalRequest;
import com.management.demo.api.dto.AnimalResponse;
import com.management.demo.domain.entity.Animal;
import com.management.demo.domain.repository.AnimalRepository;
import com.management.demo.domain.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final OwnerRepository ownerRepository;

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

    public AnimalResponse findById(Long id) {
        return animalRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found: " + id));
    }

    AnimalResponse toResponse(Animal animal) {
        return new AnimalResponse(
                animal.getId(), animal.getName(), animal.getSpecies(),
                animal.getBreed(), animal.getBirthDate(),
                animal.getOwner().getId(), animal.getOwner().getName()
        );
    }
}
```

**Step 7: Rodar o teste para confirmar que passa**

```bash
<maven-cmd> test -Dtest=AnimalServiceTest -q 2>&1 | grep -E "Tests run|BUILD"
```
Expected: Tests run: 1, Failures: 0 — BUILD SUCCESS

**Step 8: Criar AnimalController** *(history endpoint sera adicionado na Task 7)*

```java
// src/main/java/com/management/demo/api/controller/AnimalController.java
package com.management.demo.api.controller;

import com.management.demo.api.dto.AnimalRequest;
import com.management.demo.api.dto.AnimalResponse;
import com.management.demo.service.AnimalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;

    @PostMapping
    public ResponseEntity<AnimalResponse> create(@RequestBody AnimalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.findById(id));
    }
}
```

**Step 9: Verificar compilacao**

```bash
<maven-cmd> compile -q 2>&1 | grep -E "ERROR|BUILD"
```
Expected: BUILD SUCCESS

---

## Task 3: Modulo Employee

**Files:**
- Create: `src/main/java/com/management/demo/domain/entity/Employee.java`
- Create: `src/main/java/com/management/demo/domain/repository/EmployeeRepository.java`
- Create: `src/main/java/com/management/demo/api/dto/EmployeeRequest.java`
- Create: `src/main/java/com/management/demo/api/dto/EmployeeResponse.java`
- Create: `src/main/java/com/management/demo/service/EmployeeService.java`
- Create: `src/main/java/com/management/demo/api/controller/EmployeeController.java`

**Step 1: Criar entidade Employee**

```java
// src/main/java/com/management/demo/domain/entity/Employee.java
package com.management.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
public class Employee extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String specialty;

    private String position;
}
```

**Step 2: Criar EmployeeRepository**

```java
// src/main/java/com/management/demo/domain/repository/EmployeeRepository.java
package com.management.demo.domain.repository;

import com.management.demo.domain.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {}
```

**Step 3: Criar DTOs**

```java
// src/main/java/com/management/demo/api/dto/EmployeeRequest.java
package com.management.demo.api.dto;

public record EmployeeRequest(String name, String specialty, String position) {}
```

```java
// src/main/java/com/management/demo/api/dto/EmployeeResponse.java
package com.management.demo.api.dto;

public record EmployeeResponse(Long id, String name, String specialty, String position) {}
```

**Step 4: Criar EmployeeService**

```java
// src/main/java/com/management/demo/service/EmployeeService.java
package com.management.demo.service;

import com.management.demo.api.dto.EmployeeRequest;
import com.management.demo.api.dto.EmployeeResponse;
import com.management.demo.domain.entity.Employee;
import com.management.demo.domain.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeResponse create(EmployeeRequest request) {
        Employee employee = Employee.builder()
                .name(request.name())
                .specialty(request.specialty())
                .position(request.position())
                .build();
        return toResponse(employeeRepository.save(employee));
    }

    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public EmployeeResponse findById(Long id) {
        return employeeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id));
    }

    private EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(e.getId(), e.getName(), e.getSpecialty(), e.getPosition());
    }
}
```

**Step 5: Criar EmployeeController**

```java
// src/main/java/com/management/demo/api/controller/EmployeeController.java
package com.management.demo.api.controller;

import com.management.demo.api.dto.EmployeeRequest;
import com.management.demo.api.dto.EmployeeResponse;
import com.management.demo.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> findAll() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }
}
```

**Step 6: Verificar compilacao**

```bash
<maven-cmd> compile -q 2>&1 | grep -E "ERROR|BUILD"
```
Expected: BUILD SUCCESS

---

## Task 4: Modulo ServiceType (catalogo)

**Files:**
- Create: `src/main/java/com/management/demo/domain/entity/ServiceType.java`
- Create: `src/main/java/com/management/demo/domain/repository/ServiceTypeRepository.java`
- Create: `src/main/java/com/management/demo/api/dto/ServiceTypeRequest.java`
- Create: `src/main/java/com/management/demo/api/dto/ServiceTypeResponse.java`
- Create: `src/main/java/com/management/demo/service/ServiceTypeService.java`
- Create: `src/main/java/com/management/demo/api/controller/ServiceTypeController.java`

**Step 1: Criar entidade ServiceType**

```java
// src/main/java/com/management/demo/domain/entity/ServiceType.java
package com.management.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_types")
public class ServiceType extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;
}
```

**Step 2: Criar ServiceTypeRepository**

```java
// src/main/java/com/management/demo/domain/repository/ServiceTypeRepository.java
package com.management.demo.domain.repository;

import com.management.demo.domain.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {}
```

**Step 3: Criar DTOs**

```java
// src/main/java/com/management/demo/api/dto/ServiceTypeRequest.java
package com.management.demo.api.dto;

import java.math.BigDecimal;

public record ServiceTypeRequest(String name, String description, BigDecimal cost) {}
```

```java
// src/main/java/com/management/demo/api/dto/ServiceTypeResponse.java
package com.management.demo.api.dto;

import java.math.BigDecimal;

public record ServiceTypeResponse(Long id, String name, String description, BigDecimal cost) {}
```

**Step 4: Criar ServiceTypeService**

```java
// src/main/java/com/management/demo/service/ServiceTypeService.java
package com.management.demo.service;

import com.management.demo.api.dto.ServiceTypeRequest;
import com.management.demo.api.dto.ServiceTypeResponse;
import com.management.demo.domain.entity.ServiceType;
import com.management.demo.domain.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceTypeResponse create(ServiceTypeRequest request) {
        ServiceType serviceType = ServiceType.builder()
                .name(request.name())
                .description(request.description())
                .cost(request.cost())
                .build();
        return toResponse(serviceTypeRepository.save(serviceType));
    }

    public List<ServiceTypeResponse> findAll() {
        return serviceTypeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ServiceType findEntityById(Long id) {
        return serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceType not found: " + id));
    }

    private ServiceTypeResponse toResponse(ServiceType st) {
        return new ServiceTypeResponse(st.getId(), st.getName(), st.getDescription(), st.getCost());
    }
}
```

**Step 5: Criar ServiceTypeController**

```java
// src/main/java/com/management/demo/api/controller/ServiceTypeController.java
package com.management.demo.api.controller;

import com.management.demo.api.dto.ServiceTypeRequest;
import com.management.demo.api.dto.ServiceTypeResponse;
import com.management.demo.service.ServiceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @PostMapping
    public ResponseEntity<ServiceTypeResponse> create(@RequestBody ServiceTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceTypeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ServiceTypeResponse>> findAll() {
        return ResponseEntity.ok(serviceTypeService.findAll());
    }
}
```

**Step 6: Verificar compilacao**

```bash
<maven-cmd> compile -q 2>&1 | grep -E "ERROR|BUILD"
```
Expected: BUILD SUCCESS

---

## Task 5: Entidades ServiceRecord e ServiceRecordEmployee

**Files:**
- Create: `src/main/java/com/management/demo/domain/entity/ServiceRecordEmployee.java`
- Create: `src/main/java/com/management/demo/domain/entity/ServiceRecord.java`
- Create: `src/main/java/com/management/demo/domain/repository/ServiceRecordRepository.java`
- Create: `src/main/java/com/management/demo/api/dto/ServiceRecordRequest.java`
- Create: `src/main/java/com/management/demo/api/dto/EmployeeInRecordRequest.java`

**Step 1: Criar ServiceRecordEmployee**

```java
// src/main/java/com/management/demo/domain/entity/ServiceRecordEmployee.java
package com.management.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_record_employees")
public class ServiceRecordEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_record_id", nullable = false)
    private ServiceRecord serviceRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String role;
}
```

**Step 2: Criar ServiceRecord**

```java
// src/main/java/com/management/demo/domain/entity/ServiceRecord.java
package com.management.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_records")
public class ServiceRecord extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    private String notes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal gainAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @Builder.Default
    @OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceRecordEmployee> employees = new ArrayList<>();
}
```

**Step 3: Criar ServiceRecordRepository**

```java
// src/main/java/com/management/demo/domain/repository/ServiceRecordRepository.java
package com.management.demo.domain.repository;

import com.management.demo.domain.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    @Query("SELECT DISTINCT sr FROM ServiceRecord sr " +
           "JOIN FETCH sr.serviceType " +
           "JOIN FETCH sr.animal a " +
           "JOIN FETCH a.owner " +
           "LEFT JOIN FETCH sr.employees sre " +
           "LEFT JOIN FETCH sre.employee " +
           "WHERE a.id = :animalId " +
           "ORDER BY sr.date DESC")
    List<ServiceRecord> findByAnimalIdWithDetails(@Param("animalId") Long animalId);
}
```

**Step 4: Criar DTOs de request**

```java
// src/main/java/com/management/demo/api/dto/EmployeeInRecordRequest.java
package com.management.demo.api.dto;

public record EmployeeInRecordRequest(Long employeeId, String role) {}
```

```java
// src/main/java/com/management/demo/api/dto/ServiceRecordRequest.java
package com.management.demo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ServiceRecordRequest(
        Long animalId,
        Long serviceTypeId,
        BigDecimal gainAmount,
        LocalDate date,
        String notes,
        List<EmployeeInRecordRequest> employees
) {}
```

**Step 5: Verificar compilacao**

```bash
<maven-cmd> compile -q 2>&1 | grep -E "ERROR|BUILD"
```
Expected: BUILD SUCCESS

---

## Task 6: ServiceRecordService e ServiceRecordController

**Files:**
- Create: `src/main/java/com/management/demo/api/dto/ServiceRecordResponse.java`
- Create: `src/main/java/com/management/demo/service/ServiceRecordService.java`
- Create: `src/main/java/com/management/demo/api/controller/ServiceRecordController.java`
- Create: `src/test/java/com/management/demo/service/ServiceRecordServiceTest.java`

**Step 1: Criar DTOs de response**

```java
// src/main/java/com/management/demo/api/dto/ServiceRecordResponse.java
package com.management.demo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ServiceRecordResponse(
        Long id,
        LocalDate date,
        String notes,
        BigDecimal gainAmount,
        BigDecimal cost,
        BigDecimal profit,
        Long animalId,
        String animalName,
        Long serviceTypeId,
        String serviceTypeName,
        List<EmployeeInRecordResponse> employees
) {}
```

```java
// src/main/java/com/management/demo/api/dto/EmployeeInRecordResponse.java
package com.management.demo.api.dto;

public record EmployeeInRecordResponse(Long id, String name, String role) {}
```

**Step 2: Escrever o teste do ServiceRecordService**

```java
// src/test/java/com/management/demo/service/ServiceRecordServiceTest.java
package com.management.demo.service;

import com.management.demo.api.dto.EmployeeInRecordRequest;
import com.management.demo.api.dto.ServiceRecordRequest;
import com.management.demo.api.dto.ServiceRecordResponse;
import com.management.demo.domain.entity.*;
import com.management.demo.domain.repository.AnimalRepository;
import com.management.demo.domain.repository.EmployeeRepository;
import com.management.demo.domain.repository.ServiceRecordRepository;
import com.management.demo.domain.repository.ServiceTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceRecordServiceTest {

    @Mock
    private ServiceRecordRepository serviceRecordRepository;
    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private ServiceTypeRepository serviceTypeRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private ServiceRecordService serviceRecordService;

    @Test
    void shouldCreateServiceRecord() {
        Owner owner = Owner.builder().id(1L).name("Joao").build();
        Animal animal = Animal.builder().id(1L).name("Rex").owner(owner).build();
        ServiceType serviceType = ServiceType.builder().id(1L).name("Banho").cost(new BigDecimal("60.00")).build();
        Employee employee = Employee.builder().id(1L).name("Maria").build();

        ServiceRecordRequest request = new ServiceRecordRequest(
                1L, 1L, new BigDecimal("55.00"), LocalDate.now(), "OK",
                List.of(new EmployeeInRecordRequest(1L, "AUXILIAR"))
        );

        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(serviceTypeRepository.findById(1L)).thenReturn(Optional.of(serviceType));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(serviceRecordRepository.save(any())).thenAnswer(inv -> {
            ServiceRecord sr = inv.getArgument(0);
            sr = ServiceRecord.builder()
                    .id(10L).date(sr.getDate()).notes(sr.getNotes())
                    .gainAmount(sr.getGainAmount()).animal(animal)
                    .serviceType(serviceType).employees(sr.getServiceRecordEmployee())
                    .build();
            return sr;
        });

        ServiceRecordResponse response = serviceRecordService.create(request);

        assertThat(response.gainAmount()).isEqualByComparingTo("55.00");
        assertThat(response.cost()).isEqualByComparingTo("60.00");
        assertThat(response.profit()).isEqualByComparingTo("-5.00");
    }
}
```

**Step 3: Rodar o teste para confirmar que falha**

```bash
<maven-cmd> test -Dtest=ServiceRecordServiceTest -q 2>&1 | grep -E "FAIL|ERROR|BUILD"
```

**Step 4: Criar ServiceRecordService**

```java
// src/main/java/com/management/demo/service/ServiceRecordService.java
package com.management.demo.service;

import com.management.demo.api.dto.*;
import com.management.demo.domain.entity.*;
import com.management.demo.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceRecordService {

    private final ServiceRecordRepository serviceRecordRepository;
    private final AnimalRepository animalRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final EmployeeRepository employeeRepository;

    public ServiceRecordResponse create(ServiceRecordRequest request) {
        Animal animal = animalRepository.findById(request.animalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found"));

        ServiceType serviceType = serviceTypeRepository.findById(request.serviceTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceType not found"));

        ServiceRecord record = ServiceRecord.builder()
                .date(request.date())
                .notes(request.notes())
                .gainAmount(request.gainAmount())
                .animal(animal)
                .serviceType(serviceType)
                .build();

        List<ServiceRecordEmployee> employeeLinks = request.employees().stream()
                .map(e -> {
                    Employee emp = employeeRepository.findById(e.employeeId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + e.employeeId()));
                    return ServiceRecordEmployee.builder()
                            .serviceRecord(record)
                            .employee(emp)
                            .role(e.role())
                            .build();
                }).toList();

        record.getServiceRecordEmployee().addAll(employeeLinks);

        ServiceRecord saved = serviceRecordRepository.save(record);
        return toResponse(saved);
    }

    ServiceRecordResponse toResponse(ServiceRecord sr) {
        var cost = sr.getServiceType().getCost();
        var profit = sr.getGainAmount().subtract(cost);

        List<EmployeeInRecordResponse> employees = sr.getServiceRecordEmployee().stream()
                .map(e -> new EmployeeInRecordResponse(
                        e.getEmployee().getId(), e.getEmployee().getName(), e.getRole()))
                .toList();

        return new ServiceRecordResponse(
                sr.getId(), sr.getDate(), sr.getNotes(), sr.getGainAmount(),
                cost, profit,
                sr.getAnimal().getId(), sr.getAnimal().getName(),
                sr.getServiceType().getId(), sr.getServiceType().getName(),
                employees
        );
    }
}
```

**Step 5: Rodar o teste para confirmar que passa**

```bash
<maven-cmd> test -Dtest=ServiceRecordServiceTest -q 2>&1 | grep -E "Tests run|BUILD"
```
Expected: Tests run: 1, Failures: 0 — BUILD SUCCESS

**Step 6: Criar ServiceRecordController**

```java
// src/main/java/com/management/demo/api/controller/ServiceRecordController.java
package com.management.demo.api.controller;

import com.management.demo.api.dto.ServiceRecordRequest;
import com.management.demo.api.dto.ServiceRecordResponse;
import com.management.demo.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-records")
@RequiredArgsConstructor
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;

    @PostMapping
    public ResponseEntity<ServiceRecordResponse> create(@RequestBody ServiceRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRecordService.create(request));
    }
}
```

**Step 7: Verificar compilacao**

```bash
<maven-cmd> compile -q 2>&1 | grep -E "ERROR|BUILD"
```
Expected: BUILD SUCCESS

---

## Task 7: Endpoint de historico do animal

**Files:**
- Create: `src/main/java/com/management/demo/api/dto/AnimalHistoryResponse.java`
- Modify: `src/main/java/com/management/demo/service/AnimalService.java` — adicionar metodo `getHistory`
- Modify: `src/main/java/com/management/demo/api/controller/AnimalController.java` — adicionar endpoint GET /{id}/history
- Create: `src/test/java/com/management/demo/service/AnimalHistoryTest.java`

**Step 1: Criar DTO de historico**

```java
// src/main/java/com/management/demo/api/dto/AnimalHistoryResponse.java
package com.management.demo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnimalHistoryResponse(
        AnimalInfo animal,
        OwnerInfo owner,
        List<ServiceRecordSummary> records,
        BigDecimal totalCost,
        BigDecimal totalGain,
        BigDecimal totalProfit
) {
    public record AnimalInfo(Long id, String name, String species, String breed) {}
    public record OwnerInfo(String name, String phone, String email) {}
    public record ServiceRecordSummary(
            Long id, LocalDate date, ServiceTypeInfo serviceType,
            BigDecimal gainAmount, BigDecimal profit,
            List<EmployeeInRecordResponse> employees, String notes
    ) {}
    public record ServiceTypeInfo(Long id, String name, BigDecimal cost) {}
}
```

**Step 2: Escrever o teste do historico**

```java
// src/test/java/com/management/demo/service/AnimalHistoryTest.java
package com.management.demo.service;

import com.management.demo.api.dto.AnimalHistoryResponse;
import com.management.demo.domain.entity.*;
import com.management.demo.domain.repository.AnimalRepository;
import com.management.demo.domain.repository.OwnerRepository;
import com.management.demo.domain.repository.ServiceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalHistoryTest {

    @Mock private AnimalRepository animalRepository;
    @Mock private OwnerRepository ownerRepository;
    @Mock private ServiceRecordRepository serviceRecordRepository;
    @InjectMocks private AnimalService animalService;

    @Test
    void shouldReturnHistoryWithTotals() {
        Owner owner = Owner.builder().id(1L).name("Joao").phone("51999").email("j@j.com").build();
        Animal animal = Animal.builder().id(1L).name("Rex").species("Cachorro").breed("Lab").owner(owner).build();
        ServiceType serviceType = ServiceType.builder().id(1L).name("Banho").cost(new BigDecimal("60.00")).build();

        ServiceRecord record = ServiceRecord.builder()
                .id(10L).date(LocalDate.now()).gainAmount(new BigDecimal("55.00"))
                .animal(animal).serviceType(serviceType).employees(List.of()).build();

        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(serviceRecordRepository.findByAnimalIdWithDetails(1L)).thenReturn(List.of(record));

        AnimalHistoryResponse history = animalService.getHistory(1L);

        assertThat(history.totalCost()).isEqualByComparingTo("60.00");
        assertThat(history.totalGain()).isEqualByComparingTo("55.00");
        assertThat(history.totalProfit()).isEqualByComparingTo("-5.00");
        assertThat(history.records()).hasSize(1);
    }
}
```

**Step 3: Rodar o teste para confirmar que falha**

```bash
<maven-cmd> test -Dtest=AnimalHistoryTest -q 2>&1 | grep -E "FAIL|ERROR|BUILD"
```

**Step 4: Adicionar getHistory ao AnimalService**

Adicionar no `AnimalService.java` — campo e metodo novos:

```java
// Adicionar no construtor (field injection via @RequiredArgsConstructor):
private final ServiceRecordRepository serviceRecordRepository;

// Adicionar metodo:
public AnimalHistoryResponse getHistory(Long animalId) {
    Animal animal = animalRepository.findById(animalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found: " + animalId));

    List<ServiceRecord> records = serviceRecordRepository.findByAnimalIdWithDetails(animalId);

    BigDecimal totalCost = records.stream()
            .map(r -> r.getServiceType().getCost())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalGain = records.stream()
            .map(ServiceRecord::getGainAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    List<AnimalHistoryResponse.ServiceRecordSummary> summaries = records.stream()
            .map(r -> new AnimalHistoryResponse.ServiceRecordSummary(
                    r.getId(), r.getDate(),
                    new AnimalHistoryResponse.ServiceTypeInfo(
                            r.getServiceType().getId(), r.getServiceType().getName(), r.getServiceType().getCost()),
                    r.getGainAmount(),
                    r.getGainAmount().subtract(r.getServiceType().getCost()),
                    r.getEmployees().stream()
                            .map(e -> new EmployeeInRecordResponse(
                                    e.getEmployee().getId(), e.getEmployee().getName(), e.getRole()))
                            .toList(),
                    r.getNotes()
            )).toList();

    return new AnimalHistoryResponse(
            new AnimalHistoryResponse.AnimalInfo(animal.getId(), animal.getName(), animal.getSpecies(), animal.getBreed()),
            new AnimalHistoryResponse.OwnerInfo(animal.getOwner().getName(), animal.getOwner().getPhone(), animal.getOwner().getEmail()),
            summaries,
            totalCost,
            totalGain,
            totalGain.subtract(totalCost)
    );
}
```

O `AnimalService` completo apos a modificacao ficara assim — adicionar os imports necessarios:
```java
import com.management.demo.api.dto.AnimalHistoryResponse;
import com.management.demo.api.dto.EmployeeInRecordResponse;
import com.management.demo.domain.entity.ServiceRecord;
import com.management.demo.domain.repository.ServiceRecordRepository;
import java.math.BigDecimal;
import java.util.List;
```

**Step 5: Adicionar endpoint no AnimalController**

```java
// Adicionar no AnimalController:
@GetMapping("/{id}/history")
public ResponseEntity<AnimalHistoryResponse> getHistory(@PathVariable Long id) {
    return ResponseEntity.ok(animalService.getHistory(id));
}
```

Adicionar import:
```java
import com.management.demo.api.dto.AnimalHistoryResponse;
```

**Step 6: Rodar o teste para confirmar que passa**

```bash
<maven-cmd> test -Dtest=AnimalHistoryTest -q 2>&1 | grep -E "Tests run|BUILD"
```
Expected: Tests run: 1, Failures: 0 — BUILD SUCCESS

**Step 7: Rodar todos os testes**

```bash
<maven-cmd> test -q 2>&1 | grep -E "Tests run|BUILD|ERROR"
```
Expected: BUILD SUCCESS — todos os testes passando

---

## Task 8: Verificacao final

**Step 1: Subir a aplicacao**

```bash
<maven-cmd> spring-boot:run
```
Expected: Started DemoApplication — porta 8080

**Step 2: Registrar um usuario e fazer login (pegar o token)**

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@email.com","name":"Admin","password":"admin123","role":"ADMIN"}'

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

**Step 3: Criar dono, animal, funcionario, tipo de servico**

```bash
curl -s -X POST http://localhost:8080/api/owners \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Joao Silva","phone":"51999999999","email":"joao@email.com"}'

curl -s -X POST http://localhost:8080/api/animals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Rex","species":"Cachorro","breed":"Labrador","ownerId":1}'

curl -s -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Dr. Carlos","specialty":"Veterinario","position":"VETERINARIO"}'

curl -s -X POST http://localhost:8080/api/service-types \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Banho e Tosa","description":"Higiene completa","cost":60.00}'
```

**Step 4: Registrar atendimento**

```bash
curl -s -X POST http://localhost:8080/api/service-records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "animalId":1,"serviceTypeId":1,"gainAmount":55.00,
    "date":"2026-02-27","notes":"Desconto de fidelidade",
    "employees":[{"employeeId":1,"role":"VETERINARIO"}]
  }'
```

**Step 5: Consultar historico do animal**

```bash
curl -s http://localhost:8080/api/animals/1/history \
  -H "Authorization: Bearer $TOKEN"
```

Expected:
```json
{
  "animal": {"id":1,"name":"Rex","species":"Cachorro","breed":"Labrador"},
  "owner": {"name":"Joao Silva","phone":"51999999999","email":"joao@email.com"},
  "records": [{
    "id":1,"date":"2026-02-27",
    "serviceType":{"id":1,"name":"Banho e Tosa","cost":60.0},
    "gainAmount":55.0,"profit":-5.0,
    "employees":[{"id":1,"name":"Dr. Carlos","role":"VETERINARIO"}],
    "notes":"Desconto de fidelidade"
  }],
  "totalCost":60.0,"totalGain":55.0,"totalProfit":-5.0
}
```
