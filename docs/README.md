# Documentación — AgroCortex

Portal de documentación técnica del proyecto. Organizada por categorías funcionales (estándar enterprise). Cada documento describe un problema, una decisión o un procedimiento, y referencia a sus dependientes.

## Índice

### 01 — Architecture

| Documento | Qué es | Relacionado con |
|---|---|---|
| [clean-architecture.md](01-architecture/clean-architecture.md) | Fuente única de arquitectura: regla de dependencias, capabilities de negocio, RBAC/JWT e IA como puertos y adaptadores | Modelo de datos (02) |
| [agricultural-analysis.md](01-architecture/agricultural-analysis.md) | Restricciones de diseño del dominio: entrada multimodal, reglas plaga-cultivo-clima-región, tolerancia al error | Modelo de datos (02) |
| [architecture-flow.drawio](01-architecture/architecture-flow.drawio) | Diagrama de flujo de entidades del dominio (abrir en draw.io) | Modelo de datos (02) |

### 02 — Data

| Documento | Qué es | Relacionado con |
|---|---|---|
| [data-model-mvp.md](02-data/data-model-mvp.md) | Modelo de entidades MVP: espina dorsal relacional **Agricultor → Sembradio → Cultivo → Consulta → Sesión → Mensaje** + diagnóstico/recomendación | Análisis agrícola (01) |
| [data-model-er.drawio](02-data/data-model-er.drawio) | Diagrama entidad-relación del MVP (editar en draw.io) | Modelo de datos MVP (02) |
| [possible-design-patterns.md](02-data/possible-design-patterns.md) | Borrador de patrones de diseño candidatos como evidencia por capability (documento vivo, no una decisión cerrada) | Arquitectura (01) |

> El esquema operativo de la base de datos son las migraciones Flyway en `backend/src/main/resources/db/migration/`, no un diagrama ni un script suelto.

### 03 — Engineering

| Documento | Qué es | Relacionado con |
|---|---|---|
| [github-actions-workflows.md](03-engineering/github-actions-workflows.md) | Cómo se construyeron los workflows de GitHub Actions: decisiones, alternativas descartadas, verificaciones | CI/CD (04) |

### 04 — Operations

| Documento | Qué es | Relacionado con |
|---|---|---|
| [ci-cd.md](04-operations/ci-cd.md) | Pipeline automatizado CI/CD: workflows, secretos, environments, branch protection | Workflows (03) · Deployment (04) |
| [deployment.md](04-operations/deployment.md) | Despliegue manual: Heroku (`heroku.yml` + git push) y Vercel, con troubleshooting | CI/CD (04) |

### 05 — Security

| Documento | Qué es | Relacionado con |
|---|---|---|
| [README.md](05-security/README.md) | Postura de seguridad: qué está automatizado, qué hay que configurar | CI/CD (04) · Workflows (03) |

### 06 — Contribution

| Documento | Qué es | Relacionado con |
|---|---|---|
| [git-commit-prefixes.md](06-contribution/git-commit-prefixes.md) | Convención de mensajes de commit (Conventional Commits) del monorepo | AGENTS.md |

### 07 — Work

| Documento | Qué es | Relacionado con |
|---|---|---|
| [SPEC.md](SPEC.md) | Punto de entrada a la especificación HTML del reto de semana 4 | Plan (07) · Producto de reservas |
| [PLAN.md](PLAN.md) | Plan derivado de la especificación del reto | Skills de OpenCode · Producto de reservas |
| [decisiones/semana04.md](decisiones/semana04.md) | Evidencia de decisiones, pruebas y guion de la demostración | Spec (07) · Plan (07) |
| [especificacion-crear-especificacion.html](07-work/especificacion-crear-especificacion.html) | Contrato de la skill que produce specs verificables | `.opencode/skills/crear-especificacion/` |
| [especificacion-escribir-plan.html](07-work/especificacion-escribir-plan.html) | Contrato de la skill que deriva planes | `.opencode/skills/escribir-plan/` |
| [especificacion-ejecutar-plan.html](07-work/especificacion-ejecutar-plan.html) | Contrato de la skill que ejecuta una tarea con evidencia | `.opencode/skills/ejecutar-plan/` |

## Convenciones del portal

- **Nombres de archivo**: inglés, `kebab-case`, una palabra de dominio por documento (no `despliegue-heroku.pdf`).
- **Prefijo numérico** por categoría: establece el orden de lectura y evita que GitHub ordene alfabéticamente rompiendo la cronología conceptual.
- **Cada documento referencia a sus dependientes** (tabla "Relacionado con") y no duplica contenido que ya vive en otro.
- **Assets** compartidos en [`assets/`](assets/), fuera de cualquier categoría.
