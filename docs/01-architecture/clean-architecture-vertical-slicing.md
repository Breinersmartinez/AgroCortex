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

- `auth`: authentication and session security
- `farmers`: farmer management
- `fields`: `Sembradio` management
- `crops`: crop management
- `consultations`: agronomic consultation lifecycle
- `evidence`: image/file evidence associated with a consultation
- `conversations`: conversational sessions and messages
- `diagnoses`: probabilistic diagnosis and hypotheses
- `recommendations`: recommendations and human validation

Each slice owns its application use cases and domain rules. Cross-cutting infrastructure is kept outside the slices.

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

## Security boundary

JWT, Spring Security filters and password encoders belong to infrastructure. Authentication use cases depend on abstractions such as `TokenProvider` and `PasswordHasher`.

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
7. Security mechanisms are isolated from business logic.
8. Integration tests verify infrastructure adapters; unit tests focus on domain/use-case behavior.
9. Database schema changes must be versioned with migrations before production.
10. New features should be added as vertical slices instead of creating global technical-layer folders.
