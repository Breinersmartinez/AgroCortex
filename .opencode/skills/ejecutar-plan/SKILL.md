---
name: ejecutar-plan
description: Ejecuta y verifica una única tarea de un plan existente cuando el usuario pide realizarla. Expone el impacto y espera confirmación antes de modificar artefactos; no crea specs ni planes.
---

# Ejecutar plan

Materializa una tarea ya definida sin ampliar el alcance. Sirve tanto para código como para documentos, configuración u otro artefacto del plan.

## Procedimiento

1. Lee el plan, la spec asociada y la primera tarea pendiente que el usuario haya indicado o que esté lista por dependencias.
2. Explica qué tarea realizará, por qué está lista, qué archivos o secciones tocará y cómo verificará el criterio.
3. **Espera confirmación explícita antes de cambiar nada.**
4. Ejecuta una sola tarea y no empieza la siguiente.
5. Verifica el resultado contra el criterio de aceptación usando evidencia observable.
6. Informa `pasó` o `no pasó`, adjunta la evidencia, identifica un hallazgo que pueda afectar el siguiente paso y dice a qué tramo volver si falló: especificación, plan o ejecución. Luego se detiene. No marca tareas como terminadas por iniciativa propia.

## Reglas duras

- No hace cambios fuera del plan. Si descubre una necesidad nueva, para y la reporta.
- No empieza una segunda tarea aunque la primera haya pasado.
- Si no puede verificar un criterio, lo declara no verificable; nunca lo declara cumplido.
- No sustituye confirmación humana por una inferencia.
- Describe "artefactos" y no presupone un lenguaje, framework o proyecto particular.
- La evidencia cita el archivo, comportamiento, salida de prueba o valor observado; nunca usa "listo" como evidencia.

## Ejemplo de activación

"Ejecuta la tarea 2 del plan de reservas" activa esta skill. Primero debe anunciar los archivos y la verificación, y esperar autorización antes de editarlos.
