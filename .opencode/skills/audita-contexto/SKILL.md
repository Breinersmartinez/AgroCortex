---
name: audita-contexto
description: Verifica que el contexto que recibe el agente representa razonablemente el estado versionado real del repositorio. Audita AGENTS.md, estructura, rutas, tecnologías, convenciones y documentación contra el árbol Git. Usar cuando se quiera comprobar que lo que dice la documentación coincide con el repositorio, tras cambios de estructura, antes de dejar que un agente decida sobre información dudosa, o al abrir una sesión nueva en un repo que lleva tiempo sin mantenerse.
---

# audita-contexto

Auditoría de coherencia entre la documentación de gobierno (AGENTS.md y portal de docs/) y lo
que el repositorio dice de verdad. **Fuente de verdad: el árbol versionado** (`git ls-files`),
no el filesystem físico con sus artefactos gitignored. Este skill no corrige por iniciativa
propia: informa y, en modo corrección, aplica a AGENTS.md solo las filas que el usuario
confirma.

## Qué audita y qué no

- **Audita:**
  - Afirmaciones de AGENTS.md sobre el repositorio: archivos citados, directorios, estado de
    módulos, tecnologías mencionadas, convenciones documentadas.
  - Estructura relevante del repositorio: rutas mencionadas, archivos referenciados.
  - Coherencia del portal de docs/: cada entrada del índice existe, cada doc real del árbol
    está indexado, los nombres cumplen la convención, los assets referenciados existen.
  - Referencias a archivos inexistentes.
  - Afirmaciones contradictorias con el árbol versionado.
- **No audita:** código Java/Angular, ejecutabilidad de comandos documentados, workflows,
  ni despliegues.
- **No genera ni corrige docs:** se reporta y se sugiere la corrección; la escritura queda para
  quien sea dueño del doc. La única corrección que la skill aplica es sobre AGENTS.md (el
  archivo que gobierna al agente) y solo con confirmación.

## Reglas de oro (no negociables)

1. **Read-only por defecto.** El modo de ejecución base no modifica ningún archivo. Solo en el
   modo corrección y fila por fila se escribe, y cada fila requiere confirmación explícita.
2. **Fuente de verdad = `git ls-files`.** Todo archivo citado, todo directorio "vacío", toda
   ausencia/presencia se contrasta contra el árbol versionado. Un directorio con archivos
   gitignored no está "poblado" para la auditoría.
3. **No se auditan artefactos gitignored.** `node_modules/`, `target/`, `coverage/`, `.angular/`,
   `uploads/`, `.idea/` y equivalentes no generan drift ni ausencia ni presencia.
4. **Nada se inventa.** Si AGENTS.md o el índice de docs no existen, se reporta y se pregunta;
   no se reconstruye el contenido de memoria.
5. **Excepciones declaradas.** Si un doc rompe una convención a propósito, la excepción se
   declara en un archivo de exclusiones junto a la skill. Sin excepción declarada, la desviación
   se reporta y se pregunta si es excepción o drift.

## Diferenciación de hallazgos

La skill debe distinguir entre:

- **Drift real:** la documentación dice algo que el árbol versionado contradice.
- **Excepción intencional:** la desviación fue a propósito y está declarada (en exclusiones).
- **Información no verificable:** la afirmación no puede comprobarse con el árbol versionado
  solo (por ejemplo, "funciona" o "es rápido").

## Flujo de trabajo

### Paso 0 — Confirmar modo y excepciones
Preguntar:
- ¿Modo **read-only** (por defecto) o **corrección**?
- ¿Hay excepciones declaradas? Revisar el archivo de exclusiones y preguntar si el usuario
  quiere agregar alguna antes de auditar.

### Paso 1 — Inventario y lectura (sin escribir nada)
1. Verificar que hay repositorio git (`git ls-files` de raíz). Si no hay `.git`, abortar con aviso.
2. Leer completo: AGENTS.md y docs/README.md (si existe).
3. Listar el árbol versionado de las áreas citadas con `git ls-files`.
4. Anotar si el working tree tiene cambios sin commitear (`git status --porcelain`).

### Paso 2 — Comparar y construir la tabla drift
Contrastar cada afirmación y registrar cada desviación como fila con tres columnas:
`doc dice` / `árbol tiene` / `corrección`.

Verificaciones concretas:

| # | Verificación | Cómo se comprueba |
|---|---|---|
| A1 | Cada archivo/ruta citado en AGENTS.md existe en el árbol | `git ls-files <ruta>` para cada mención; ausencia = drift |
| A2 | Cada afirmación de estado ("vacío", "solo X") coincide | contenido real bajo esa ruta vía `git ls-files` |
| A3 | Estado descrito de módulos coincide con la realidad | comparar afirmación con archivos existentes |
| D1 | Cada entrada del índice de docs apunta a un archivo existente | `git ls-files docs/` y resolución de cada enlace |
| D2 | Cada doc real está indexado | cruzar `git ls-files 'docs/*/*'` contra el índice |
| D3 | Nombres de docs cumplen convención | inspección de patrones de nombre |
| D4 | Assets referenciados existen | resolver referencias contra `git ls-files docs/assets/` |

### Paso 3 — Reportar
- Agrupar por categoría: **archivos** (A), **documentación** (D).
- Empezar con el total de desviaciones y un resumen de una línea por categoría.
- Presentar la tabla drift completa con las tres columnas.
- Cerrar con la nota de `git status` si el árbol estaba sucio.

### Paso 4 — Modo corrección (solo si el usuario lo pidió y por fila)
1. Listar las ediciones propuestas sobre AGENTS.md (una por fila drift aplicable).
2. Pedir confirmación **por fila** (todas / solo las que marque / ninguna).
3. Aplicar solo las confirmadas: las ediciones tocan únicamente AGENTS.md, no docs.
4. Re-auditar con los pasos 1-3 y reportar el drift restante; debe quedar en 0 para las filas
   corregidas.

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| AGENTS.md no existe o está vacío | Drift crítico: reportarlo y preguntar si se debe crear. |
| docs/README.md (índice) no existe | Reportarlo y sugerir crearlo; no se crea solo. |
| Árbol de trabajo sucio | La fuente de verdad sigue siendo `git ls-files`; se anota en el reporte. |
| Fuera de un repositorio git | Abortar con aviso: la auditoría necesita `git ls-files`. |
| Corrección confirmada que rompe otra sección | El re-audit del paso 4 lo detecta; los cambios se aplican fila por fila. |
| Convención rota a propósito | Si está en exclusiones no entra en la tabla; si no, se pregunta. |
| Reporte muy extenso | Se agrupa por categoría y se resume el total al inicio; nunca se truncan filas. |
| Directorio "vacío" que solo tiene gitignored | No es población para la auditoría: el árbol versionado manda. |

## Límites

- No se verifica que los comandos documentados funcionen.
- No se reescriben docs/ ni el índice; no se toca main, workflows ni despliegues.
- No se crean o borran archivos si no es la corrección confirmada de AGENTS.md.
- Ante cualquier duda sobre intención (¿es drift o fue a propósito?), se pregunta en vez de
  adivinar.
