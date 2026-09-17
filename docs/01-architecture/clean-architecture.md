# Clean Architecture

## Architectural decision

AgroCortex enforces Clean Architecture dependency rules within a modular Spring Boot monolith.

The system remains a single Spring Boot deployment. Infrastructure details such as Spring MVC, JPA, PostgreSQL, JWT and external AI providers are adapters and are not part of the domain model.

## Dependency rule

```text
Presentation / Web
        |
        v
Application / Use Cases
        |
        v
Domain

Infrastructure -----> Application ports
Infrastructure -----> Domain
```

Dependencies must point toward the business rules. Domain code must not depend on Spring, JPA, HTTP, JWT libraries or external AI SDKs.

## Business capabilities

The system is organized by business capabilities:

- `auth`: authentication, identity and access control
- `farmers`: farmer management
- `fields`: `Sembradio` management
- `crops`: crop management
- `consultations`: agronomic consultation lifecycle
- `evidence`: image/file evidence associated with a consultation
- `conversations`: conversational sessions and messages
- `diagnoses`: probabilistic diagnosis and hypotheses
- `recommendations`: recommendations and human validation

Each capability owns its application use cases and domain rules. Cross-cutting infrastructure is kept outside the capabilities.

## RBAC authorization model

AgroCortex follows the same authorization semantics used in ClinCore: JWT establishes authentication, while method-level authorization uses Spring Security `@PreAuthorize` with explicit permissions.

The authorization model is:

```text
User
  |
  +---- Role 1 ----+---- Permission A
  |                +---- Permission B
  |
  +---- Role 2 ----+---- Permission C
                   +---- Permission D
```

A role is a collection of permissions. A user can have multiple active roles. At authentication time the security adapter translates the active roles and permissions into Spring Security authorities:

```text
ROLE_ADMIN
USERS_READ
USERS_UPDATE
DIAGNOSES_VALIDATE
...
```

Roles use the `ROLE_` prefix so standard Spring role checks remain available. Fine-grained permissions use their own authority names and are the preferred mechanism for module authorization.

Controllers should express authorization close to the endpoint, for example:

```java
@PreAuthorize("hasAuthority('DIAGNOSES_VALIDATE')")
public ResponseEntity<?> validateDiagnosis(...) {
    // transport concern only
}
```

Multiple permissions can be combined where a use case requires it:

```java
@PreAuthorize("hasAuthority('ROLES_READ') or hasAuthority('USERS_READ')")
```

RBAC answers **what a user is allowed to do**. It does not replace resource-level authorization. For example, `FIELDS_READ` alone does not mean that an authenticated farmer may read every field; the use case must also verify ownership or another business rule.

### AgroCortex baseline roles

The initial roles are:

| Role | Purpose |
| --- | --- |
| `ADMIN` | Full application administration and access management. |
| `AGRICULTOR` | Manage own agricultural context and create/track consultations. |
| `AGRONOMO` | Analyze consultations and validate diagnoses/recommendations. |
| `EXTENSIONISTA` | Support diagnosis and human validation workflows. |

The initial permission vocabulary follows `MODULE_ACTION`, with CRUD permissions plus explicit business actions such as `DIAGNOSES_VALIDATE` and `RECOMMENDATIONS_VALIDATE`.

## JWT authentication boundary

JWT implementation belongs exclusively to infrastructure. The application layer depends on `TokenProvider` and knows only that an access token can be generated/validated and that its subject can be extracted.

```text
Login use case
      |
      v
TokenProvider
      ^
      |
JwtTokenProvider
      |
JJWT
```

The access token contains the user identifier as its subject. Authorities are loaded from the persisted user, role and permission model when the request is authenticated rather than hard-coding authorization rules in the domain.

Access tokens are short-lived by configuration. The signing secret is supplied through environment configuration (`JWT_SECRET`) and is never committed to the repository.

## Security boundary

The following remain infrastructure concerns:

