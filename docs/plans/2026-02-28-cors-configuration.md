# CORS Configuration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Habilitar CORS no Spring Security para permitir chamadas do frontend Vite em `http://localhost:5173`.

**Architecture:** `CorsConfigurationSource` registrado como bean e habilitado via `.cors(...)` no `SecurityFilterChain`. A origin permitida é lida de `application.properties` via `@Value`. Preflight `OPTIONS` é resolvido pelo filtro CORS antes do `JwtFilter`.

**Tech Stack:** Spring Boot 4.0.3, Spring Security 7, JUnit 5, MockMvc, spring-security-test

---

## Comando Maven (usar sempre este)

```bash
JAVA_HOME="/c/Users/user/.jdks/openjdk-25.0.2" \
PATH="/c/Users/user/.jdks/openjdk-25.0.2/bin:$PATH" \
/c/dev/infra/apache-maven-3.9.9/bin/mvn.cmd
```

---

### Task 1: Adicionar propriedade `cors.allowed-origins` no `application.properties`

**Files:**
- Modify: `src/main/resources/application.properties`

**Step 1: Editar o arquivo**

Adicionar ao final de `src/main/resources/application.properties`:

```properties
# CORS
cors.allowed-origins=http://localhost:5173
```

**Step 2: Verificar que o arquivo ficou correto**

Abrir o arquivo e confirmar que a linha foi adicionada sem duplicatas.

**Step 3: Commit parcial**

```bash
git add src/main/resources/application.properties
git commit -m "config: add cors.allowed-origins property"
```

---

### Task 2: Escrever o teste de CORS antes de implementar

**Files:**
- Create: `src/test/java/com/management/demo/infrastructure/security/CorsConfigTest.java`

**Step 1: Criar o arquivo de teste**

```java
package com.management.demo.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightFromAllowedOriginShouldReturn200WithCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    void requestFromAllowedOriginShouldHaveCorsHeader() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void requestFromDisallowedOriginShouldNotHaveCorsHeader() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://evil.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
```

**Step 2: Rodar o teste para confirmar que falha**

```bash
JAVA_HOME="/c/Users/user/.jdks/openjdk-25.0.2" PATH="/c/Users/user/.jdks/openjdk-25.0.2/bin:$PATH" /c/dev/infra/apache-maven-3.9.9/bin/mvn.cmd -pl . test -Dtest=CorsConfigTest -q
```

Esperado: FAIL — os headers CORS não existem ainda.

**Step 3: Commit do teste**

```bash
git add src/test/java/com/management/demo/infrastructure/security/CorsConfigTest.java
git commit -m "test: add failing CORS integration tests"
```

---

### Task 3: Implementar CORS no `SecurityConfig`

**Files:**
- Modify: `src/main/java/com/management/demo/infrastructure/security/SecurityConfig.java`

**Step 1: Adicionar os imports necessários**

Acrescentar ao bloco de imports existente:

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
```

**Step 2: Adicionar o campo `@Value` após os campos existentes**

Dentro da classe `SecurityConfig`, após `private final UserDetailsServiceImpl userDetailsService;`:

```java
@Value("${cors.allowed-origins}")
private String allowedOrigins;
```

**Step 3: Habilitar CORS no `securityFilterChain`**

No método `securityFilterChain`, adicionar `.cors(...)` como **primeira** linha da cadeia, antes de `.csrf(...)`:

```java
return http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        // ... restante igual
```

**Step 4: Adicionar o bean `corsConfigurationSource`**

Adicionar novo método ao final da classe, antes do fechamento `}`:

```java
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

**Step 5: Rodar os testes para confirmar que passam**

```bash
JAVA_HOME="/c/Users/user/.jdks/openjdk-25.0.2" PATH="/c/Users/user/.jdks/openjdk-25.0.2/bin:$PATH" /c/dev/infra/apache-maven-3.9.9/bin/mvn.cmd -pl . test -Dtest=CorsConfigTest -q
```

Esperado: BUILD SUCCESS, 3 testes passando.

**Step 6: Rodar toda a suite de testes**

```bash
JAVA_HOME="/c/Users/user/.jdks/openjdk-25.0.2" PATH="/c/Users/user/.jdks/openjdk-25.0.2/bin:$PATH" /c/dev/infra/apache-maven-3.9.9/bin/mvn.cmd -pl . test -q
```

Esperado: BUILD SUCCESS, nenhum teste existente quebrado.

**Step 7: Commit da implementação**

```bash
git add src/main/java/com/management/demo/infrastructure/security/SecurityConfig.java
git commit -m "feat: configure CORS for localhost:5173 via SecurityFilterChain"
```

---

## Resultado esperado

Após a Task 3 concluída:

- Preflight `OPTIONS` de `http://localhost:5173` → `200 OK` com `Access-Control-Allow-Origin: http://localhost:5173`
- Requests normais (GET, POST, etc.) de `http://localhost:5173` → header CORS presente
- Requests de outras origens → sem header `Access-Control-Allow-Origin` (bloqueado pelo browser)
- `JwtFilter` não é acionado em preflight
- Todos os testes existentes continuam passando
