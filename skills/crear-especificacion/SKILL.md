---
name: crear-especificacion
description: Crea una especificación verificable para un incremento cuando existe una necesidad o requisito pero aún no hay una spec cerrada. Pregunta decisiones pendientes y entrega HTML autocontenido; no planifica ni implementa.
---

# Crear especificación

Convierte una petición en el acuerdo que antecede al trabajo. La salida es una especificación; no implementa, no crea un plan y no rellena ambigüedades con decisiones plausibles.

## Procedimiento

1. Recibe el requisito literalmente y consulta el contexto disponible del repositorio.
2. Identifica las decisiones que alteran alcance, comportamiento, seguridad, datos, responsables o verificación. Pregunta solo las necesarias y, cuando haya alternativas razonables, preséntalas numeradas para que la decisión sea inequívoca.
3. **No avances a la redacción** mientras una decisión material no esté definida. No sustituyas una respuesta por una suposición.
4. Redacta un único HTML autocontenido, en español, en `docs/especificacion-<tema>.html` salvo que el usuario indique otra ruta.
5. Antes de entregarlo, responde dentro de la propia revisión:
   - ¿Qué decisión se tomó sin argumentar?
   - ¿Qué sección está plana, es decir, solo describe y no decide?
   - ¿Podría implementarlo alguien que no participó en esta conversación?
6. Si una respuesta revela una carencia, vuelve a las preguntas y corrige el documento. Entrega la ruta y una lista corta de decisiones cerradas.

## Contrato del documento

Incluye exactamente estas secciones y en este orden:

1. **§1 Resumen:** qué se construye y por qué, en una respiración.
2. **§2 Objetivos:** resultados medibles, nunca adjetivos sin medida.
3. **§3 Fuera de alcance:** elementos explícitos que no se harán y su razón. Nunca queda vacío.
4. **§4 Diseño:** funcionamiento, datos y flujo. Si el flujo tiene más de tres pasos, incluye un diagrama SVG embebido y accesible.
5. **§5 Casos borde:** situación y comportamiento preciso esperado.
6. **§6 Criterios de aceptación:** cada criterio se verifica con Sí o No por una persona ajena a la conversación.
7. **§7 Decisiones:** duda, elección, alternativa descartada y porqué.

## Reglas duras

- No inventes límites, roles, políticas, plazos ni comportamientos de error. Una decisión pendiente se pregunta; nunca se entrega como una nota pendiente.
- No declares "maneja errores", "rápido" o "intuitivo" como criterios: sustitúyelos por un comportamiento comprobable.
- El HTML no carga recursos remotos ni necesita JavaScript para leerse.
- Los diagramas son SVG inline, nunca capturas ni imágenes raster.
- El conocimiento del dominio llega por el contexto y las respuestas del usuario; esta skill no depende de AgroCortex ni de un caso de clase.

## Ejemplo de activación

"Necesitamos que los agricultores puedan registrar un sembradío" activa esta skill. Antes de escribir pregunta, entre otros asuntos, quién puede hacerlo, qué campos son obligatorios y cómo se sabrá que el registro fue exitoso.
