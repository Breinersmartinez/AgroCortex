---
name: audita-agents-md
description: Audita la coherencia de AGENTS.md y del portal docs/ contra el árbol versionado (git ls-files) y emite una tabla de drift (doc dice / árbol tiene / corrección), con opción de aplicar correcciones a AGENTS.md solo con confirmación por fila. Usar cuando se quiera comprobar que lo que dice la documentación coincide con el repositorio, tras cambios de estructura, antes de dejar que un agente decida sobre información dudosa, o al abrir una sesión nueva en un repo que lleva tiempo sin mantenerse.
---

# audita-agents-md

Auditoría de coherencia entre la documentación de gobierno (`AGENTS.md` y el portal `docs/`) y lo
que el repositorio dice de verdad. Fuentica de verdad: **el árbol versionado** (`git ls-files`), no
el filesystem físico con sus artefactos gitignored. Este skill no corrige por iniciativa propia:
informa y, en modo corrección, aplica a `AGENTS.md` solo las filas que el usuario confirma.

## Qué audita y qué no

- **Audita:**
  - Afirmaciones de `AGENTS.md` sobre el repositorio: archivos citados, directorios, estado del
    backend/frontend/db, convenciones.
  - Coherencia del portal `docs/`: cada entrada del índice de `docs/README.md` existe, cada doc
    real del árbol está indexado, los nombres cumplen la convención (inglés kebab-case, prefijo
    numérico `0X-` por categoría) y los assets referenciados existen en `docs/assets/`.
- **No audita:** código Java/Angular, ejecutabilidad de los comandos documentados (eso implica
  compilar/testear/desplegar), workflows, ni despliegues.
- **No genera ni corrige docs:** se reporta y se sugiere la corrección; la escritura queda para
  quien sea dueño del doc. La única corrección que la skill aplica es sobre `AGENTS.md` (el
  archivo que gobierna al agente) y solo con confirmación.

## Reglas de oro (no negociables)

1. **Read-only por defecto.** El modo de ejecución base no modifica ningún archivo. Solo en el
   modo corrección y fila por fila se escribe, y cada fila requiere confirmación explícita.
2. **Fuente de verdad = `git ls-files`.** Todo archivo citado, todo directorio "vacío", toda
   ausencia/presencia se contrasta contra el árbol versionado. Un directorio con archivos
   gitignored (por ejemplo `node_modules/`) no está "poblado" para la auditoría.
3. **No se auditan artefactos gitignored.** `node_modules/`, `target/`, `coverage/`, `.angular/`,
   `uploads/`, `.idea/` y equivalentes no generan drift ni ausencia ni presencia.
4. **Nada se inventa.** Si `AGENTS.md` o el índice de docs no existen, se reporta y se pregunta;
   no se reconstruye el contenido de memoria.
5. **Override declara, no decide por el dueño.** Si un doc rompe una convención a propósito, la
   excepción se declara en `exclusiones.txt` (junto a este `SKILL.md`). Sin excepción declarada,
   la desviación se reporta y se pregunta si es excepción o drift.

## Flujo de trabajo

### Paso 0 — Confirmar modo y excepciones
Preguntar:
- ¿Modo **read-only** (por defecto) o **corrección**?
- ¿Hay excepciones declaradas? Responder las de `exclusiones.txt` y preguntar si el usuario quiere
  agregar alguna antes de auditar.

### Paso 1 — Inventario y lectura (sin escribir nada)
1. Verificar que hay repositorio git (`git ls-files` de raíz). Si no hay `.git`, abortar con aviso.
2. Leer completo: `AGENTS.md` y `docs/README.md`.
3. Listar el árbol versioneado de las áreas citadas con `git ls-files` (por ejemplo
   `git ls-files k8s/ monitoring/ backend/ docs/ db/`).
4. Anotar si el working tree tiene cambios sin commitear (`git status --porcelain`) para
   reflejarlos como nota al pie del reporte.

### Paso 2 — Comparar y construir la tabla drift
Contrastar cada afirmación y registrar cada desviación como fila con **tres columnas**:
`doc dice` / `árbol tiene` / `corrección`. Para cada fila, `doc dice` es la cita textual o
paráfrasis exacta de la fuente; `árbol tiene` es la evidencia del versionado (`git ls-files` o
contenido real); `corrección` es el único cambio que se sugiere.

Verificaciones concretas:

