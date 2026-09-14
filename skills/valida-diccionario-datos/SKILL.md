---
name: valida-diccionario-datos
description: Audita la salud del diccionario de datos de AgroCortex (data-model-mvp.md) contra el SQL de las migraciones y las convenciones del proyecto (uuid PK, auditoría, DANE, enums en español, camelCase, Sembradio canónico), y emite una tabla de drift con correcciones; en modo corrección edita solo la md con confirmación por fila y nunca el SQL. Usar cuando se quiera comprobar que el diccionario es completo y correcto, que cumple las convenciones, y que las migraciones respetan el mismo idioma de diseño, antes de implementar entidades o de crear una migración nueva.
---

# valida-diccionario-datos

Auditoría de la salud del diccionario de datos: `docs/02-data/data-model-mvp.md` (el diccionario)
contra las migraciones SQL de `backend/src/main/resources/db/migration/` y contra las convenciones
del proyecto (AGENTS.md). Este skill informa y, en modo corrección, corrige la md; nunca toca el SQL.

## Qué audita y qué no

- **Audita:**
  - Completitud del diccionario: toda entidad documentada con su tabla de atributos, y cada
    atributo con tipo, requerido (Sí/No) y origen.
  - Convenciones en el diccionario: PK `id` uuid en cada entidad, `creadoEn`/`actualizadoEn` en
    todas, `departamentoDane` text(2)/`municipioDane` text(5), columnas camelCase en español,
    nombre canónico `Sembradio`/`sembradioId`.
  - Convenciones en el SQL: las tablas/columnas que crean las migraciones contra la misma regla
    (por ejemplo `created_at`, `updated_at`, `app_users` = snake_case/inglés → desviación).
  - Enums: valores en español, valores idénticos entre diccionario y SQL, y dominio definido para
    todo campo de estado/tipo (nunca `VARCHAR` libre).
  - Diccionario vs SQL: toda tabla/columna del SQL tiene entrada en el diccionario y viceversa;
    las entidades sin migración son "migración pendiente".
- **No audita:** relaciones/cardinalidades entre fuentes (eso es `concuerda-docs-diagramas`), el
  diagrama ER (ídem), cumplimiento de requisitos del dominio (es `traza-requisitos-modelo`),
  coherencia de AGENTS.md contra el árbol (es `audita-agents-md`), ni la base de datos real
  (no se conecta: la auditoría es sobre texto versionado).
- **No modifica:** nunca edita las migraciones ni crea la V3. Solo puede editar
  `data-model-mvp.md`, con confirmación por fila.

## Reglas de oro (no negociables)

1. **Read-only por defecto.** La edición de la md requiere modo corrección y confirmación por fila.
2. **La convención manda sobre el diccionario.** Si la md incumple una convención, la corrección
   sugerida va a la md.
3. **La migración es la verdad operativa para lo que existe.** Una tabla que el SQL crea y la md
   no documenta se corrige documentándola; un SQL que no cumple la convención se reporta como
   "a migrar en V3", jamás se edita y nunca se obliga a la md a copiar el error.
4. **Lo no implementado no es error del diccionario.** Entidades sin migración y enums sin
   `CREATE TYPE` son "migración pendiente", no defectos de la md.
5. **Nada se inventa.** Un atributo sin tipo, sin requerido o sin origen no se adivina: se reporta
   como drift de completitud.

## Flujo de trabajo

### Paso 0 — Confirmar modo
Preguntar si es read-only (por defecto) o corrección de la md. Sin modo corrección, el trabajo
termina en el reporte.

### Paso 1 — Inventario y lectura
1. Localizar y leer completo: `docs/02-data/data-model-mvp.md` y
   `backend/src/main/resources/db/migration/*.sql`.
2. Extraer del diccionario: entidades, su tabla de atributos, y por atributo tipo, requerido y origen.
3. Extraer del SQL: tablas, columnas con tipo, PK, FKs, defaults y `CREATE TYPE`/constraints.

### Paso 2 — Verificar completitud del diccionario
Para cada entidad de la md: ¿tiene tabla de atributos? Para cada atributo: ¿declara tipo,
requerido y origen? Cada ausencia = fila de drift de completitud.