- Spring Security configuration.
- JWT filter and token parsing.
- BCrypt password hashing.
- Conversion of domain roles/permissions into `GrantedAuthority`.
- HTTP authentication/authorization error mapping.

The domain contains no `GrantedAuthority`, `SecurityContextHolder`, `HttpServletRequest`, `Jwt`, `PasswordEncoder` or JPA annotations.

## AI boundary

The AI provider is represented by an application port. The domain/application layer must describe the capability required by AgroCortex (for example, generating a diagnosis from agricultural evidence), not the vendor or SDK.

```text
Diagnosis use case
       |
       v
AgronomicDiagnosisPort
       ^
       |
OpenAI / Gemini / local model adapter
```

This allows the AI provider to change without changing the core business rules.

## Persistence boundary

Repositories are application/domain ports. JPA entities and Spring Data repositories belong to infrastructure and must not leak into domain objects.

RBAC persistence is modeled independently of the business profile:

```text
app_users
   |
   +-- app_user_roles -- app_roles
                           |
                           +-- app_role_permissions -- app_permissions
```

This keeps authentication credentials/authorization independent from the agronomic `Agricultor` aggregate, which can be linked as the business profile without coupling the security model to agricultural rules.

## Aggregate boundaries

The MVP data model identifies `Consulta` as the root of the diagnostic workflow and establishes the core chain:

```text
Agricultor -> Sembradio -> Cultivo
Agricultor -> Consulta -> Sesión -> Mensaje
Consulta -> Evidencia
Consulta -> Diagnóstico -> Hipótesis
Diagnóstico -> Recomendación -> Validación humana
```

The database model does not automatically define the domain aggregate structure. Aggregate boundaries must be selected according to transactional consistency and business invariants.

## Enterprise engineering rules

1. Use cases are the application entry points.
2. Controllers contain transport concerns only.
3. Domain objects contain business rules, not persistence annotations.
4. Infrastructure implements ports; it does not define business policy.
5. DTOs are transport contracts and are not reused as domain entities.
6. External AI calls are isolated behind ports and adapters.
7. JWT and Spring Security are isolated from business logic.
8. RBAC permissions are expressed as explicit authorities such as `CONSULTATIONS_READ` and `DIAGNOSES_VALIDATE`.
9. RBAC does not replace object-level checks such as farmer ownership of a `Sembradio` or `Consulta`.
10. Integration tests verify infrastructure adapters; unit tests focus on domain/use-case behavior.
11. Database schema changes must be versioned with migrations before production.

---

# Guía para desarrolladores en capas tradicionales

## El problema con la arquitectura en capas tradicional

En una arquitectura en capas tradicional (Presentation → Business → Data Access → Database), el flujo de dependencias es lineal y vertical. Todo depende de la capa de abajo:

```text
Controller → Service → Repository → JPA Entity → Database
```

El problema: si mañana cambias de PostgreSQL a MongoDB, o de JPA a JDBC, o de Spring Security a otra librería, tienes que tocar **todas** las capas. La lógica de negocio está **atada** al framework.

## La idea central de Clean Architecture

Clean Architecture (Robert C. Martin) invierte la regla: **las dependencias siempre apuntan hacia adentro, hacia el negocio**.

```text
Web / Controllers
       |
       v
Use Cases (aplicación)
       |
       v
Domain (negocio)

Infrastructure ──> Application ports
Infrastructure ──> Domain
```

El dominio **nunca** depende de Spring, JPA, JWT, ni de nada externo. Infrastructure implementa lo que el dominio pide.

## Comparación directa: capas tradicionales vs Clean Architecture

### Capas tradicionales (el "clásico")

```text
com.agrocortex
├── controller/        ← HTTP + lógica de negocio mezcladas
│   └── AuthController.java
├── service/           ← depende de JPA, Spring Security
│   └── AuthService.java
├── repository/        ← depende de JPA annotations
│   └── UserRepository.java
└── model/             ← tiene @Entity, @Column
    └── User.java
```

