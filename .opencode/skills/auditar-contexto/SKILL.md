---
name: auditar-contexto
description: Audita si el contexto versionado de AgroCortex sigue siendo coherente con el repositorio y sus fuentes de verdad. Se usa al cambiar reglas, arquitectura, datos, operación o documentación; informa hallazgos sin editar.
---

# Auditar contexto

Verifica que un agente pueda confiar en el contexto del repositorio antes de tomar decisiones. Es una auditoría de coherencia entre instrucciones, documentación y estado observable; no es una revisión de estilo ni una implementación.

## Fuentes y precedencia

1. `AGENTS.md`: reglas operativas, convenciones, restricciones y harness.
2. `docs/01-architecture/clean-architecture.md`: fuente de arquitectura.
3. `docs/02-data/data-model-mvp.md` y las migraciones Flyway: modelo y esquema operativo.
4. `docs/03-engineering/`, `docs/04-operations/` y `.github/workflows/`: ingeniería y operación.

Si dos fuentes difieren, no elige una en silencio: reporta el conflicto, indica la fuente de mayor precedencia y pide decidir o actualizar la fuente inferior.

## Procedimiento

1. Determina el alcance: cambio de arquitectura, modelo de datos, operación, harness o documentación general. Si no se indicó, audita las cuatro fuentes de verdad.
2. Lee la fuente aplicable y contrástala con archivos, estructura y configuración observables del repositorio.
3. Registra cada afirmación comprobada en una tabla con fuente, evidencia y estado: **coherente**, **desactualizada**, **contradictoria** o **no verificable**.
4. Clasifica hallazgos como bloqueantes, advertencias o informativos. Propone una corrección precisa, pero no modifica archivos.
5. Declara los límites de la auditoría: archivos no inspeccionados, servicios externos no comprobados o decisiones que requieren al dueño.

## Formato de salida

```markdown
## Alcance y fuentes revisadas

## Matriz de coherencia
| Afirmación | Fuente | Evidencia observada | Estado |

## Hallazgos
| Severidad | Conflicto o desactualización | Corrección propuesta |

## Límites y decisiones pendientes
```

## Reglas duras

- Cada hallazgo cita una ruta y una evidencia observable; no usa impresiones ni suposiciones.
- No audita ni reporta como defecto los artefactos generados o ignorados (`backend/target/`, `frontend/dist`, `coverage`, `node_modules`, `.idea`, `uploads`).
- Ignora el ruido de whitespace señalado para `AgroCortexApplication.java`.
- No cambia contexto, documentación, configuración ni código. Para aplicar una corrección se solicita una tarea posterior.
- No inventa el estado de servicios externos, secretos, despliegues o bases de datos: los declara no verificables cuando falte evidencia local.

## Ejemplo de activación

"Audita si el contexto de datos y arquitectura sigue alineado con las migraciones Flyway" activa esta skill.