| # | Verificación | Cómo se comprueba |
|---|---|---|
| A1 | Cada archivo/ruta citado en `AGENTS.md` existe en el árbol | `git ls-files <ruta>` para cada mención; ausencia = drift |
| A2 | Cada afirmación de estado ("directorio vacío", "sin deps", "solo X") coincide | contenido real bajo esa ruta vía `git ls-files <ruta>`; capas/archivos extra = drift |
| A3 | El backend descrito (scaffold vs entidades/controllers) coincide con `git ls-files backend/` | comparar afirmación con los archivos de `backend/src/main/` y `backend/pom.xml` |
| D1 | Cada entrada del índice de `docs/README.md` apunta a un archivo existente | `git ls-files docs/` y resolución de cada enlace relativo |
| D2 | Cada doc real bajo `docs/` está indexado en `docs/README.md` | cruzando `git ls-files 'docs/*/*'` contra las rutas del índice; huérfanos = drift |
| D3 | Nombres de docs cumplen convención: inglés kebab-case (minúsculas, guiones, sin subrayados) y prefijo numérico por categoría y categoría en los `0X-` correctos | inspección de patrones de nombre; excepciones solo vía `exclusiones.txt` |
| D4 | Los assets referenciados en los docs existen en `docs/assets/` | resolver referencias `assets/...` de los docs contra `git ls-files docs/assets/` |

### Paso 3 — Reportar
- Agrupar por categoría: **archivos** (A), **convenciones** (C), **índices** (D).
- Empezar con el total de desviaciones y un resumen de una línea por categoría.
- Presentar la tabla drift completa con las tres columnas (`doc dice / árbol tiene / corrección`).
- Cerrar con la nota de `git status` si el árbol estaba sucio.

### Paso 4 — Modo corrección (solo si el usuario lo pidió y por fila)
1. Listar las ediciones propuestas sobre `AGENTS.md` (una por fila drift aplicable).
2. Pedir confirmación **por fila** (todas / solo las que marque / ninguna).
3. Aplicar solo las confirmadas: las ediciones tocan únicamente `AGENTS.md`, no docs.
4. Re-auditar con los pasos 1-3 y reportar el drift restante; debe quedar en 0 para las filas
   corregidas.

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| `AGENTS.md` no existe o está vacío | Drift crítico: reportarlo y preguntar si se debe crear. Crearlo queda fuera de la auditoría read-only. |
| `docs/README.md` (índice) no existe | Reportarlo y sugerir crearlo; no se crea solo y no se inventa el índice. |
| Árbol de trabajo sucio | La fuente de verdad sigue siendo `git ls-files` (estado versionado); se anota en el reporte que hay cambios sin commitear. |
| Fuera de un repositorio git | Abortar con aviso: la auditoría necesita `git ls-files` como fuente de verdad. |
| Corrección confirmada que rompe otra sección | El re-audit del paso 4 lo detecta; los cambios se aplican fila por fila, no en bloque. |
| Convención rota a propósito | Si el path está en `exclusiones.txt` no entra en la tabla; si no, se pregunta al usuario si es excepción o drift antes de reportarla. |
| Reporte muy extenso | Se agrupa por categoría y se resume el total al inicio; nunca se truncan filas de drift. |
| Directorio "vacío" que solo tiene gitignored | No es población para la auditoría: el árbol versionado manda. |

## Desviaciones conocidas de referencia (validación de la skill)

Al ejecutar sobre este repo, la skill debe detectar al menos estas (de `git ls-files` de la fecha
de creación):

1. `AGENTS.md` afirma `k8s/ y monitoring/ con configs vacías` → el árbol **no tiene `k8s/`** y
   **`monitoring/` tiene 5 archivos** reales (grafana/prometheus/graphite).
2. `AGENTS.md` describe el backend como scaffold sin deps → `backend/pom.xml` declara
   security/flyway/jjwt/springdoc y existen `V1__create_rbac.sql` y `V2__seed_rbac.sql`.
3. Existe `backend/.mvn/wrapper/maven-wrapper.properties` pero no hay `mvnw`; `AGENTS.md` dice
   "No hay `mvnw`" → la afirmación es verdadera pero la infraestructura del wrapper existe.
4. El índice de `docs/README.md` no indexa: `clean-architecture.md`, `clean-architecture-guide.md`,
   `possible-design-patterns.md` y `git-commit-prefixes.md` (ni la categoría 06).
5. `docs/assets/` tiene `modelo_Datos_mvp.jpg` y `Problem_Domain_Definition_Layer.jpeg`, nombres
   que no son kebab-case (exceptuables vía `exclusiones.txt`).

## Límites

- No se verifica que los comandos documentados (builds, tests, deploys) funcionen.
- No se reescriben `docs/` ni el índice; no se toca `main`, workflows ni despliegues.
- No se crean o borran archivos si no es la corrección confirmada de `AGENTS.md`.
- Ante cualquier duda sobre intención (¿es drift o fue a propósito?), se pregunta en vez de
  adivinar.