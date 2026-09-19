---
name: escribir-plan
description: Deriva un plan de trabajo de una especificación ya cerrada cuando falta ordenar y acotar las tareas. Produce tareas verificables; no redefine la spec ni ejecuta cambios.
---

# Escribir plan

Usa una especificación completa como fuente de verdad. El plan dice cómo y en qué orden se realizará el trabajo; no repite el qué ni agrega alcance nuevo.

## Procedimiento

1. Lee la spec completa y los archivos de contexto aplicables.
2. Comprueba que cada criterio de aceptación de la spec se pueda verificar. Pregunta únicamente lo que la spec no define y que sea indispensable para asignar, ordenar o verificar una tarea. Si falta una decisión de producto o un criterio no es verificable, devuelve el trabajo a `crear-especificacion`.
3. Identifica dependencias y deriva el menor conjunto de tareas que satisfaga los criterios de aceptación.
4. Genera `docs/PLAN.md` salvo que se solicite otra ruta.
5. Señala la primera tarea ejecutable, que no puede depender de otra, y entrega el plan sin modificar artefactos de producto.

## Formato de salida

El plan contiene:

- **Alcance del plan:** referencia a la spec y criterios que cubre.
- **Tareas:** número, resultado, responsable, dependencias, estimación y criterio Sí/No.
- **Orden y dependencias:** explicación breve de por qué ese orden.
- **Primera tarea:** indicada explícitamente.
- **Fuera de este ciclo:** alcance de la spec que se difiere y motivo.

## Reglas duras

- Cada tarea tiene un responsable real; pregunta los nombres si no están disponibles.
- Ninguna tarea dura más de media jornada (aprox. cuatro horas); divídela si la supera.
- Cada tarea tiene criterio de aceptación comprobable con Sí o No.
- No inventes tareas que no se desprendan de la spec. Si hace falta algo, pregunta.
- Si un riesgo cambia el alcance, registra el bloqueo y devuelve la decisión a la spec; no lo resuelvas creando una tarea nueva.
- El plan no implementa cambios ni afirma que algo está terminado.

## Ejemplo de activación

"Con `docs/especificacion-registro.html`, prepara el plan de implementación" activa esta skill. "Especifica el registro de un sembradío" no: allí todavía falta una spec y corresponde a `crear-especificacion`.
