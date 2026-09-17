---
name: crear-especificacion
description: Crea especificaciones de trabajo en siete secciones fijas (Resumen, Objetivos, Fuera de alcance, Diseño, Casos borde, Criterios de aceptación, Decisiones), entregadas como un único archivo HTML autocontenido en español con diagramas en SVG. Usar cuando el usuario pida "crear una especificación", "hacer una spec", "especificar", "documentar un encargo", "definir alcance" o "dejar por escrito qué hay que construir" para cualquier tarea, feature, proyecto o trabajo.
---

# crear-especificacion

Automatiza la creación de especificaciones de trabajo. Devuelve un único archivo HTML
autocontenido, en español, con las siete secciones y diagramas en SVG. Este skill —como su
propio nombre indica— especifica: no hace el trabajo. Escribe qué hay que construir, para
quién, y cómo se sabrá que quedó bien.

## Regla de oro: no se asumen decisiones

Toda especificación asume decisiones en silencio, y justamente esas son las peligrosas.

- Si una decisión no se puede deducir del contexto y no se ha preguntado, SE PREGUNTA. Nunca
  se rellena con un valor plausible.
- No se avanza a la siguiente sección ni se entrega el documento mientras haya decisiones sin
  definir que afecten el resultado.
- Cada decisión que el usuario cierre queda registrada en §7 Decisiones, con su porqué.
- Lo que el usuario respondió en la entrevista se usa; lo que quedó sin tocar, se pregunta o
  se omite con honestidad. No se inventa.

## Flujo de trabajo

1. **Recibir la petición tal como la dijo el usuario**, sin reescribirla ni "mejorarla".
2. **Entrevista de cierre**: preguntar lo mínimo que haga falta para especificar con
   confianza, y ni una pregunta más. Averiguar:
   - Objetivo real: qué decisión o qué acción depende de este resultado.
   - Quién lo recibe: quién lo leerá, usará o calificará, y contra qué lo compara.
   - Contexto: por qué ahora, qué lo disparó, qué se intentó antes y se descartó.
   - Restricciones: con qué se cuenta, hasta cuándo, qué no se puede cambiar.
   - Preocupación: qué es lo que más dudas le genera al usuario.
   - Lo ya intentado: qué se probó y por qué no funcionó.
   Se pregunta hasta cerrar las decisiones; mientras queden abiertas, no se entrega.
3. **Redactar la especificación** siguiendo las siete secciones, usando la plantilla de
   `references/plantilla-especificacion.html`.
4. **Auto-auditar** el borrador con las tres preguntas de la sección "Tres preguntas a toda
   especificación".
5. **Entregar el HTML final**. Si la auditoría revela una decisión nueva, se vuelve al usuario
   para cerrarla y luego se actualiza el documento.

## Las siete secciones

Todas obligatorias y en este orden.

### §1 Resumen
Qué se construye y por qué, en una respiración. Si no cabe aquí, el autor no lo entendió.
No es un listado de features; es la frase que explica el resultado y el problema que resuelve.

### §2 Objetivos
Medibles, con unidad o límite numérico. "Rápido" no es un objetivo; "responde en menos de dos
segundos" sí. "Agradecible" no es un objetivo; "< el 5 % de los formularíos quedan a medias"
sí. Un objetivo que no se puede comprobar se corrige o se elimina.

### §3 Fuera de alcance
Lo que explícitamente no se va a hacer. Es la sección que evita que el proyecto crezca solo.
Nunca va vacía: si nada queda fuera, algo se está ocultando. También se anota aquí lo que se
sacrifica (calidad, plazos, partes parecidas) y el trabajo que se difiere.

### §4 Diseño
Cómo funciona el resultado. Si el flujo tiene más de tres pasos, es **obligatorio** incluir un
diagrama. El diagrama se dibuja en SVG embebido (ver "Formato de entrega"). Describe también
cómo se resuelven los pasos, no solo los dibuja.

### §5 Casos borde
Qué pasa cuando falla, cuando no hay datos, cuando el usuario hace lo raro. Cada caso escribe
el comportamiento esperado, nunca "se muestra un error". Formato: caso + comportamiento preciso.

### §6 Criterios de aceptación
La lista contra la que se verifica el resultado. Cada línea se responde con **sí o con no**,
con una comprobación que otra persona pueda ejecutar sin estar en esta conversación. Un
criterio que requiere interpretación no es criterio: se reescribe.

### §7 Decisiones
Qué se eligió y qué se descartó, cada una con su porqué. Sin el porqué, la decisión no sirve
dentro de seis meses. Formato: duda aparacida / qué se eligió / qué se descartó / por qué.

## Tres preguntas a toda especificación

Antes de entregar, la especificación debe quedar en pie frente a estas tres preguntas:

1. **¿Qué decisión estoy tomando sin argumentar?** Toda spec asume cosas; las peligrosas son
   las que asume en silencio. Buscar los supuestos implícitos y convertirlos en decisiones
   registradas en §7: si el usuario no las confirmó, vuelve a la entrevista.
2. **¿Qué sección está plana?** Las secciones que solo describen y no deciden nada son relleno.
   Cada sección o decide algo o se elimina o se fusiona. §2, §3, §6 y §7 deciden; §4 y §5 deben
   decidir comportamiento (cómo funciona, qué pasa en cada caso borde), no describir escaparates.
3. **¿Podría implementarlo alguien que no estuvo en la conversación?** Si la respuesta es no,
   falta información que solo vive en la cabeza del solicitante: se pregunta y se incorpora. La
   prueba es que un equipo distinto ejecute la spec sin tener que preguntar nada.

Si alguna de las tres falla, se vuelve al paso de entrevista antes de entregar.

## Formato de entrega

- Un único archivo HTML autocontenido: CSS embebido, sin dependencias externas, sin
  JavaScript requerido para mostrarse. Abre directo en un navegador.
- En **español**, incluidos los nombres de sección.
- Nombre del archivo: descriptivo, en kebab-case, que empiece por el tipo (por ejemplo
  `especificacion-login.html`, `spec-estabilizacion-repo.html`).
- Título del documento: `Especificación: <título corto>`.
- Cada sección precedida por su número y nombre: `§1 Resumen` … `§7 Decisiones`.
- **Diagramas en SVG embebido** (`<svg>` inline). Nunca imágenes raster, ni capturas, ni
  referencias externas a imágenes. Si un flujo tiene más de tres pasos, ese diagrama es
  obligatorio; si no llega a tres pasos, es opcional.
- La tabla de criterios de aceptación tiene columna de verificación con casillas Sí/No.
- Se usa la plantilla de `references/plantilla-especificacion.html` como base de estructura,
  estilo y accesibilidad, adaptando solo el contenido.