package com.management.demo.infrastructure.config;

import com.management.demo.domain.entity.*;
import com.management.demo.domain.enums.Role;
import com.management.demo.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final AnimalRepository animalRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) return;

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@demo.com")
                .name("Administrador")
                .password(passwordEncoder.encode("admin"))
                .role(Role.ADMIN)
                .active(true)
                .build());

        Owner owner1 = ownerRepository.save(Owner.builder()
                .name("João Silva")
                .phone("51999990001")
                .email("joao@email.com")
                .build());

        Owner owner2 = ownerRepository.save(Owner.builder()
                .name("Maria Souza")
                .phone("51999990002")
                .email("maria@email.com")
                .build());

        Animal animal1 = animalRepository.save(Animal.builder()
                .name("Rex")
                .species("Cachorro")
                .breed("Labrador")
                .birthDate(LocalDate.of(2020, 3, 10))
                .owner(owner1)
                .build());

        Animal animal2 = animalRepository.save(Animal.builder()
                .name("Mimi")
                .species("Gato")
                .breed("Persa")
                .birthDate(LocalDate.of(2021, 7, 22))
                .owner(owner2)
                .build());

        Employee emp1 = employeeRepository.save(Employee.builder()
                .name("Dr. Carlos")
                .specialty("Clínica Geral")
                .position("Veterinário")
                .build());

        Employee emp2 = employeeRepository.save(Employee.builder()
                .name("Ana Paula")
                .specialty("Estética Animal")
                .position("Tosadora")
                .build());

        ServiceType consulta = serviceTypeRepository.save(ServiceType.builder()
                .name("Consulta Veterinária")
                .description("Consulta clínica geral")
                .cost(new BigDecimal("80.00"))
                .build());

        ServiceType banho = serviceTypeRepository.save(ServiceType.builder()
                .name("Banho e Tosa")
                .description("Higienização completa")
                .cost(new BigDecimal("60.00"))
                .build());

        ServiceRecord record1 = ServiceRecord.builder()
                .date(LocalDateTime.of(2026, 7, 10, 9, 0))
                .notes("Consulta de rotina + banho")
                .gainAmount(new BigDecimal("180.00"))
                .animal(animal1)
                .build();

        record1.getServiceRecordEmployee().add(ServiceRecordEmployee.builder()
                .serviceRecord(record1)
                .employee(emp1)
                .serviceType(consulta)
                .build());

        record1.getServiceRecordEmployee().add(ServiceRecordEmployee.builder()
                .serviceRecord(record1)
                .employee(emp2)
                .serviceType(banho)
                .build());

        serviceRecordRepository.save(record1);

        ServiceRecord record2 = ServiceRecord.builder()
                .date(LocalDateTime.of(2026, 7, 20, 14, 30))
                .notes("Consulta pós-operatória")
                .gainAmount(new BigDecimal("90.00"))
                .animal(animal2)
                .build();

        record2.getServiceRecordEmployee().add(ServiceRecordEmployee.builder()
                .serviceRecord(record2)
                .employee(emp1)
                .serviceType(consulta)
                .build());

        serviceRecordRepository.save(record2);
    }
}
