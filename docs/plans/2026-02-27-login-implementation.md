# Login e Autenticacao Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implementar sistema de login e registro de usuarios com JWT, Spring Security, JPA e arquitetura DDD.

**Architecture:** Spring Security com UserDetailsService customizado autentica o usuario; JwtService gera e valida tokens HMAC-SHA256; Spring Data JPA Auditing preenche automaticamente os campos created_at, updated_at, created_by e updated_by.

**Tech Stack:** Spring Boot 4.0.3, Spring Security, Spring Data JPA, H2, jjwt 0.12.6, Lombok, Java 25

---

## Task 1: Adicionar dependencias no pom.xml

**Files:**
- Modify: `pom.xml`

**Step 1: Substituir o bloco `<dependencies>` no pom.xml**

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
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
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Step 2: Verificar que o build compila**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: add web, security, jpa, h2, jwt and lombok dependencies"
```

---

## Task 2: Configurar application.properties

**Files:**
- Modify: `src/main/resources/application.properties`

**Step 1: Substituir o conteudo do arquivo**

```properties
spring.application.name=demo

# H2 Database
spring.datasource.url=jdbc:h2:mem:demodb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
```

**Step 2: Commit**

```bash
git add src/main/resources/application.properties
git commit -m "config: configure H2, JPA and JWT properties"
```

---

## Task 3: Criar Role enum e entidade User com Auditable

**Files:**
- Create: `src/main/java/com/management/demo/domain/enums/Role.java`
- Create: `src/main/java/com/management/demo/domain/entity/User.java`
- Modify: `src/main/java/com/management/demo/DemoApplication.java`

**Step 1: Criar o enum Role**

```java
// src/main/java/com/management/demo/domain/enums/Role.java
package com.management.demo.domain.enums;

public enum Role {
    USER,
    ADMIN
}
```

**Step 2: Criar a entidade User**

```java
// src/main/java/com/management/demo/domain/entity/User.java
package com.management.demo.domain.entity;

import com.management.demo.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

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

**Step 3: Adicionar @EnableJpaAuditing na classe principal**

```java
// src/main/java/com/management/demo/DemoApplication.java
package com.management.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

**Step 4: Verificar compilacao**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add src/
git commit -m "feat: add User entity with auditable fields and Role enum"
```

---

## Task 4: Criar UserRepository

**Files:**
- Create: `src/main/java/com/management/demo/domain/repository/UserRepository.java`
- Create: `src/test/java/com/management/demo/domain/repository/UserRepositoryTest.java`

**Step 1: Escrever o teste**

```java
// src/test/java/com/management/demo/domain/repository/UserRepositoryTest.java
package com.management.demo.domain.repository;

import com.management.demo.domain.entity.User;
import com.management.demo.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void shouldFindByUsername() {
        User user = User.builder()
                .username("joao")
                .email("joao@email.com")
                .name("Joao Silva")
                .password("hashed")
                .role(Role.USER)
                .build();
        repository.save(user);

        Optional<User> found = repository.findByUsername("joao");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        User user = User.builder()
                .username("maria")
                .email("maria@email.com")
                .name("Maria")
                .password("hashed")
                .role(Role.USER)
                .build();
        repository.save(user);

        assertThat(repository.existsByUsername("maria")).isTrue();
        assertThat(repository.existsByEmail("maria@email.com")).isTrue();
    }
}
```

**Step 2: Rodar o teste para confirmar que falha**

```bash
./mvnw test -Dtest=UserRepositoryTest -q
```
Expected: FAIL — "UserRepository not found"

**Step 3: Criar o UserRepository**

```java
// src/main/java/com/management/demo/domain/repository/UserRepository.java
package com.management.demo.domain.repository;

import com.management.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

**Step 4: Rodar o teste para confirmar que passa**

```bash
./mvnw test -Dtest=UserRepositoryTest -q
```
Expected: BUILD SUCCESS — Tests run: 2, Failures: 0

**Step 5: Commit**

```bash
git add src/
git commit -m "feat: add UserRepository with findByUsername and existence checks"
```

---

## Task 5: Criar JwtService

**Files:**
- Create: `src/main/java/com/management/demo/service/JwtService.java`
- Create: `src/test/java/com/management/demo/service/JwtServiceTest.java`

**Step 1: Escrever o teste**

```java
// src/test/java/com/management/demo/service/JwtServiceTest.java
package com.management.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
            86400000L
        );
    }

    @Test
    void shouldGenerateAndExtractUsername() {
        String token = jwtService.generateToken("joao");

        assertThat(jwtService.extractUsername(token)).isEqualTo("joao");
    }

    @Test
    void shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken("joao");

        assertThat(jwtService.isTokenValid(token, "joao")).isTrue();
    }

    @Test
    void shouldReturnFalseForWrongUsername() {
        String token = jwtService.generateToken("joao");

        assertThat(jwtService.isTokenValid(token, "outro")).isFalse();
    }
}
```

**Step 2: Rodar o teste para confirmar que falha**

```bash
./mvnw test -Dtest=JwtServiceTest -q
```
Expected: FAIL — "JwtService not found"

**Step 3: Criar o JwtService**

```java
// src/main/java/com/management/demo/service/JwtService.java
package com.management.demo.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
```

**Step 4: Rodar o teste para confirmar que passa**

```bash
./mvnw test -Dtest=JwtServiceTest -q
```
Expected: BUILD SUCCESS — Tests run: 3, Failures: 0

**Step 5: Commit**

```bash
git add src/
git commit -m "feat: add JwtService for token generation and validation"
```

---

## Task 6: Criar UserDetailsServiceImpl e AuditorAwareImpl

**Files:**
- Create: `src/main/java/com/management/demo/infrastructure/security/UserDetailsServiceImpl.java`
- Create: `src/main/java/com/management/demo/infrastructure/security/AuditorAwareImpl.java`

**Step 1: Criar UserDetailsServiceImpl**

```java
// src/main/java/com/management/demo/infrastructure/security/UserDetailsServiceImpl.java
package com.management.demo.infrastructure.security;

