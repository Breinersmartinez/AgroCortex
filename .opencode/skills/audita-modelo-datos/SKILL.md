---
name: audita-modelo-datos
description: Verifica la consistencia y conformidad del modelo de datos entre documentación, diagrama y persistencia SQL. Comprueba que las fuentes describen el mismo modelo (consistencia) y que ese modelo cumple las convenciones del proyecto (conformidad). Usar cuando se quiera comprobar que el diccionario de datos es completo, correcto y coherente entre fuentes, tras tocar cualquiera de ellas, antes de implementar entidades o de crear una migración nueva.
---

# audita-modelo-datos

Auditoría del modelo de datos del proyecto. Verifica dos cosas que se parecen pero no son lo
mismo: **consistencia** (¿las distintas fuentes describen el mismo modelo?) y **conformidad**
(¿ese modelo cumple las reglas y convenciones del proyecto?). Este skill informa y, en modo
corrección, corrige la documentación; no toca el diagrama ni las migraciones SQL.

## Qué audita y qué no

- **Consistencia entre fuentes:**
  - Documentación de datos (diccionario / modelo).
  - Diagrama ER (parseando su XML si es drawio).
  - Migraciones SQL.
  - Comprueba: entidades, atributos, tipos, PK, FK, relaciones, cardinalidades, enums,
    presencia/ausencia de tablas.
- **Conformidad con convenciones del proyecto:**
  - PK uuid en cada entidad.
  - Auditoría (creadoEn/actualizadoEn o su equivalente) en todas las entidades.
  - Nombres de columnas en el estilo documentado (camelCase, snake_case, etc.).
  - Enums: valores idénticos entre diccionario y SQL, idioma, dominio definido.
  - Completitud: cada entidad con tabla de atributos, cada atributo con tipo y requerido.
  - DANE u otros campos con tamaño específico documentado.
  - Naming canónico cuando exista.
- **No audita:** cumplimiento de requisitos del dominio (eso es `traza-requisitos`),
  coherencia de la documentación de gobierno contra el árbol (es `audita-contexto`), ni código.
- **No modifica:** nunca edita el `.drawio` ni las migraciones SQL. Solo puede editar la
  documentación del modelo, con confirmación por fila.

## Reglas de oro (no negociables)

1. **Read-only por defecto.** La edición de la documentación requiere modo corrección y
   confirmación por fila.
2. **Tres vías de verdad.** Cualquier par de discrepancias significa drift: la documentación
   dice una cosa, el diagrama otra y las migraciones una tercera; las tres se contrastan entre sí.
3. **Ante conflicto documentación vs migraciones, priman las migraciones.** Son la verdad
   operativa; si la documentación las desmiente, la corrección sugerida es documentar lo
   implementado.
4. **Lo implementado sin documentar no es error de la documentación.** Entidades creadas en SQL
   sin entrada en el diccionario se reportan como "documentar lo implementado"; lo no escrito en
   SQL no se corrige forzando la documentación a copiar un error.
5. **Lo no implementado no es error del diccionario.** Entidades documentadas sin migración son
   "migración pendiente", no defectos de la documentación.
6. **Nada se inventa.** Si una fuente falta o está corrupta, se reporta y se sigue con las demás;
   no se reconstruye contenido de memoria.
7. **Cada problema aparece una sola vez.** No se duplican hallazgos entre inconsistencia y
   conformidad; cada hallazgo se clasifica en la categoría que corresponde.

## Separación conceptual

### Consistencia

> ¿Las distintas fuentes describen el mismo modelo?

Compara elementos entre pares de fuentes. Ejemplo: la documentación lista una entidad que el
diagrama no dibuja → inconsistencia.

### Conformidad

> ¿Ese modelo cumple las reglas y convenciones del proyecto?

Valida cada fuente contra la lista de convenciones. Ejemplo: una columna de auditoría usa
snake_case en inglés en vez del camelCase documentado → falta de conformidad.

## Flujo de trabajo

### Paso 0 — Confirmar modo
Preguntar si es leer todo (read-only) o corregir la documentación (corrección). Sin modo
corrección pedido, el trabajo termina en el reporte.

### Paso 1 — Inventario y lectura de las fuentes
1. Localizar: documentación del modelo, diagrama ER (si existe), migraciones SQL. Si alguna
   no existe, anotarla como caso borde.
