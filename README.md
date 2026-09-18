# AgroCortex

Diagnóstico agronómico conversacional para pequeños agricultores: foto + descripción libre del cultivo/plaga → diagnóstico con nivel de confianza → validación humana.

![Domain definition layer](docs/assets/Problem_Domain_Definition_Layer.jpeg)

## Estado actual

Scaffold: el repositorio tiene los cimientos, no la funcionalidad del MVP.

- `backend/`: Spring Boot con dependencias de Spring Security/Flyway/JWT y migraciones RBAC `V1`/`V2`; sin controllers ni entidades de dominio.
- `frontend/`: SPA Angular sin rutas ni servicios.
- CI/CD: workflows de CI, Security, Qodana y Deploy (Heroku + Vercel).
- Documentación de arquitectura, modelo de datos y trabajo en `docs/`.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.4 · Java 17 · Maven · PostgreSQL · Flyway |
| Frontend | Angular 19 · TypeScript · Node 20 |
| CI/CD | GitHub Actions · Heroku (backend) · Vercel (frontend) |

## Estructura

```text
backend/     API Spring Boot (Maven)
frontend/    SPA Angular
docs/        Portal de documentación (ver docs/README.md)
.github/     Workflows de GitHub Actions
```

## Dónde consultar

| Tema | Fuente |
|---|---|
| Arquitectura | `docs/01-architecture/clean-architecture.md` |
| Análisis del dominio | `docs/01-architecture/agricultural-analysis.md` |
| Modelo de datos | `docs/02-data/data-model-mvp.md` |
| Esquema de la base de datos | migraciones Flyway en `backend/src/main/resources/db/migration/` |
| CI/CD y despliegue | `docs/04-operations/` |
| Seguridad | `docs/05-security/README.md` |
| Reglas para agentes | `AGENTS.md` |

## Cómo correr

Ver `AGENTS.md` (sección "Como se corre") para backend, frontend, tests y Docker.