import com.management.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
```

**Step 2: Criar AuditorAwareImpl**

```java
// src/main/java/com/management/demo/infrastructure/security/AuditorAwareImpl.java
package com.management.demo.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditorAwareImpl {

    @Bean(name = "auditorAware")
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }
}
```

**Step 3: Verificar compilacao**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add src/
git commit -m "feat: add UserDetailsServiceImpl and AuditorAwareImpl"
```

---

## Task 7: Criar JwtFilter e SecurityConfig

**Files:**
- Create: `src/main/java/com/management/demo/infrastructure/security/JwtFilter.java`
- Create: `src/main/java/com/management/demo/infrastructure/security/SecurityConfig.java`

**Step 1: Criar JwtFilter**

```java
// src/main/java/com/management/demo/infrastructure/security/JwtFilter.java
package com.management.demo.infrastructure.security;

import com.management.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**Step 2: Criar SecurityConfig**

```java
// src/main/java/com/management/demo/infrastructure/security/SecurityConfig.java
package com.management.demo.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Step 3: Verificar compilacao**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add src/
git commit -m "feat: add JwtFilter and SecurityConfig"
```

---

## Task 8: Criar DTOs

**Files:**
- Create: `src/main/java/com/management/demo/api/dto/LoginRequest.java`
- Create: `src/main/java/com/management/demo/api/dto/LoginResponse.java`
- Create: `src/main/java/com/management/demo/api/dto/RegisterRequest.java`
- Create: `src/main/java/com/management/demo/api/dto/RegisterResponse.java`

**Step 1: Criar LoginRequest**

```java
// src/main/java/com/management/demo/api/dto/LoginRequest.java
package com.management.demo.api.dto;

public record LoginRequest(String username, String password) {}
```

**Step 2: Criar LoginResponse**

```java
// src/main/java/com/management/demo/api/dto/LoginResponse.java
package com.management.demo.api.dto;

public record LoginResponse(String token, String type, String username, String role) {
    public LoginResponse(String token, String username, String role) {
        this(token, "Bearer", username, role);
    }
}
```

**Step 3: Criar RegisterRequest**

```java
// src/main/java/com/management/demo/api/dto/RegisterRequest.java
package com.management.demo.api.dto;

public record RegisterRequest(String username, String email, String name, String password, String role) {}
```

**Step 4: Criar RegisterResponse**

```java
// src/main/java/com/management/demo/api/dto/RegisterResponse.java
package com.management.demo.api.dto;

public record RegisterResponse(Long id, String username, String email, String name, String role) {}
```

**Step 5: Verificar compilacao**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

**Step 6: Commit**

```bash
git add src/
git commit -m "feat: add request and response DTOs for auth endpoints"
```

---

## Task 9: Criar AuthService

**Files:**
- Create: `src/main/java/com/management/demo/service/AuthService.java`
- Create: `src/test/java/com/management/demo/service/AuthServiceTest.java`

**Step 1: Escrever o teste**

```java
// src/test/java/com/management/demo/service/AuthServiceTest.java
package com.management.demo.service;

import com.management.demo.api.dto.LoginRequest;
import com.management.demo.api.dto.LoginResponse;
import com.management.demo.api.dto.RegisterRequest;
import com.management.demo.api.dto.RegisterResponse;
import com.management.demo.domain.entity.User;
import com.management.demo.domain.enums.Role;
import com.management.demo.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    @Test
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest("joao", "joao@email.com", "Joao Silva", "senha123", "USER");
        when(userRepository.existsByUsername("joao")).thenReturn(false);
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder()
                .id(1L).username(u.getUsername()).email(u.getEmail())
                .name(u.getName()).password(u.getPassword()).role(u.getRole()).active(true)
                .build();
            return u;
        });

        RegisterResponse response = authService.register(request);

        assertThat(response.username()).isEqualTo("joao");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest("joao", "joao@email.com", "Joao", "senha", "USER");
        when(userRepository.existsByUsername("joao")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username or email already in use");
    }

    @Test
    void shouldLoginAndReturnToken() {
        LoginRequest request = new LoginRequest("joao", "senha123");
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "joao", "hashed", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        when(jwtService.generateToken("joao")).thenReturn("fake-token");

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("fake-token");
        assertThat(response.username()).isEqualTo("joao");
        assertThat(response.role()).isEqualTo("ROLE_USER");
    }
}
```

**Step 2: Rodar o teste para confirmar que falha**

```bash
./mvnw test -Dtest=AuthServiceTest -q
```
Expected: FAIL — "AuthService not found"

**Step 3: Criar o AuthService**

```java
// src/main/java/com/management/demo/service/AuthService.java
package com.management.demo.service;

