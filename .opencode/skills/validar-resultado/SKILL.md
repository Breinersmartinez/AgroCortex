---
name: validar-resultado
description: Valida un producto, documento o cambio ya realizado contra una especificación cerrada y sus criterios de aceptación. Emite evidencia y un veredicto sin modificar el resultado.
---

# Validar resultado

Es el control posterior a la ejecución. Se usa cuando existen una spec y un resultado que se desea comprobar; no crea specs, planes ni arregla el resultado.

## Procedimiento

1. Lee la especificación completa, el producto a validar y, si existe, el plan y su evidencia de ejecución.
2. Extrae todos los criterios de aceptación tal como fueron escritos. Si la spec no los contiene o alguno no se puede interpretar como una comprobación, el veredicto es **No se puede emitir** y se devuelve a `crear-especificacion`.
3. Ejecuta o inspecciona la comprobación indicada para cada criterio. Registra evidencia concreta: ruta, comportamiento observado, salida de prueba o valor contado.
4. Clasifica cada criterio como **Sí**, **No** o **No verificable**. No omite criterios aunque parezcan obvios.
5. Revisa por separado lo que el criterio no cubre: comportamiento inesperado, alcance agregado o riesgo para un usuario. Es una observación, no un criterio nuevo.
6. Emite el veredicto y detalla el tramo al que hay que volver si hay un fallo: especificación, plan o ejecución. Se detiene.

## Formato de salida

```markdown
## Veredicto
Sirve / Sirve con reservas / No sirve / No se puede emitir — razón en una frase.

## Criterios verificados
| Criterio de la spec | Estado: Sí / No / No verificable | Evidencia |

## Fuera de los criterios
- Observación o `Sin hallazgos`.

## Siguiente corrección
- Tramo: especificación / plan / ejecución.
- Cambio concreto requerido, o `Ninguno`.
```

## Reglas duras

- Valida contra la spec, no contra preferencias nuevas ni contra una solución que parezca mejor.
- Un `Sí` sin evidencia está prohibido. `No verificable` es válido y nunca se reemplaza por un `Sí` prudente.
- No modifica artefactos, no marca tareas como completadas y no crea criterios adicionales.
- Si un criterio falla, no propone un arreglo silencioso: identifica el tramo y el cambio que debe revisarse.
- El detalle de un caso concreto vive en su spec, no en el skill: un ejemplo solo ilustra la activación, nunca se vuelve regla.

## Ejemplo de activación

"Valida `producto/reservas-laboratorio.html` contra `docs/especificacion-reserva-puestos-laboratorio.html`" activa esta skill.
