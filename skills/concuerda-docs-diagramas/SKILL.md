---
name: concuerda-docs-diagramas
description: Audita la concordancia de las tres descripciones del modelo de datos (data-model-mvp.md, data-model-er.drawio parseando su XML, y migraciones SQL) y emite una tabla de drift cruzada por pares (dice/tiene/corrección), con opción de corregir la md por fila confirmada; nunca toca el .drawio ni el SQL. Usar cuando se quiera comprobar que el documento de datos, el diagrama ER y las migraciones cuentan el mismo modelo, tras tocar cualquiera de ellos, antes de implementar sobre una versión desmentida por otra.
---

# concuerda-docs-diagramas

Auditoría de concordancia entre las tres versiones del modelo de datos del monorepo AgroCortex:
`docs/02-data/data-model-mvp.md` (fuente única de estructura), `docs/02-data/data-model-er.drawio`
(diagrama visual) y las migraciones SQL de `backend/src/main/resources/db/migration/`. Este skill
informa y, en modo corrección, corrige la md; no toca el diagrama ni el SQL.

## Qué audita y qué no

- **Audita la concordancia entre las tres fuentes**, en los tres pares posibles (md↔drawio,
  md↔migraciones, drawio↔migraciones), para estos elementos: entidades, atributos (nombre,
  PK/FK, tipo), relaciones (origen → destino con cardinalidad) y valores de enum (solo md↔SQL).
- **No audita:** cumplimiento de requisitos del dominio (eso es `traza-requisitos-modelo`),
  convenciones del diccionario como auditoría/DANE/enums en español (eso es `valida-diccionario-datos`),
  coherencia de AGENTS.md contra el árbol (eso es `audita-agents-md`), ni código.
- **No modifica:** nunca edita el `.drawio` (se mantiene a mano en draw.io) ni las migraciones
  SQL (checksums de Flyway). Solo puede editar `data-model-mvp.md`, con confirmación por fila.

## Reglas de oro (no negociables)

1. **Read-only por defecto.** En modo base no se escribe ningún archivo; la edición de la md
   requiere modo corrección y confirmación por fila.
2. **Tres vías de verdad.** Cualquier par discrepan significa drift: la md dice una cosa, el
   diagrama otra y las migraciones una tercera; las tres se contrastan entre sí.
3. **Ante conflicto md vs migraciones, priman las migraciones.** Son la verdad operativa; si la
   md las desmiente, la corrección sugerida es documentar en la md lo implementado.
4. **Entidades MVP sin migración no corrigen la md.** El SQL no escrito no es un error de la md:
   se reporta como "migración pendiente", no se inventa documentación.
5. **Nada se inventa.** Si una fuente falta o está corrupta, se reporta y se sigue con las demás;
   no se reconstruye contenido de memoria.

## Flujo de trabajo

### Paso 0 — Confirmar modo
Preguntar si es leer todo (read-only) o corregir la md (corrección). Sin modo corrección pedido,
el trabajo termina en el reporte.

### Paso 1 — Inventario y lectura de las tres fuentes
1. Localizar: `docs/02-data/data-model-mvp.md`, `docs/02-data/data-model-er.drawio`,
   `backend/src/main/resources/db/migration/*.sql`. Si alguna no existe, anotarlo como caso borde.
2. Leer la md completa (entidades, atributos, tipos, PK/FK, enums, relaciones).
3. Parsear el XML del drawio SIN abrirlo:
   - Celdas compuestas (tablas) = entidades; su `value` lleva el nombre.
   - Filas internas de cada tabla = atributos: de su `value` se extrae nombre y (si lo tiene) tipo.
   - Celdas con `edge="1"` y etiqueta = relaciones: extremos (from/to), etiqueta y cardinalidad.
   - Si el XML no parsea, reportar el error y auditar el par md↔SQL.
4. Parsear las migraciones SQL:
   - `CREATE TABLE` → entidad + columnas con tipo y PK.
   - `FOREIGN KEY` (tabla + columnas + references) → relaciones.
   - `CREATE TYPE ... AS ENUM(...)` o restricciones de check con valores → enums.

