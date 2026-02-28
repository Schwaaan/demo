# Design: Configuração CORS

**Data:** 2026-02-28
**Status:** Aprovado

## Contexto

A API Spring Boot não possui configuração CORS. O frontend Vite rodando em `http://localhost:5173` está recebendo erros de CORS ao fazer chamadas para `http://localhost:8080`.

## Decisão

Usar `CorsConfigurationSource` integrado ao `SecurityFilterChain` (Spring Security 7). Esta é a forma idiomática para projetos com JWT: o filtro CORS roda dentro da cadeia de segurança, garantindo que preflight requests (`OPTIONS`) sejam resolvidas antes do `JwtFilter`.

## Escopo

Apenas desenvolvimento local. Ambiente de produção não contemplado nesta iteração.

## Arquivos alterados

| Arquivo | Tipo de mudança |
|---|---|
| `src/main/java/.../infrastructure/security/SecurityConfig.java` | Adicionar `.cors(...)` no filter chain + novo bean `CorsConfigurationSource` |
| `src/main/resources/application.properties` | Adicionar propriedade `cors.allowed-origins` |

## Design detalhado

### `application.properties`

```properties
cors.allowed-origins=http://localhost:5173
```

### `SecurityConfig.java`

**No `securityFilterChain`:** adicionar `.cors(cors -> cors.configurationSource(corsConfigurationSource()))` antes do `.csrf(...)`.

**Novo bean:**

```java
@Value("${cors.allowed-origins}")
private String allowedOrigins;

@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(allowedOrigins));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

## Fluxo de request

```
Request HTTP
   │
   ▼
CorsFilter (Spring Security) ──► CorsConfigurationSource (lê allowed-origins)
   │
   ▼  preflight OPTIONS → retorna headers CORS e encerra aqui (sem passar pelo JwtFilter)
JwtFilter → AuthorizationFilter → Controller
```

## Configuração CORS resultante

| Parâmetro | Valor |
|---|---|
| `allowedOrigins` | `http://localhost:5173` (via properties) |
| `allowedMethods` | GET, POST, PUT, DELETE, OPTIONS |
| `allowedHeaders` | Authorization, Content-Type |
| `allowCredentials` | true |
| Mapeamento | `/**` |

## Alternativas descartadas

- **`@CrossOrigin` nos controllers:** espalhado, difícil de manter, risco de preflight quebrar com Spring Security.
- **`WebMvcConfigurer.addCorsMappings`:** requer habilitar CORS também no SecurityFilterChain de qualquer forma; sem ganho sobre a Opção A.
