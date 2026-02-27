# Design: Sistema de Login e Autenticacao

**Data:** 2026-02-27
**Status:** Aprovado

---

## Contexto

Projeto Spring Boot 4.0.3 (Java 25) iniciando do zero.
Necessidade: endpoint de login e registro com JWT, tabela de usuario com dados basicos e auditoria.

---

## Stack

| Componente     | Tecnologia                          |
|----------------|-------------------------------------|
| Framework      | Spring Boot 4.0.3                   |
| Seguranca      | Spring Security                     |
| Persistencia   | Spring Data JPA + H2 (em memoria)   |
| Token          | JWT via `jjwt` (HMAC-SHA256)        |
| Hash de senha  | BCryptPasswordEncoder               |
| Auditoria      | Spring Data JPA Auditing            |
| Lombok         | Reducao de boilerplate              |

---

## Arquitetura — DDD

```
src/main/java/com/management/demo/
├── api/
│   ├── controller/
│   │   └── AuthController.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── LoginResponse.java
│       ├── RegisterRequest.java
│       └── RegisterResponse.java
│
├── domain/
│   ├── entity/
│   │   └── User.java
│   ├── repository/
│   │   └── UserRepository.java
│   └── enums/
│       └── Role.java
│
├── service/
│   ├── AuthService.java
│   └── JwtService.java
│
└── infrastructure/
    └── security/
        ├── SecurityConfig.java
        ├── JwtFilter.java
        └── UserDetailsServiceImpl.java
```

---

## Tabela de Usuario

```sql
CREATE TABLE users (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'USER',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,

    -- Auditable (gerenciado pelo Spring Data JPA Auditing)
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);
```

---

## Endpoints

### POST /api/auth/register

**Request:**
```json
{
  "username": "joao",
  "email": "joao@email.com",
  "name": "Joao Silva",
  "password": "senha123",
  "role": "USER"
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "username": "joao",
  "email": "joao@email.com",
  "name": "Joao Silva",
  "role": "USER"
}
```

**Response 409 Conflict:**
```json
{ "error": "Username or email already in use" }
```

---

### POST /api/auth/login

**Request:**
```json
{
  "username": "joao",
  "password": "senha123"
}
```

**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "joao",
  "role": "USER"
}
```

**Response 401 Unauthorized:**
```json
{ "error": "Invalid credentials" }
```

---

## Regras de Seguranca

- Endpoints `/api/auth/**` sao publicos (sem autenticacao)
- Demais endpoints exigem header `Authorization: Bearer <token>`
- Usuarios com `active = false` recebem 401 ao tentar autenticar
- Senhas armazenadas como hash BCrypt (nunca plain text)

---

## Dependencias a Adicionar no pom.xml

```xml
<!-- Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```
