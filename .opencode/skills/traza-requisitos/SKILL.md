---
name: traza-requisitos
description: Construye la matriz de trazabilidad bidireccional entre los requisitos del dominio, las capabilities de arquitectura, el modelo de datos y los patrones de diseño, reportando requisitos cubiertos/parciales/sin soporte y elementos huérfanos; en modo corrección edita solo docs de modelo/capabilities con confirmación por fila y nunca el doc de requisitos. Usar cuando se quiera comprobar que el diseño cubre lo que el análisis promete y que cada pieza del diseño responde a un requisito real.
---

# traza-requisitos

Matriz de trazabilidad bidireccional entre los requisitos del dominio y el diseño del proyecto.
Este skill informa y, en modo corrección, corrige los docs de modelo/capabilities; jamás el doc
de requisitos.

## Qué traza y qué no

- **Traza la cadena:** requisitos → capabilities → modelo de datos, con los patrones de diseño
  como evidencia de cómo se implementa cada capability.
- **Granularidad:** cada restricción/regla concreta del doc de requisitos es una fila de la matriz.
- **No traza** el drift entre modelo y SQL/diagrama (eso es `audita-modelo-datos`), ni la
  coherencia de la documentación de gobierno contra el árbol (es `audita-contexto`).
- **No modifica** el doc de requisitos: las restricciones del dominio son decisiones del dueño.
  Solo puede editar docs de modelo/capabilities, con confirmación por fila.

## Reglas de oro (no negociables)

1. **Read-only por defecto.** La edición de docs de modelo/capabilities solo ocurre en modo
   corrección y por fila confirmada.
2. **Evidencia por nombre.** El soporte se cita con su nombre real (capability, entidad,
   patrón). Una afirmación sin nombre no es evidencia.
3. **No se inventa soporte.** Un requisito sin evidencia en ninguna capa es _sin soporte_ y
   se pregunta al dueño (¿va al MVP, se difiere, o era ruido?); jamás se rellena con un soporte
   plausible.
4. **El doc de requisitos es intocable.** Se lee y se traza; corregirlo sería redefinir el producto.
5. **Lo huérfano se explica o se reporta.** Un elemento sin requisito se marca "origen:
   convención/arquitectura" si la convención lo justifica; si no, es huérfano.

## Flujo de trabajo

### Paso 0 — Confirmar modo
Preguntar si es read-only (por defecto) o corrección de los docs de modelo/capabilities.

### Paso 1 — Inventario y lectura de las fuentes
Leer completos los documentos de requisitos, capabilities, modelo de datos y patrones de diseño.
Si el doc de requisitos falta o está vacío, abortar con aviso y preguntar si debe crearse
(fuera del alcance).

### Paso 2 — Extraer requisitos por regla
Descomponer cada sección del doc de requisitos en restricciones/reglas concretas. Cada regla
lleva su referencia de origen (sección/línea).

### Paso 3 — Extraer capabilities, elementos del modelo y patrones
- Capabilities del doc de arquitectura.
- Entidades/atributos del modelo de datos.
- Patrones candidatos del doc de patrones y su propósito.

### Paso 4 — Construir la matriz bidireccional
- **Forward:** para cada regla, el estado _cubierto_ (capability + elemento de modelo +
  patrón), _parcial_ (falta un eslabón, se indica cuál) o _sin soporte_ (sin evidencia).
- **Patrones:** asignar a cada capability su patrón candidato; sin patrón asignable = brecha de
  diseño.
- **Backward:** para cada capability y entidad principal, el requisito que lo origina, o la marca
  "origen: convención/arquitectura", o huérfano.

### Paso 5 — Reportar y (corrección, solo si corresponde)
1. Reporte con resumen de conteos por estado al inicio (cubierto/parcial/sin soporte) y detalle
   completo después. Tabla: `requisito / estado / evidencia (capability · modelo · patrón) /
   corrección`.
2. Las brechas de alcance se listan aparte y se formulan como pregunta al dueño.
3. Si hay corrección pedida: filas a editar en docs de modelo/capabilities, confirmación por fila,
   aplicación y re-audit.

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| Requisito con soporte parcial (falta una capa) | Estado _parcial_; la fila indica el eslabón que falta. |
| Requisito sin evidencia con nombre | Estado _sin soporte_, brecha de alcance; se pregunta al dueño en vez de inventar. |
| Capability sin patrón asignable | Fila de brecha de diseño; el patrón se decide en la doc de patrones, no aquí. |
| Elemento del modelo sin requisito | "origen: convención/arquitectura" si aplica; si no, huérfano reportado. |
| Requisito ambiguo o no verificable | Estado _no evaluable_; se pregunta si es requisito o ruido del documento. |
| Doc de requisitos ausente/vacío | Abortar con aviso y preguntar si crear; no se inventa la matriz. |
| Doc de patrones ausente | La evidencia de patrones se omite del soporte y se reporta la ausencia. |
| Matriz muy grande | Resumen de conteos al inicio; detalle completo después. |
| Corrección que rompe otra sección | El re-audit lo detecta; los cambios se aplican por fila. |

## Límites

- No se crean capabilities/entidades para cerrar brechas: la skill reporta; el diseño lo decide el dueño.
- No se definen patrones; solo se asignan como evidencia.
- No se edita nunca el doc de requisitos.
- Ante cualquier duda (brecha, huérfano, ambigüedad), se pregunta en vez de adivinar.