### Paso 2 — Normalizar
Construir para cada fuente un modelo con los mismos elementos:
`entidad (nombre canónico)`, `atributo (entidad, nombre, PK/FK, tipo)`,
`relación (origen, destino, cardinalidad)`, `enum (entidad, valores)`. Un elemento coincide
entre fuentes solo si su estructura coincide completa.

### Paso 3 — Comparar por pares
Para cada elemento, recorrer los tres pares y anotar cada discrepancia. Ejemplos de filas drift:
- md lista entidad que el drawio no dibuja → par md↔drawio, tipo entidad.
- migración crea tabla que la md no documenta → par md↔SQL, tipo entidad.
- mismo atributo con tipo distinto entre md y SQL → par md↔SQL, tipo tipo.
- relación sin cardinalidad en el drawio → par md↔drawio, tipo cardinalidad.

### Paso 4 — Reportar
- Agrupar por tipo de elemento (entidad, atributo, tipo, relación, cardinalidad, enum).
- Inicio con total numérico y una línea por categoría.
- Tabla drift con columnas `dice (fuente A) / tiene (fuente B) / corrección`; la corrección nombra
  la fuente a tocar (md, o "migración pendiente", o "revisar en draw.io").
- Nota final: `git status` del repo y si el árbol estaba sucio.

### Paso 5 — Modo corrección (solo md, fila por fila, confirmado)
1. Listar las filas que se corregirían en `data-model-mvp.md` (nunca en drawio ni SQL).
2. Pedir confirmación por fila (todas / las marcadas / ninguna).
3. Aplicar solo las confirmadas y re-auditar con los pasos 1-4; el drift de las filas corregidas
   debe quedar en 0.

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| `.drawio` ausente o sin celdas con `value` | Drift "sin diagrama": se reporta y se pregunta si debe crearse (fuera del alcance read-only). |
| XML del `.drawio` corrupto | Se reporta el error de parseo y se auditan los pares restantes (md↔SQL) sin abortar. |
| Migraciones crean entidades no documentadas en la md | Fila de drift md↔SQL; corrección: documentar en la md lo implementado. |
| Entidades del MVP sin migración creada | Fila de drift con corrección "migración pendiente"; no se corrige la md. |
| Misma entidad con tipo distinto según fuente | Fila de drift de tipo; el conflicto md↔SQL se resuelve corrigiendo la md (verdad operativa). |
| Relación sin cardinalidad en el drawio | Fila de drift de cardinalidad (el diagrama estricto debe declararla). |
| Enum sin valores en el drawio | No genera drift: los enums se comparan solo entre md y SQL. |
| Reporte con muchas discrepancias | Resumen numérico por tipo al inicio; filas completas después; no se truncan. |
| Corrección confirmada que rompe otra sección | El re-audit lo detecta; los cambios se aplican fila por fila, no en bloque. |

## Desviaciones conocidas de referencia (validación de la skill)

Al ejecutar sobre este repo, la skill debe detectar al menos:

1. Las migraciones crean el esquema RBAC (`V1__create_rbac.sql`, seed en `V2__seed_rbac.sql`) que
   la md del MVP no documenta → filas md↔SQL (entidad y atributos).
2. La md documenta las entidades del MVP (Agricultor, Sembradio, Cultivo, Consulta, Sesión,
   Mensaje, diagnóstico/recomendación) sin migración creada → filas con "migración pendiente".
3. Si el drawio no incluye atributos con tipo, o los incluye distintos a la md → filas md↔drawio
   (atributo/tipo). El reporte muestra los elementos extraídos del XML (prueba de parseo).

## Límites

- No se edita `.drawio` ni SQL; no se crean migraciones nuevas (eso es el trabajo "Alinear el
  modelo de datos", migración V3).
- No se valida cumplimiento de requisitos ni convenciones del diccionario: es responsabilidad de
  las otras skills de la familia, no de esta.
- Ante cualquier duda sobre si una fuente es la que se desvió, se pregunta en vez de adivinar.