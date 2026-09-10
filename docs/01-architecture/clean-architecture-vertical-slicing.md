# Clean Architecture + Vertical Slicing

## Architectural decision

AgroCortex uses a modular monolith organized by business capabilities (vertical slices) while enforcing Clean Architecture dependency rules.

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

## Vertical slices

The primary slices are based on business capabilities rather than technical layers:

- `auth`: authentication, identity and access control
- `farmers`: farmer management
- `fields`: `Sembradio` management
- `crops`: crop management
- `consultations`: agronomic consultation lifecycle
- `evidence`: image/file evidence associated with a consultation
- `conversations`: conversational sessions and messages
- `diagnoses`: probabilistic diagnosis and hypotheses
- `recommendations`: recommendations and human validation

Each slice owns its application use cases and domain rules. Cross-cutting infrastructure is kept outside the slices.

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

Controllers in vertical slices should express authorization close to the endpoint, for example:

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
12. New features should be added as vertical slices instead of creating global technical-layer folders.
