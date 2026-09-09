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
| [data-model-mvp.md](02-data/data-model-mvp.md) | Modelo de entidades MVP: espina dorsal relacional **Agricultor → Parcela → Cultivo → Consulta → Sesión → Mensaje** + diagnóstico/recomendación | Análisis agrícola (01) |
| [er/model.yaml](02-data/er/model.yaml) | Fuente única de verdad del modelo (diagram-as-code): entidades, atributos, relaciones, versión | Modelo de datos MVP (02), tool ER diagram (engineering) |
| [data-model-er.drawio](02-data/data-model-er.drawio) | Diagrama entidad-relación **generado** desde `er/model.yaml` (abrir en draw.io). No editar a mano | Modelo de datos MVP (02) |

### 03 — Engineering

| Documento | Qué es | Relacionado con |
|---|---|---|
| [github-actions-workflows.md](03-engineering/github-actions-workflows.md) | Cómo se construyeron los workflows de GitHub Actions: decisiones, alternativas descartadas, verificaciones | CI/CD (04) |
| [tools/er-diagram/README.md](../tools/er-diagram/README.md) | Estándar diagram-as-code del diagrama ER: fuente única (`er/model.yaml`), generador, validación | Modelo de datos (02), CI (04) |

### 04 — Operations

| Documento | Qué es | Relacionado con |
|---|---|---|
| [deployment.md](04-operations/deployment.md) | Despliegue manual: Heroku (Container Registry) y Vercel, con troubleshooting | CI/CD (04) |
| [ci-cd.md](04-operations/ci-cd.md) | Pipeline automatizado CI/CD: workflows, secretos, environments, branch protection | Workflows (03) · Deployment (04) |

### 05 — Security

| Documento | Qué es | Relacionado con |
|---|---|---|
| [README.md](05-security/README.md) | Postura de seguridad: qué está automatizado, qué hay que configurar | CI/CD (04) · Workflows (03) |

## Convenciones del portal

- **Nombres de archivo**: inglés, `kebab-case`, una palabra de dominio por documento (no `despliegue-heroku.pdf`).
- **Prefijo numérico** por categoría: establece el orden de lectura y evita que GitHub ordene alfabéticamente rompiendo la cronología conceptual.
- **Cada documento referencia a sus dependientes** (tabla "Relacionado con") y no duplica contenido que ya vive en otro.
- **Assets** compartidos en [`assets/`](assets/), fuera de cualquier categoría.