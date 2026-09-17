# Clean Architecture en AgroCortex — Guía para desarrolladores en capas tradicionales

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
