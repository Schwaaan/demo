# Design: Funcionarios, Servicos e Historico de Atendimentos

**Data:** 2026-02-27
**Status:** Aprovado

---

## Contexto

Sistema de clinica veterinaria / pet shop com estrutura generica o suficiente para ser adaptada a outros negocios (agropecuaria, clinica medica, etc.) apenas renomeando entidades.

Modulo construido sobre a base existente (DDD, Spring Boot 4, JPA, H2).

---

## Modelo de Dominio

```
Owner (dono)
  - id, name, phone, email
  - auditavel (created_at, updated_at, created_by, updated_by)
       │ 1:N
       ▼
Animal (paciente)
  - id, name, species, breed, birthDate
  - ownerId (FK → Owner)
  - auditavel

ServiceType (catalogo de servicos)
  - id, name, description
  - cost (BigDecimal — preco fixo tabelado)
  - auditavel

Employee (funcionario)
  - id, name, specialty, position
  - auditavel

ServiceRecord (registro de atendimento)
  - id, date, notes
  - gainAmount (BigDecimal — valor efetivamente pago no ato)
  - animalId (FK → Animal)
  - serviceTypeId (FK → ServiceType)
  - auditavel
       │ N:N
       ▼
ServiceRecordEmployee (tabela de juncao)
  - serviceRecordId (FK)
  - employeeId (FK)
  - role (STRING — ex: "VETERINARIO", "AUXILIAR")
```

### Regras de Negocio

- `ServiceType.cost` e o preco tabelado (fixo por tipo de servico)
- `ServiceRecord.gainAmount` e o valor pago pelo cliente no ato (pode diferir do tabelado — desconto, pacote, etc.)
- `profit = gainAmount - cost`
- Um `ServiceRecord` pode ter N funcionarios (`Employee`) com papeis distintos
- Um `Owner` pode ter N animais
- Todos os dados sao auditaveis via `@EnableJpaAuditing`

---

## Arquitetura DDD

Seguindo a mesma estrutura do modulo de autenticacao:

```
src/main/java/com/management/demo/
├── domain/
│   ├── entity/
│   │   ├── Owner.java
│   │   ├── Animal.java
│   │   ├── Employee.java
│   │   ├── ServiceType.java
│   │   ├── ServiceRecord.java
│   │   └── ServiceRecordEmployee.java
│   └── repository/
│       ├── OwnerRepository.java
│       ├── AnimalRepository.java
│       ├── EmployeeRepository.java
│       ├── ServiceTypeRepository.java
│       ├── ServiceRecordRepository.java
│       └── ServiceRecordEmployeeRepository.java
├── service/
│   ├── OwnerService.java
│   ├── AnimalService.java
│   ├── EmployeeService.java
│   ├── ServiceTypeService.java
│   └── ServiceRecordService.java
└── api/
    ├── controller/
    │   ├── OwnerController.java
    │   ├── AnimalController.java
    │   ├── EmployeeController.java
    │   ├── ServiceTypeController.java
    │   └── ServiceRecordController.java
    └── dto/
        └── (requests e responses por entidade)
```

---

## Endpoints

### Owner
| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | /api/owners | Cadastrar dono |
| GET | /api/owners/{id} | Buscar dono por ID |
| GET | /api/owners/{id}/animals | Listar animais do dono |

### Animal
| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | /api/animals | Cadastrar animal |
| GET | /api/animals/{id} | Buscar animal |
| GET | /api/animals/{id}/history | Historico completo de servicos |

### Employee
| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | /api/employees | Cadastrar funcionario |
| GET | /api/employees | Listar todos |
| GET | /api/employees/{id} | Buscar por ID |

### ServiceType (catalogo)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | /api/service-types | Cadastrar tipo de servico |
| GET | /api/service-types | Listar catalogo |

### ServiceRecord (atendimento)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | /api/service-records | Registrar atendimento |
| GET | /api/service-records/{id} | Buscar atendimento com detalhes |

---

## Contratos

### POST /api/service-records — Request
```json
{
  "animalId": 1,
  "serviceTypeId": 2,
  "gainAmount": 55.00,
  "date": "2026-02-27",
  "notes": "Desconto de fidelidade",
  "employees": [
    { "employeeId": 1, "role": "VETERINARIO" },
    { "employeeId": 2, "role": "AUXILIAR" }
  ]
}
```

### GET /api/animals/{id}/history — Response
```json
{
  "animal": {
    "id": 1,
    "name": "Rex",
    "species": "Cachorro",
    "breed": "Labrador"
  },
  "owner": {
    "name": "Joao Silva",
    "phone": "51999999999",
    "email": "joao@email.com"
  },
  "records": [
    {
      "id": 10,
      "date": "2026-02-27",
      "serviceType": {
        "id": 2,
        "name": "Banho e Tosa",
        "cost": 60.00
      },
      "gainAmount": 55.00,
      "profit": -5.00,
      "employees": [
        { "id": 1, "name": "Maria", "role": "AUXILIAR" }
      ],
      "notes": "Desconto de fidelidade"
    }
  ],
  "totalCost": 60.00,
  "totalGain": 55.00,
  "totalProfit": -5.00
}
```

---

## Tabelas Geradas pelo JPA

```sql
owners, animals, employees, service_types, service_records, service_record_employees
```

Todos com campos auditaveis: `created_at`, `updated_at`, `created_by`, `updated_by`.