`User.java` tiene `@Entity`, `@Column`, `@GeneratedValue`. El Service usa `@Autowired`. El Repository extiende `JpaRepository`. **Todo depende de Spring.** Si quitas Spring, nada compila.

### Clean Architecture (AgroCortex)

```text
com.agrocortex
├── shared/                    ← contratos compartidos (puertos + base de dominio)
│   ├── domain/
│   │   └── AggregateRoot.java
│   └── application/ports/out/
│       ├── PasswordHasher.java
│       └── TokenProvider.java
│
└── auth/                      ← capabilities (organizado por negocio, NO por capa técnica)
    ├── domain/                ← NADA de Spring aquí
    │   ├── User.java
    │   ├── Role.java
    │   └── Permission.java
    ├── application/           ← use cases + puertos
    │   ├── login/
    │   │   ├── LoginCommand.java
    │   │   ├── LoginUseCase.java
    │   │   ├── LoginHandler.java
    │   │   ├── LoginResult.java
    │   │   └── LoginResponse.java
    │   ├── ports/out/
    │   │   └── UserRepository.java
    │   └── exception/
    │       └── InvalidCredentialsException.java
    └── infrastructure/        ← TODO de Spring está aquí
        ├── persistence/       ← JPA entities, Spring Data repos, adapters
        ├── security/          ← BCrypt, JWT, Spring Security config
        └── web/               ← Controllers, exception handlers
```

**La diferencia clave:** `domain/` no tiene **ninguna** anotación de Spring, JPA ni JWT. `infrastructure/` tiene **toda** la implementación técnica.

## Las capas explicadas con código real

### 1. Domain — Las reglas de negocio puras

```java
// auth/domain/User.java
// NO tiene @Entity, NO tiene @Column, NO importa Spring
public final class User implements AggregateRoot {
    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final Set<Role> roles;

    public User(UUID id, String email, String passwordHash, boolean active, Set<Role> roles) {
        this.id = Objects.requireNonNull(id, "User id must not be null");
        this.email = Objects.requireNonNull(email, "User email must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash must not be null");
        this.active = active;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public boolean active() { return active; }
    public Set<Role> roles() { return Collections.unmodifiableSet(roles); }
}
```

Esto es un **objeto de dominio**: inmutable, con reglas de negocio (null checks, copy inmutable), sin anotaciones de persistencia. Si quitas Spring Boot de la ecuación, **este archivo sigue compilando**.

```java
// auth/domain/Role.java — record puro, sin framework
public record Role(String code, boolean active, Set<Permission> permissions) {
    public Role {
        Objects.requireNonNull(code, "Role code must not be null");
        if (code.isBlank()) throw new IllegalArgumentException("Role code must not be blank");
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}
```

### 2. Application — Los use cases y los puertos

Los use cases orquestan la lógica. Definen **qué** se necesita (puertos), no **cómo** se hace.

```java
// auth/application/login/LoginUseCase.java — el contrato del use case
public interface LoginUseCase {
    LoginResult execute(LoginCommand command);
}

// auth/application/login/LoginCommand.java — lo que el use case recibe
public record LoginCommand(String email, String password) {}

// auth/application/login/LoginResult.java — lo que devuelve
public record LoginResult(UUID userId, String accessToken) {}
```

```java
// auth/application/login/LoginHandler.java — la implementación del use case
public final class LoginHandler implements LoginUseCase {

    private final UserRepository userRepository;      // puerto OUT (persistencia)
    private final PasswordHasher passwordHasher;        // puerto OUT (hashing)
    private final TokenProvider tokenProvider;          // puerto OUT (tokens)

    // Constructor injection — NO usa @Autowired
    public LoginHandler(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public LoginResult execute(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .filter(User::active)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        return new LoginResult(
                user.id(),
                tokenProvider.generateAccessToken(user.id())
        );
    }
}
```

