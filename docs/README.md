# Documentación — AgroCortex

Portal de documentación técnica del proyecto. Organizada por categorías funcionales (estándar enterprise). Cada documento describe un problema, una decisión o un procedimiento, y referencia a sus dependientes.

## Índice

### 01 — Architecture

| Documento | Qué es | Relacionado con |
|---|---|---|
| [agricultural-analysis.md](01-architecture/agricultural-analysis.md) | Restricciones de diseño del dominio: entrada multimodal, reglas plaga-cultivo-clima-región, tolerancia al error | Modelo de datos (02) |
| [architecture-flow.drawio](01-architecture/architecture-flow.drawio) | Diagrama de flujo de entidades del dominio (abrir en draw.io) | Modelo de datos (02) |

### 02 — Data

| Documento | Qué es | Relacionado con |
|---|---|---|
| [data-model-mvp.md](02-data/data-model-mvp.md) | Modelo de entidades MVP: espina dorsal relacional **Agricultor → Sembradio → Cultivo → Consulta → Sesión → Mensaje** + diagnóstico/recomendación | Análisis agrícola (01) |
| [data-model-er.drawio](02-data/data-model-er.drawio) | Diagrama entidad-relación del MVP (editar en draw.io) | Modelo de datos MVP (02) |

### 03 — Engineering

| Documento | Qué es | Relacionado con |
|---|---|---|
| [github-actions-workflows.md](03-engineering/github-actions-workflows.md) | Cómo se construyeron los workflows de GitHub Actions: decisiones, alternativas descartadas, verificaciones | CI/CD (04) |

### 04 — Operations

| Documento | Qué es | Relacionado con |
|---|---|---|
| [deployment.md](04-operations/deployment.md) | Despliegue manual: Heroku (`heroku.yml` + git push) y Vercel, con troubleshooting | CI/CD (04) |
| [ci-cd.md](04-operations/ci-cd.md) | Pipeline automatizado CI/CD: workflows, secretos, environments, branch protection | Workflows (03) · Deployment (04) |

### 05 — Security

| Documento | Qué es | Relacionado con |
|---|---|---|
| [README.md](05-security/README.md) | Postura de seguridad: qué está automatizado, qué hay que configurar | CI/CD (04) · Workflows (03) |

### 07 — Work

| Documento | Qué es | Relacionado con |
|---|---|---|
| [especificacion-crear-especificacion.html](07-work/especificacion-crear-especificacion.html) | Spec de la skill `crear-especificacion` | Harness (`.opencode/skills/`) |
| [especificacion-traza-requisitos.html](07-work/especificacion-traza-requisitos.html) | Spec de la skill `traza-requisitos` | Harness (`.opencode/skills/`) |
| [especificacion-audita-modelo-datos.html](07-work/especificacion-audita-modelo-datos.html) | Spec de la skill `audita-modelo-datos` | Harness (`.opencode/skills/`) |
| [especificacion-audita-contexto.html](07-work/especificacion-audita-contexto.html) | Spec de la skill `audita-contexto` | Harness (`.opencode/skills/`) |
| [especificacion-audita-pruebas.html](07-work/especificacion-audita-pruebas.html) | Spec de la skill pendiente `audita-pruebas` (no construida aún) | Harness (`.opencode/skills/`) |
| [especificacion-alinear-modelo-datos.html](07-work/especificacion-alinear-modelo-datos.html) | Encargo pendiente: migración V3 para alinear modelo RBAC | Modelo de datos (02) |
| [historico-especificacion-auditoria-vida-util.html](07-work/historico-especificacion-auditoria-vida-util.html) | Histórico: spec del orquestador `auditoria-vida-util` (retirado) | Harness (`.opencode/skills/`) |
| [historico-especificacion-formatea-commits.html](07-work/historico-especificacion-formatea-commits.html) | Histórico: spec de la skill `formatea-commits` (retirada) | Harness (`.opencode/skills/`) |
| [historico-especificacion-concuerda-docs-diagramas.html](07-work/historico-especificacion-concuerda-docs-diagramas.html) | Histórico: spec de `concuerda-docs-diagramas` (fusionada en `audita-modelo-datos`) | Harness (`.opencode/skills/`) |
| [historico-especificacion-valida-diccionario-datos.html](07-work/historico-especificacion-valida-diccionario-datos.html) | Histórico: spec de `valida-diccionario-datos` (fusionada en `audita-modelo-datos`) | Harness (`.opencode/skills/`) |
| [historico-informe-vida-util-2026-09-13.html](07-work/historico-informe-vida-util-2026-09-13.html) | Histórico: informe consolidado de la auditoría de vida útil | Harness (`.opencode/skills/`) |

## Convenciones del portal

- **Nombres de archivo**: inglés, `kebab-case`, una palabra de dominio por documento (no `despliegue-heroku.pdf`).
- **Prefijo numérico** por categoría: establece el orden de lectura y evita que GitHub ordene alfabéticamente rompiendo la cronología conceptual.
- **Cada documento referencia a sus dependientes** (tabla "Relacionado con") y no duplica contenido que ya vive en otro.
- **Assets** compartidos en [`assets/`](assets/), fuera de cualquier categoría.