import com.management.demo.api.dto.LoginRequest;
import com.management.demo.api.dto.LoginResponse;
import com.management.demo.api.dto.RegisterRequest;
import com.management.demo.api.dto.RegisterResponse;
import com.management.demo.domain.entity.User;
import com.management.demo.domain.enums.Role;
import com.management.demo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username()) || userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Username or email already in use");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role().toUpperCase()))
                .active(true)
                .build();

        User saved = userRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getUsername(), saved.getEmail(),
                saved.getName(), saved.getRole().name());
    }

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        String token = jwtService.generateToken(auth.getName());
        return new LoginResponse(token, auth.getName(), role);
    }
}
```

**Step 4: Rodar o teste para confirmar que passa**

```bash
./mvnw test -Dtest=AuthServiceTest -q
```
Expected: BUILD SUCCESS — Tests run: 3, Failures: 0

**Step 5: Commit**

```bash
git add src/
git commit -m "feat: add AuthService for register and login logic"
```

---

## Task 10: Criar AuthController

**Files:**
- Create: `src/main/java/com/management/demo/api/controller/AuthController.java`
- Create: `src/test/java/com/management/demo/api/controller/AuthControllerTest.java`

**Step 1: Escrever o teste**

```java
// src/test/java/com/management/demo/api/controller/AuthControllerTest.java
package com.management.demo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.demo.api.dto.LoginRequest;
import com.management.demo.api.dto.LoginResponse;
import com.management.demo.api.dto.RegisterRequest;
import com.management.demo.api.dto.RegisterResponse;
import com.management.demo.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldRegisterUserAndReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest("joao", "joao@email.com", "Joao Silva", "senha123", "USER");
        RegisterResponse response = new RegisterResponse(1L, "joao", "joao@email.com", "Joao Silva", "USER");
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("joao"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldReturn409WhenUsernameExists() throws Exception {
        RegisterRequest request = new RegisterRequest("joao", "joao@email.com", "Joao", "senha", "USER");
        when(authService.register(any())).thenThrow(new IllegalArgumentException("Username or email already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username or email already in use"));
    }

    @Test
    void shouldLoginAndReturn200WithToken() throws Exception {
        LoginRequest request = new LoginRequest("joao", "senha123");
        LoginResponse response = new LoginResponse("fake-token", "Bearer", "joao", "ROLE_USER");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-token"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }
}
```

**Step 2: Rodar o teste para confirmar que falha**

```bash
./mvnw test -Dtest=AuthControllerTest -q
```
Expected: FAIL — "AuthController not found"

**Step 3: Criar o AuthController**

```java
// src/main/java/com/management/demo/api/controller/AuthController.java
package com.management.demo.api.controller;

import com.management.demo.api.dto.LoginRequest;
import com.management.demo.api.dto.LoginResponse;
import com.management.demo.api.dto.RegisterRequest;
import com.management.demo.api.dto.RegisterResponse;
import com.management.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }
}
```

**Step 4: Rodar o teste para confirmar que passa**

```bash
./mvnw test -Dtest=AuthControllerTest -q
```
Expected: BUILD SUCCESS — Tests run: 3, Failures: 0

**Step 5: Rodar todos os testes**

```bash
./mvnw test -q
```
Expected: BUILD SUCCESS — Todos os testes passando

**Step 6: Commit**

```bash
git add src/
git commit -m "feat: add AuthController with register and login endpoints"
```

---

## Task 11: Verificacao final — subir a aplicacao e testar manualmente

**Step 1: Subir a aplicacao**

```bash
./mvnw spring-boot:run
```
Expected: Started DemoApplication — porta 8080

**Step 2: Registrar um usuario**

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@email.com","name":"Admin","password":"admin123","role":"ADMIN"}' | jq .
```
Expected: `{"id":1,"username":"admin","email":"admin@email.com","name":"Admin","role":"ADMIN"}`

**Step 3: Fazer login**

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq .
```
Expected: `{"token":"eyJ...","type":"Bearer","username":"admin","role":"ROLE_ADMIN"}`

**Step 4: Acessar H2 Console (opcional)**

Abrir no browser: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:demodb`
- Username: `sa`
- Password: (vazio)

**Step 5: Commit final**

```bash
git add .
git commit -m "feat: complete login and registration system with JWT and DDD architecture"
```
