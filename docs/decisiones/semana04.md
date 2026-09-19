# Decisiones — Semana 4

**Equipo:** AgroCortex  
**Caso elegido:** A — aplicación HTML autónoma de reserva de puestos.

## Las tres decisiones más difíciles de la spec

1. **Identificación:** se eligió pedir un nombre visible antes de reservar, sin autenticar usuarios. Se descartó el inicio de sesión porque el reto prohíbe servidor y no lo exige.
2. **Límite de reservas:** se eligió una reserva activa por nombre y franja. Se descartaron límites diarios y sanciones porque el enunciado no los define y requerirían una política institucional.
3. **Cancelación:** se dejó fuera de este ciclo. Se descartó implementarla antes de la demo para priorizar reserva y verificación completa.

## Los skills y dónde quedaron

Las fuentes canónicas viven en `.opencode/skills/`. Las tres fueron exportadas sin cambios a `skills/` para la revisión del reto y sincronizadas con `~/.config/opencode/skills/` y `~/.agents/skills/`.

Las skills auxiliares `validar-resultado` y `auditar-contexto` complementan el harness, pero no sustituyen la cadena de tres eslabones exigida para el reto.

## Reglas duras agregadas

- `crear-especificacion` no avanza si hay decisiones de producto relevantes sin respuesta.
- `escribir-plan` no permite una tarea mayor a cuatro horas ni sin responsable.
- `ejecutar-plan` espera confirmación antes de tocar un artefacto y no marca una tarea como cumplida si no puede verificarla.

## Pruebas de la cadena

| Skill | Prueba | Resultado | Ajuste o evidencia |
|---|---|---|---|
| Instalación | Ejecutar `opencode debug skill`. | Pasó. | OpenCode reconoció las tres skills desde `.opencode/skills/`. |
| Renderizado mínimo | Abrir el producto con Chrome headless y contar los puestos dibujados. | Pasó. | El DOM generado contiene 20 puestos. |
| crear-especificacion | Pedir una spec sin nombrar la skill. | Pendiente de ejecutar en OpenCode. | Debe activarse cuando no existe spec cerrada. |
| escribir-plan | Pedir un plan a partir de la spec sin nombrar la skill. | Pendiente de ejecutar en OpenCode. | Debe activar solo con una spec completa. |
| ejecutar-plan | Reemplazar temporalmente un criterio por uno no verificable. | Pendiente de ejecutar en OpenCode. | Debe informar que no puede verificar, no declarar éxito. |

## Guion de demo

**Modalidad:** individual · 6 minutos · mostrar artefactos y evidencias guardadas, sin generar nada en vivo.

| Tiempo | Qué se muestra | Qué se dice |
|---:|---|---|
| 0:00–0:15 | Producto abierto | Proceso: convierto un requisito ambiguo en algo verificable; el resultado es esto. |
| 0:15–1:15 | Spec §7 | Dos decisiones difíciles: identificación (nombre propio, se descartó login) y límite de reservas (una por nombre y franja, se descartaron sanciones). |
| 1:15–2:30 | Demo del producto | Reservar “Ana”, celda bloqueada, recarga con persistencia. Mínimo: 20 puestos, 7 franjas, sin backend. |
| 2:30–4:30 | Skills + spec + plan + evidencia | Cadena de tres skills, una regla dura por skill, spec con 7 secciones y fuera de alcance ≥4, `PLAN.md` con primera tarea y dependencias. Prueba del criterio no verificable con su resultado real; si falló, la corrección aplicada. |
| 4:30–5:30 | Plan o producto | Decisión defendida: cancelación fuera del ciclo; tres tareas verificadas valen más que ocho con palomita. |
| 5:30–6:00 | `validar-resultado` | Dos revisiones de solo lectura (`validar-resultado`, `auditar-contexto`) refuerzan la cadena; evidencia versionada en el repo. |

**Plan B:** si OpenCode falla, mostrar los `SKILL.md` versionados, la spec, el plan y la evidencia guardada sin intentar repararlo. Si el navegador falla, abrir `reservas-laboratorio.html` en un segundo navegador o mostrar la captura de la prueba. Un video o captura sirve de respaldo, no sustituye la demostración si el archivo puede abrirse localmente.

## Lo que no se alcanzó a hacer

La cancelación está intencionalmente fuera del ciclo. Las tres pruebas de activación y la comprobación manual de reservar, bloquear y recargar deben ejecutarse y registrar su resultado real en OpenCode/navegador antes de presentar; no se deben marcar como aprobadas sin evidencia.
