# Decisiones — Semana 4

**Equipo:** AgroCortex  
**Caso elegido:** A — aplicación HTML autónoma de reserva de puestos.

## Las tres decisiones más difíciles de la spec

1. **Identificación:** se eligió pedir un nombre visible antes de reservar, sin autenticar usuarios. Se descartó el inicio de sesión porque el reto prohíbe servidor y no lo exige.
2. **Límite de reservas:** se eligió una reserva activa por nombre y franja. Se descartaron límites diarios y sanciones porque el enunciado no los define y requerirían una política institucional.
3. **Cancelación:** se dejó fuera de este ciclo. Se descartó implementarla antes de la demo para priorizar reserva y verificación completa.

## Los skills y dónde quedaron

Las fuentes canónicas viven en `.opencode/skills/`. Las tres fueron exportadas sin cambios a `skills/` para la revisión del reto y sincronizadas con `~/.config/opencode/skills/` y `~/.agents/skills/`.

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

1. **Producto funcionando — 2 min:** abrir `producto/reservas-laboratorio.html`, reservar y recargar.
2. **Decisiones — 1 min:** identificación, límite de reservas y cancelación diferida.
3. **Cadena de skills — 2 min:** mostrar las tres descripciones, reglas duras y archivos resultantes.
4. **Control de calidad — 1 min:** mostrar la prueba del criterio no verificable y explicar la corrección si aparece un fallo.

**Plan B:** abrir el archivo local en un segundo navegador y mostrar una captura de la evidencia de verificación si la sesión de OpenCode falla.

## Lo que no se alcanzó a hacer

La cancelación está intencionalmente fuera del ciclo. Las tres pruebas de activación y la comprobación manual de reservar, bloquear y recargar deben ejecutarse y registrar su resultado real en OpenCode/navegador antes de presentar; no se deben marcar como aprobadas sin evidencia.
