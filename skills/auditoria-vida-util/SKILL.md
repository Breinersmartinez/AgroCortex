---
name: auditoria-vida-util
description: Orquestador que ejecuta en secuencia las cuatro skills de auditoría de AgroCortex (traza-requisitos-modelo, valida-diccionario-datos, concuerda-docs-diagramas, audita-agents-md) en modo read-only, consolida sus drifts en una matriz de conformidad por capa del ciclo de vida (requisitos · análisis · diseño/arquitectura · esquema/BD · código + gobernanza transversal) y entrega un informe HTML fechado en docs/07-work con resumen en consola; jamás corrige (eso va por la skill individual). Usar para responder de un vistazo "¿estamos cumpliendo la vida útil del software?", al abrir una sesión, tras cambios estructurales o antes de decidir sobre el estado del proyecto.
---

# auditoria-vida-util

Orquestador de la familia de auditoría de AgroCortex. Ejecuta en secuencia las cuatro skills
construidas, consolida sus drifts por capa del ciclo de vida y entrega un único informe fechado.
Este skill **solo audita**: no corrige nada; la corrección siempre ocurre con la skill individual
de la capa afectada.

## Qué hace y qué no

- **Ejecuta en secuencia (read-only):** `traza-requisitos-modelo`, `valida-diccionario-datos`,
  `concuerda-docs-diagramas` y `audita-agents-md`. Cada skill corre con su flujo de reporte, no de
  corrección.
- **Consolida:** asigna cada drift a una capa fija del ciclo de vida — requisitos, análisis,
  diseño/arquitectura, esquema/BD, código — más la fila transversal de gobernanza (resultado de
  `audita-agents-md`), sin duplicar drifts entre capas.
- **Entrega:** un HTML autocontenido fechado en `docs/07-work/informe-vida-util-YYYY-MM-DD.html`
  (sufijo `-2`, `-3`… si la fecha ya existe) y un resumen corto en consola.
- **No corrige:** ningún drift se edita desde aquí; el orquestador no ofrece aplicar correcciones.
- **No audita código:** con backend/frontend scaffold la capa código queda `sin llegar`.

## Reglas de oro (no negociables)

1. **Read-only total.** La ejecución solo escribe el informe nuevo; ninguna fuente existente cambia.
2. **No se inventa.** Una capa sin fuentes, una skill sin instalar y una ejecución fallida se
   declaran (`sin llegar` / `no evaluable`); jamás se rellenan.
3. **Drift a una sola capa.** Cada drift se atribuye a la capa que representa y se guarda su origen
   (skill + fila). No se copia entre capas.
4. **La corrección va por skill individual.** Si el usuario quiere corregir, se le redirige a la
   skill de la capa; el orquestador no rectifica.

## Flujo de trabajo

### Paso 1 — Preparar
1. Determinar la fecha para el nombre del informe.
2. Verificar que existan las cuatro skills instaladas (deberían vivir en `~/.opencode/skills`/`.
   En caso de faltar, anotarlas (su capa queda `no evaluable`).
3. Anotar si el working tree está sucio (`git status --porcelain`) para reflejarlo al pie del informe.

### Paso 2 — Ejecutar traza (capas requisitos y diseño)
Correr `traza-requisitos-modelo` en read-only y recoger:
- Requisito sin soporte / parcial → capa **requisitos**.
- Capability sin patrón asignable, huérfanos backward → capa **diseño/arquitectura**.

### Paso 3 — Ejecutar diccionario y concordancia (capas análisis y esquema/BD)
Correr `valida-diccionario-datos` y `concuerda-docs-diagramas` en read-only:
- Completitud/convenciones del diccionario y discrepancias md↔drawio → capa **análisis**.
- Convenciones rotas en SQL, enums sin materializar, md↔SQL → capa **esquema/BD**.

### Paso 4 — Ejecutar gobernanza y declarar código
Correr `audita-agents-md` en read-only → fila transversal **gobernanza**. La capa **código** se
declara `sin llegar` mientras `backend/src/main/java` solo contenga `AgroCortexApplication.java` y
el frontend no tenga rutas/servicios.

### Paso 5 — Consolidar y emitir
1. Matriz por capa con veredicto:
   - `conforme` = 0 drifts en la capa.
   - `desviado` = ≥1 drift, con conteo de críticos y menores heredado de la skill de origen
     (brechas de alcance, convenciones rotas en SQL y migraciones desalineadas = críticos; naming,
     huérfanos documentales y detalles = menores).
   - `sin llegar` = capa sin fuentes.
2. Veredicto global con la regla documentada: `conforme` solo si las 5 capas son conforme; con
   capas `sin llegar` y el resto conforme = `parcial`; cualquier `desviado` = global `desviado`.
3. Escribir `docs/07-work/informe-vida-util-<fecha>.html` (autocontenido: matriz primero, detalle
   de drifts por capa con su skill de origen después, nota de git status al pie) y emitir en
   consola: veredicto global + una línea por capa.

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| Falla la ejecución de una skill | Se continúa con las demás; la capa de la skill fallida queda `no evaluable` y se declara. |
| Skill de la familia no instalada | Capa correspondiente `no evaluable`; el informe lista la skill ausente y el global dice "evaluación parcial". |
| Fuente de una capa inexistente | La capa queda `sin llegar`; no se rellena. |
| Informe del mismo día ya existe | Se escribe con sufijo `-2`, `-3`… sin sobrescribir el anterior. |
| Working tree sucio al inicio | Se anota al pie; no se trata como drift de fuentes. |
| Todo conforme (futuro) | 5 capas `conforme`, global `conforme`, conteos en 0. |
| Mezcla de sin llegar y desviadas | La regla del global prevalece: cualquier `desviado` hace el global `desviado`. |

## Límites

- No corrige nada; cada capa se corrige con su skill individual.
- No audita código implementado (no existe skill de código ni insumo).
- No evalúa capas sin fuentes ni skills ausentes: las declara y sigue.
- Ante cualquier duda sobre la atribución de un drift, se pregunta en vez de adivinar.