### Paso 3 — Chequear convenciones (en diccionario y en SQL)
Contrastar ambas fuentes contra la lista de convenciones:
- PK `id` uuid en todas las entidades/tablas.
- Auditoría `creadoEn`/`actualizadoEn` en todas.
- DANE: `departamentoDane` text(2), `municipioDane` text(5).
- Columnas en camelCase en español.
- Nombre canónico `Sembradio`/`sembradioId`.

Una columna como `created_at` o `updated_at` en el SQL es una desviación de convención (snake_case
inglés) marcada "a migrar en V3".

### Paso 4 — Verificar enums
- Idioma: los valores están en español.
- Igualdad: los valores que declara la md coinciden con los del SQL (`CREATE TYPE ... AS ENUM`,
  constraints o checks).
- Dominio: cada campo de estado/tipo de la md tiene un dominio definido; un campo de estado sin
  dominio (VARCHAR libre) es drift de enums.

### Paso 5 — Reportar y (solo si corresponde) corregir
1. Reporte agrupado por categoría (completitud, convenciones de diccionario, convenciones de SQL,
   enums, pendientes de migración) con total al inicio y filas completas después. Columnas de la
   tabla: `diccionario dice / SQL tiene / corrección`.
2. Si el usuario pidió corrección: listar filas que se editarían en la md, pedir confirmación por
   fila, aplicar solo las confirmadas y re-auditar; las filas "a migrar en V3" o "migración
   pendiente" nunca se editan.

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| Entidad de la md sin tabla de atributos | Fila de drift de completitud: la entidad existe pero no define sus atributos. |
| Atributo sin tipo, requerido u origen | Fila de drift de completitud: definición insuficiente para servir de diccionario. |
| Campo DANE sin el tamaño text(2)/text(5) | Fila de drift de convención; corrección sugerida a la md. |
| SQL de V1/V2 con snake_case/inglés (`created_at`, `app_users`) | Fila de drift de convención en SQL con corrección "a migrar en V3". |
| Enum del diccionario sin CREATE TYPE ni constraint en SQL | Fila de drift de enums "dominio sin materializar"; es migración pendiente. |
| Campo de estado/tipo de la md sin dominio definido | Fila de drift de enums; corrección sugerida a la md. |
| Valores de enum en inglés | Fila de drift de idioma: la convención exige español. |
| Valores de enum distintos entre md y SQL | Fila de drift de igualdad: diccionario y base se despidieron. |
| Tablas MVP sin migración | Fila de drift con "migración pendiente"; no se corrige la md. |
| Corrección confirmada que rompe otra sección | El re-audit lo detecta; los cambios se aplican por fila, no en bloque. |

## Desviaciones conocidas de referencia (validación de la skill)

Al ejecutar sobre este repo, la skill debe detectar al menos:

1. Las migraciones V1/V2 usan naming snake_case en inglés: `created_at`, `updated_at`, `app_users`,
   `app_roles`, `app_permissions`, `app_user_roles`, `app_role_permissions` — contra la convención
   camelCase en español y la de auditoría `creadoEn`/`actualizadoEn` → "a migrar en V3".
2. Las migraciones no materializan ningún enum (`CREATE TYPE` ausente); si el diccionario define
   enums, quedan "dominio sin materializar" (migración pendiente).
3. Las entidades MVP del diccionario (Agricultor, Sembradio, Cultivo, Consulta, Evidencia, Sesión
   Mensaje, Diagnóstico, Hipótesis, Recomendación) no tienen migración creada → "migración pendiente".
4. Si la md tiene atributos sin tipo/requerido/origen o DANE sin tamaño, aparecen como filas de
   completitud/convención.

## Límites

- No se edita ni se crea migraciones; la estabilidad de las convenciones frente al SQL se da vía
  "a migrar en V3", no con edición directa.
- No se conecta a la base de datos: la auditoría trabaja sobre el texto versionado.
- No se sustituye a `concuerda-docs-diagramas` ni a las demás skills de la familia.
- Ante cualquier duda sobre si algo es desviación o excepción intencional, se pregunta en vez de
  adivinar.