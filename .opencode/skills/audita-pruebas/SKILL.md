---
name: audita-pruebas
description: Audita las pruebas de un proyecto contra sus criterios de aceptación, en las dos direcciones —cada criterio con la prueba que lo evidencia y cada prueba con el criterio o regla de origen—, y detecta criterios sin prueba, pruebas que no verifican (sin aserción, vacías, deshabilitadas) y pruebas huérfanas. Read-only, sin umbral de cobertura. Usar cuando se quiera saber si lo que el proyecto promete está realmente probado, tras escribir o cambiar pruebas, o antes de dar por cerrada una especificación.
---

# audita-pruebas

Auditoría de la relación entre lo que un proyecto promete y lo que verifica. Contrasta los
criterios de aceptación (la promesa auditable) contra las pruebas que existen (la verificación),
en las dos direcciones. Este skill informa: no escribe pruebas ni configuración, y no ejecuta la
suite.

## Qué audita y qué no

- **Audita:**
  - **Forward:** cada criterio de aceptación contra la prueba que lo evidencia (clase y método).
  - **Backward:** cada prueba relevante contra el criterio o la regla que verifica.
  - Pruebas que no verifican: sin aserción, siempre verdes, vacías, deshabilitadas u omitidas.
  - Huérfanos: criterios sin prueba y pruebas sin criterio.
- **No audita:** calidad estática o estilo, seguridad (SAST/SCA), el contenido semántico de los
  requisitos (eso es `traza-requisitos`), el modelo de datos (`audita-modelo-datos`) ni el
  gobierno de la documentación (`audita-contexto`).
- **No modifica:** no escribe pruebas, código de aplicación, configuración de build ni
  configuración de CI. El trabajo termina en el reporte.

## Reglas de oro (no negociables)

1. **Read-only.** La skill solo reporta; al terminar, `git status` queda limpio.
2. **Evidencia por nombre.** Un criterio se marca _cubierto_ solo si se cita la prueba que lo
   verifica (clase y método). Sin nombre no hay evidencia: el criterio queda _sin prueba_.
3. **Una prueba que no verifica no cubre nada.** Sin aserción, siempre verde, vacía,
   deshabilitada u omitida no cuenta como evidencia y se reporta aparte.
4. **La cobertura no es un veredicto.** El éxito se mide por criterios con prueba, no por
   porcentaje; si existe cobertura, se usa como señal secundaria, nunca como criterio de éxito.
5. **Nada se inventa.** Un criterio sin prueba es una brecha reportada; no se rellena con una
   prueba plausible ni se asume que otra capa "lo cubrirá".
6. **No se ejecuta CI.** El pipeline ya corre las pruebas y publica reportes; la skill juzga
   pertinencia, no decide si el build pasa. Si usa reportes, los lee; no los regenera.
7. **Genérica.** El stack, las rutas y los criterios entran por contexto; el núcleo no depende
   de ningún proyecto ni framework.

## Adaptadores por stack

La skill descubre las pruebas por convención del stack, no por rutas hardcodeadas. El stack y
sus rutas se confirman con el contexto de ejecución (build files, convención de nombres). Tabla
de referencia para los stacks más comunes; si el stack no aparece, se deduce y se deja
constancia del adaptador usado.

| Stack | Archivos de prueba | Reporte | Herramientas típicas |
|---|---|---|---|
| Java / JUnit | `src/test/**/*Test.java` | `target/surefire-reports/` | JUnit, Mockito, Spring Test |
| TS / Angular | `src/**/*.spec.ts` | `coverage/` | Jasmine, Karma |
| Python | `tests/**/test_*.py` | `.pytest_cache/` | pytest |

## Flujo de trabajo

### Paso 0 — Confirmar alcance
Confirmar el alcance (todo el repo o un conjunto de specs/módulos) y las fuentes de criterios.
No hay modo corrección: la skill solo reporta.

### Paso 1 — Inventario
1. Localizar la fuente de criterios: los documentos con criterios de aceptación (secciones §6
   de las specs y reglas de dominio equivalentes). Si falta, anotarlo como caso borde.
2. Localizar las pruebas con el adaptador del stack: rutas y convención de nombres.
3. Anotar la evidencia opcional disponible (reportes de pruebas, cobertura) sin generarla.

### Paso 2 — Extraer criterios
Descomponer cada fuente: cada criterio de aceptación es una fila, con su referencia de origen
(documento y número). Un criterio ambiguo o no verificable se marca _no evaluable_.

### Paso 3 — Extraer pruebas
Por cada archivo de prueba, listar sus casos (método / `it`) y clasificarlos:
- **Válida:** tiene al menos una aserción que puede fallar.
- **No verifica:** sin aserción, siempre verde, vacía.
- **Deshabilitada/omitida:** `@Disabled`, `xit`, `fdescribe`, `skip`, `todo`, equivalentes.

### Paso 4 — Construir la matriz bidireccional
- **Forward:** para cada criterio, estado _cubierto_ (con la prueba citada), _parcial_ (la
  prueba cubre solo parte del criterio) o _sin prueba_.
- **Backward:** para cada prueba válida, el criterio o la regla que verifica; si es
  infraestructura (por ejemplo, un _smoke test_ de arranque), se marca
  "origen: convención/infraestructura".

### Paso 5 — Reportar
1. Resumen de conteos al inicio: criterios cubiertos / parciales / sin prueba, pruebas que no
   verifican, huérfanas.
2. Tabla forward: `criterio / estado / evidencia (clase·método) / brecha`.
3. Tabla backward: `prueba / criterio o origen / hallazgo`.
4. Listado aparte de pruebas que no verifican y de las deshabilitadas.
5. Nota final de `git status` (debe quedar limpio).

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| No hay documentos con criterios de aceptación | Reportar que falta la fuente; no se inventan criterios ni se marca todo "sin prueba". |
| Proyecto sin ninguna prueba | Todos los criterios _sin prueba_; es una línea base reportada, no un error. |
| Prueba sin aserción o siempre verde | Fila "prueba que no verifica"; no cuenta como evidencia de ningún criterio. |
| Prueba deshabilitada u omitida | Se reporta como deshabilitada; no cubre su criterio. |
| Prueba sin criterio asociado | Huérfana; "origen: convención/infraestructura" si aplica; si no, se reporta como tal. |
| Criterio solo verificable con E2E o manual | Estado "no automatizable en unitarias"; se reporta, no se marca como defecto automático. |
| Criterio ambiguo o no verificable | _No evaluable_; se pregunta si es requisito o ruido del documento. |
| Cobertura alta con pruebas vacías | El reporte lo expone: cobertura ≠ verificación. |
| Reportes de CI ausentes o desactualizados | No se exigen; se anota la ausencia y se audita sobre el texto versionado. |
| Reporte muy extenso | Resumen de conteos al inicio; detalle completo después; no se truncan filas. |

## Límites

- No escribe pruebas ni código de aplicación: reporta. Generar pruebas es otro trabajo.
- No fija ni mide cobertura porcentual ni toca la configuración de build.
- No ejecuta la suite ni el pipeline de CI.
- No valida requisitos, modelo de datos ni gobierno de la documentación: eso es
  `traza-requisitos`, `audita-modelo-datos` y `audita-contexto`.
- Ante cualquier duda (criterio ambiguo, prueba huérfana, excepción intencional), se pregunta en
  vez de adivinar.