**Observa:** `LoginHandler` no importa Spring, JPA, JWT ni nada. Solo usa interfaces (puertos). Si mañana cambias de BCrypt a Argon2, o de JWT a sesiones, o de PostgreSQL a MongoDB, **este archivo no cambia**.

Los puertos definen qué puede hacer la infraestructura:

```java
// shared/application/ports/out/PasswordHasher.java
public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}

// shared/application/ports/out/TokenProvider.java
public interface TokenProvider {
    String generateAccessToken(UUID subject);
    boolean isValid(String token);
    UUID extractSubject(String token);
}

// auth/application/ports/out/UserRepository.java
public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
}
```

### 3. Infrastructure — La implementación técnica

Aquí vive todo lo que depende de Spring, JPA, JWT, etc.

**Puente de JPA a dominio (el adapter más importante):**

```java
// auth/infrastructure/persistence/UserRepositoryAdapter.java
// Implementa el puerto UserRepository (definido en application)
@Component
public final class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserJpaRepository repository;

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    // Convierte JPA Entity → Domain Object
    private User toDomain(UserJpaEntity entity) {
        Set<Role> roles = entity.getRoles().stream()
                .filter(RoleJpaEntity::isActive)
                .map(role -> new Role(
                        role.getCode(),
                        true,
                        role.getPermissions().stream()
                                .filter(PermissionJpaEntity::isActive)
                                .map(p -> new Permission(p.getCode(), true))
                                .collect(Collectors.toUnmodifiableSet())
                ))
                .collect(Collectors.toUnmodifiableSet());

        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.isActive(),
                roles
        );
    }
}
```

**La entidad JPA (con todas las anotaciones):**

```java
// auth/infrastructure/persistence/UserJpaEntity.java
@Entity
@Table(name = "app_users")
public class UserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "app_user_roles", ...)
    private Set<RoleJpaEntity> roles = new HashSet<>();
}
```

**Contraste:** `UserJpaEntity` tiene `@Entity`, `@Column`, `@ManyToMany`. `User` (dominio) no tiene nada. Son mundos separados.

**La implementación de BCrypt:**

```java
// auth/infrastructure/security/BcryptPasswordHasher.java
@Component
public final class BcryptPasswordHasher implements PasswordHasher {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

**La implementación de JWT:**

```java
// auth/infrastructure/security/JwtTokenProvider.java
@Component
public final class JwtTokenProvider implements TokenProvider {
    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;
    private final String issuer;

    @Override
    public String generateAccessToken(UUID subject) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(subject.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }
}
```

**El cableado (Spring Configuration):**

```java
// auth/infrastructure/AuthConfiguration.java
@Configuration
public class AuthConfiguration {

    @Bean
    LoginUseCase loginUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider
    ) {
        return new LoginHandler(userRepository, passwordHasher, tokenProvider);
    }
}
```

Spring solo se usa **aquí**, en infrastructure, para ensamblar las piezas.

## El flujo completo de un request de login

```text
HTTP POST /auth/login {"email":"a@b.com","password":"123"}
  │
  ▼
AuthController.login()                    ← infrastructure/web/
  │  convierte LoginRequest → LoginCommand
  ▼
LoginHandler.execute(LoginCommand)        ← application/login/
  │  1. userRepository.findByEmail()      ← usa puerto (interfaz)
  │  2. passwordHasher.matches()          ← usa puerto (interfaz)
  │  3. tokenProvider.generateAccessToken() ← usa puerto (interfaz)
  │  retorna LoginResult
  ▼
AuthController recibe LoginResult
  │  convierte → LoginResponse
  ▼
HTTP 200 {"userId":"...","accessToken":"..."}
```

**Detrás del escenario (infrastructure):**

```text
userRepository.findByEmail()
  → UserRepositoryAdapter (infrastructure/persistence/)
    → SpringDataUserJpaRepository (Spring Data)
      → JPA/Hibernate
        → PostgreSQL