2. Leer la documentación completa (entidades, atributos, tipos, PK/FK, enums, relaciones).
3. Parsear el XML del diagrama (si es drawio) sin abrirlo:
   - Celdas compuestas (tablas) = entidades; su `value` lleva el nombre.
   - Filas internas de cada tabla = atributos: de su `value` se extrae nombre y tipo.
   - Celdas con `edge="1"` y etiqueta = relaciones: extremos y cardinalidad.
   - Si el XML no parsea, reportar el error y auditar los pares restantes.
4. Parsear las migraciones SQL:
   - `CREATE TABLE` → entidad + columnas con tipo y PK.
   - `FOREIGN KEY` → relaciones.
   - `CREATE TYPE ... AS ENUM(...)` o restricciones → enums.

### Paso 2 — Normalizar
Construir para cada fuente un modelo con los mismos elementos: `entidad`, `atributo
(entidad, nombre, PK/FK, tipo)`, `relación (origen, destino, cardinalidad)`, `enum (entidad,
valores)`.

### Paso 3 — Verificar consistencia
Comparar los tres pares de fuentes (documentación↔diagrama, documentación↔SQL,
diagrama↔SQL) para cada elemento normalizado. Anotar cada discrepancia como fila de
inconsistencia.

### Paso 4 — Verificar conformidad
Contrastar cada fuente contra las convenciones del proyecto:
- Completitud del diccionario: cada entidad con tabla de atributos, cada atributo con tipo
  y requerido.
- Convenciones: PK, auditoría, nombres de columnas, DANE, enums, naming canónico.
- Enums: idioma, igualdad entre diccionario y SQL, dominio definido.
- SQL: naming de tablas/columnas contra la convención documentada.

### Paso 5 — Reportar y (solo si corresponde) corregir
1. Iniciar con resumen numérico: total de inconsistencias y total de faltas de conformidad.
2. Tabla de **inconsistencia** con columnas `fuente A / fuente B / elemento / discrepancia /
   corrección`.
3. Tabla de **conformidad** con columnas `fuente / elemento / convención / hallazgo /
   corrección`.
4. Nota final: `git status` del repo y si el árbol estaba sucio.
5. Si hay corrección pedida: listar filas que se editarían en la documentación del modelo,
   pedir confirmación por fila, aplicar solo las confirmadas y re-auditar.

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| `.drawio` ausente o sin celdas con `value` | Se reporta y se auditan los pares restantes (documentación↔SQL). |
| XML del `.drawio` corrupto | Se reporta el error de parseo y se auditan los pares restantes sin abortar. |
| Migraciones crean entidades no documentadas | Fila de inconsistencia documentación↔SQL; corrección: documentar lo implementado. |
| Entidades documentadas sin migración creada | Fila con corrección "migración pendiente"; no se corrige la documentación. |
| Misma entidad con tipo distinto según fuente | Fila de inconsistencia de tipo; el conflicto se resuelve documentando lo implementado. |
| Relación sin cardinalidad en el diagrama | Fila de inconsistencia de cardinalidad. |
| Enum sin valores en el diagrama | No genera inconsistencia: los enums se comparan solo entre documentación y SQL. |
| SQL con snake_case en inglés contra convención camelCase | Fila de conformidad; corrección "a migrar en V3". |
| Enum del diccionario sin CREATE TYPE en SQL | Fila de conformidad "dominio sin materializar"; es migración pendiente. |
| Campo de estado/tipo sin dominio definido | Fila de conformidad; corrección a la documentación. |
| Reporte con muchas discrepancias | Resumen numérico por categoría al inicio; filas completas después; no se truncan. |
| Corrección confirmada que rompe otra sección | El re-audit lo detecta; los cambios se aplican fila por fila, no en bloque. |

## Límites

- No se edita `.drawio` ni SQL; no se crean migraciones nuevas.
- No se valida cumplimiento de requisitos del dominio ni coherencia de la documentación de
  gobierno: es responsabilidad de `traza-requisitos` y `audita-contexto`.
- No se conecta a la base de datos: la auditoría trabaja sobre el texto versionado.
- Ante cualquier duda sobre si algo es desviación o excepción intencional, se pregunta en vez
  de adivinar.
