# CLAUDE.md — demo (Spring Boot Management API)

## Regra de ouro
- NUNCA alterar código, arquivos ou executar comandos sem permissão explícita.
- Sempre mostrar a solução proposta e aguardar aprovação antes de agir.

## Stack
- Spring Boot 4.0.3 / Java 25 / Maven 3.9.9
- H2 in-memory / JWT (jjwt 0.12.6) / Lombok / Spring Security 7
- Swagger: springdoc-openapi-starter-webmvc-ui:3.0.1

## Comando Maven (obrigatório — nunca usar ./mvnw)
```bash
JAVA_HOME="/c/Users/user/.jdks/openjdk-25.0.2" \
PATH="/c/Users/user/.jdks/openjdk-25.0.2/bin:$PATH" \
/c/dev/infra/apache-maven-3.9.9/bin/mvn.cmd
```

## Arquitetura (DDD)
- Pacote base: `com.management.demo`
- `api/` → controllers + DTOs
- `domain/` → entities + repositories + enums
- `service/` → serviços de aplicação
- `infrastructure/` → security (JWT, SecurityConfig) + config (OpenAPI, DataInitializer)
- Todas as entidades estendem `Auditable` (@MappedSuperclass)
- `@EnableJpaAuditing` em `DemoApplication`

## Autenticação
- Login via `email` (não username)
- `UserDetailsServiceImpl.findByEmail()` — principal do JWT = email
- `LoginRequest`: campos `email` e `password`
- Usuário padrão: `admin / pwd@159753` (criado pelo `DataInitializer`)

## Módulos implementados
- Auth (User, Role, JWT, register/login)
- Owner → Animal (1:N)
- Employee, ServiceType
- ServiceRecord (Animal + ServiceType + N Employees via ServiceRecordEmployee)
- `GET /api/animals/{id}/history` → AnimalHistoryResponse (totalCost, totalGain, totalProfit)

## Swagger
- URL: http://localhost:8080/swagger-ui/index.html
- Auth: Bearer JWT — obter via `POST /api/auth/login`

## CORS
- Configurado via `SecurityFilterChain` + `CorsConfigurationSource`
- Propriedade: `cors.allowed-origins=http://localhost:*`

## Spring Security 7
- `DaoAuthenticationProvider(userDetailsService)` — construtor com argumento
- CSRF desabilitado, sessão STATELESS
- Rotas públicas: `/api/auth/**`, `/h2-console/**`, `/swagger-ui/**`, `/v3/api-docs/**`

## H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:demodb`
- User: `sa` / Password: (vazio)

## Pendências (identificadas em code review)

### Crítico
- [ ] **C1** `application.properties:18` — JWT secret hardcoded; mover para env var `${JWT_SECRET}`
- [ ] **C2** `AnimalService` — chama `ServiceRecordService.toResponse()` package-private; extrair mapper ou tornar `public`
- [ ] **C3** `AuthService:37` + `AuthController:27` — `Role.valueOf()` sem tratamento; retorna 409 para role inválido (deveria ser 400)
- [ ] **C4** `ServiceRecordService:43` — `request.employees()` pode ser `null` → NPE; adicionar `@NotNull` ou null-check
- [ ] **C5** `AnimalServiceTest` — faltam mocks de `ServiceRecordRepository` e `ServiceRecordService` no `@InjectMocks`

### Importante
- [ ] **I1** Zero Bean Validation (`@Valid`, `@NotBlank`, etc.) — inputs nulos chegam ao banco e geram 500
- [ ] **I2** Sem `@RestControllerAdvice` — formatos de erro inconsistentes entre endpoints
- [ ] **I3** `JwtService`/`JwtFilter` — `JwtException` não capturada; token malformado retorna 500 em vez de 401
- [ ] **I4** `cors.allowed-origins` não separado por profile (`application-dev.properties` vs `application-prod.properties`)
- [ ] **I5** `AuthController` usa `ResponseEntity<?>` sem tipo
- [ ] **I6** Faltam `findAll`, `update`, `delete` em Owner, Animal e ServiceRecord
- [ ] **I7** Acesso a repositórios inconsistente: às vezes direto no service, às vezes via outro service
- [ ] **I8** 12+ classes de teste completamente ausentes: `AuthServiceTest`, `EmployeeServiceTest`, `ServiceTypeServiceTest`, `ServiceRecordServiceTest`, `JwtServiceTest`, todos os `*ControllerTest`, `ServiceRecordRepositoryTest`
- [ ] **I9** Nomes dos testes existentes não seguem o padrão `methodName_scenario_expectedResult`

### Menor
- [ ] **M1** `pom.xml` — `spring-boot-starter-webmvc-test` não é um starter válido do Spring Boot
- [ ] **M2** `@Data` em entidades JPA gera `equals`/`hashCode` problemáticos (baseado em `id null`)
- [ ] **M3** `ServiceRecordEmployee` não estende `Auditable` (única entidade sem auditoria)
- [ ] **M4** `LoginResponse.username` armazena email — nome enganoso; renomear para `email`
- [ ] **M5** `UserDetailsServiceImpl` — usuário inativo lança `UsernameNotFoundException` em vez de `DisabledException`
- [ ] **M6** `toResponse()` em `AnimalService` e `ServiceRecordService` é package-private sem motivo; tornar `private`
- [ ] **M7** `show-sql=true` e `format_sql=true` ativos no profile padrão; mover para `application-dev.properties`
- [ ] **M8** `DemoApplicationTests` é context load sem asserções — sem valor real

## Testes
- Todo código novo ou alterado deve ter testes que cubram 100% dos cenários relevantes.
- Cenários obrigatórios por camada:
  - **Service**: caminho feliz, entidade não encontrada, violações de regra de negócio, exceções esperadas.
  - **Controller**: status HTTP correto (200, 201, 400, 401, 403, 404), body da resposta, autenticação/autorização.
  - **Repository** (quando há queries customizadas): resultado esperado e resultado vazio.
- Usar `@ExtendWith(MockitoExtension.class)` nos testes de service.
- Usar `@WebMvcTest` + `MockMvc` nos testes de controller.
- Nomear métodos de teste no padrão: `methodName_scenario_expectedResult`.
- Nenhum PR/commit deve reduzir a cobertura existente.