passwordHasher.matches()
  → BcryptPasswordHasher (infrastructure/security/)
    → BCryptPasswordEncoder (Spring Security)

tokenProvider.generateAccessToken()
  → JwtTokenProvider (infrastructure/security/)
    → JJWT library
```

## Regla fundamental: qué va en cada capa

| Capa | Contiene | NO contiene |
|---|---|---|
| **domain/** | Objetos de negocio, reglas, invariantes | @Entity, @Column, @Service, imports de Spring |
| **application/** | Use cases, puertos (interfaces), comandos/resultados, excepciones de negocio | @Autowired, @Repository, @Component |
| **infrastructure/** | Controllers, JPA entities, Spring Data repos, JWT, BCrypt, Security config | Lógica de negocio |
| **shared/** | Puertos y primitivas compartidas entre capabilities | Implementaciones |

## La pregunta que siempre surge: "¿por qué tanta complejidad?"

En un proyecto pequeño con 1 controller y 1 service, la arquitectura en capas funciona bien. Pero AgroCortex crecerá: múltiples capabilities (farmers, consultations, diagnoses, recommendations, AI), múltiples actores (agricultores, agrónomos, extensionistas), múltiples proveedores de AI.

**Clean Architecture paga su costo cuando:**
- Cambias de PostgreSQL a otro DB → solo tocas `infrastructure/persistence/`
- Cambias de JWT a OAuth2 → solo tocas `infrastructure/security/`
- Cambias de Spring Boot a otro framework → solo tocas `infrastructure/` + `AuthConfiguration`
- Cambias el proveedor de AI → solo tocas el adapter en `infrastructure/`
- Escribes tests → testeados el dominio y los use cases **sin levantar Spring**

## Testeo: la recompensa

```java
// Test del use case SIN Spring
class LoginHandlerTest {

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        // Fakes simples, sin @MockBean, sin SpringContext
        UserRepository fakeRepo = new InMemoryUserRepository();
        fakeRepo.save(new User(UUID.randomUUID(), "a@b.com", hashedPassword, true, Set.of()));

        PasswordHasher fakeHasher = new FakePasswordHasher();
        TokenProvider fakeToken = new FakeTokenProvider();

        LoginHandler handler = new LoginHandler(fakeRepo, fakeHasher, fakeToken);
        LoginResult result = handler.execute(new LoginCommand("a@b.com", "123"));

        assertNotNull(result.accessToken());
    }
}
```

Sin levantar Spring. Sin PostgreSQL. Sin JWT. Sin configuración. **Segundos** de ejecución.

## Resumen visual

```text
┌─────────────────────────────────────────────────────┐
│                    infrastructure/                    │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────┐ │
│  │   web/    │  │ security/│  │   persistence/    │ │
│  │Controller │  │JWT, BCrypt│  │JPA, SpringData   │ │
│  └────┬─────┘  └────┬─────┘  └────────┬──────────┘ │
│       │              │                  │            │
│       ▼              ▼                  ▼            │
│  ┌─────────────────────────────────────────────┐    │
│  │            application/                      │    │
│  │  LoginHandler  ←  UserRepository (puerto)   │    │
│  │                  PasswordHasher (puerto)     │    │
│  │                  TokenProvider (puerto)      │    │
│  └──────────────────────┬──────────────────────┘    │
│                         │                           │
│  ┌──────────────────────▼──────────────────────┐    │
│  │              domain/                         │    │
│  │  User, Role, Permission                      │    │
│  │  (sin un solo import de Spring)              │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  ┌──────────────────────────────────────────────┐    │
│  │              shared/                          │    │
│  │  AggregateRoot, PasswordHasher, TokenProvider │    │
│  └──────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘

Las flechas de dependencia: ────►  (siempre hacia adentro)
Infrastructure depende de Application.
Application depende de Domain.
Domain no depende de nadie.
```
