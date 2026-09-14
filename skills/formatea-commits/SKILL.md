---
name: formatea-commits
description: Genera y valida mensajes de commit del monorepo AgroCortex siguiendo docs/06-contribution/git-commit-prefixes.md (Conventional Commits: tipo[ámbito]: descripción imperativo en español, título breve), analizando el diff staged o el working tree, y comprueba mensajes ya escritos contra ese formato; nunca ejecuta git commit. Usar antes de commitear, cuando un commit necesita mensaje bien formateado, al revisar un mensaje mal escrito o cuando el usuario pida "formatea el commit", "genera el mensaje de commit" o "valida este mensaje".
---

# formatea-commits

Genera y valida mensajes de commit del monorepo AgroCortex contra la convención documentada en
`docs/06-contribution/git-commit-prefixes.md`. La fuente única del formato es ese documento: si el
doc cambia, la skill obedece el cambio.

## Qué hace y qué no

- **Modo generar:** analiza `git diff --staged` (y cae al working tree si no hay staged), infiere
  tipo y ámbito por paths y contenido, y entrega un mensaje
  `<tipo>[ámbito]: verbo imperativo en español, minúsculas, ≤50 caracteres>`.
- **Modo validar:** comprueba un mensaje dado contra el formato y reporta cada violación con la
  versión corregida.
- **Nunca ejecuta `git commit`, `amend`, `rebase` ni `push`.** El commit es decisión del autor;
  la skill entrega el mensaje listo para copiar (o el usuario lo aplica).
- No reescribe commits existentes y no gestiona Git (merge, conflictos, push).

## Reglas de oro (no negociables)

1. **La convención está en el doc**, no endurecida aquí: la taxonomía de tipos
   (`feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`, `build`, `ci`) se lee de
   `docs/06-contribution/git-commit-prefixes.md`.
2. **Nunca commitea solo.** Si el usuario pide "haz el commit", se entrega el mensaje y se cumple
   que la acción del commit la ejecuta el usuario.
3. **Idioma español.** Descripción en imperativo y minúscula inicial (nombres propios como
   "Sembradio" se respetan).
4. **Traza visible.** El modo generar muestra los paths staged y una línea del razonamiento
   tipo/ámbito propuesto.

## Flujo de trabajo

### Paso 1 — Leer la convención
Leer `docs/06-contribution/git-commit-prefixes.md` y usar como taxonomía los tipos y la estructura
que el doc define.

### Paso 2 — Capturar el intento de cambio
`git status --porcelain` y `git diff --staged`; si no hay staged, `git diff` (working tree). Si no
hay cambios, avisar y terminar.

### Paso 3 — Inferir tipo y ámbito
Por predominancia de paths y contenido:
- `backend/src/main/resources/db/migration/` → `feat` si altera esquema, `chore` si es migración de
  mantenimiento (según contenido).
- `frontend/src/`, `backend/src/main/java/` → `feat`/`fix`/`refactor`/`perf` según el diff.
- Solo `docs/` → `docs`. Solo `*.test.ts` / `src/test/` → `test`.
- `.github/workflows/` → `ci`. Dependencias (`pom.xml`, `package.json`, lockfiles) → `build`;
  tooling/config de repo → `chore`.

Ámbito: el módulo dominante del cambio (`auth`, `sembradios`, `crops`, `consultations`,
`diagnoses`, `frontend`, `backend`, `docs`, `ci`…). Cambios transversales → sin ámbito (o el
componente mayoritario marcado con "revise").

### Paso 4 — Generar o validar
- **Generar:** redactar `tipo[ámbito]: <imperativo corto en español>`; si el diff lo pide, ofrecer
  un cuerpo breve tras una línea en blanco. Si el tipo es ambiguo, elegir el más probable con
  justificación de una línea y marcar "revise".
- **Validar:** comprobar el mensaje contra `^(feat|fix|docs|style|refactor|perf|test|chore|build|ci)(\([a-z0-9-]+\))?: .+`
  más las reglas humanas (minúscula inicial, imperativo, título ≤50). Cada violación se reporta con
  el mensaje corregido; un título largo se propone acortado manteniendo el contenido. Un commit de
  merge no se formatea.

### Paso 5 — Entregar
Mostrar el mensaje final en bloque de código para copiar y el razonamiento en una línea. No se
ejecuta ningún comando de commit.

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| Nada staged y working tree limpio | Avisa que no hay cambios que formatear y termina. |
| Cambios en varios módulos a la vez | Tipo/ámbito del módulo mayoritario; lista los demás en el cuerpo y marca "revise" si el reparto es dudoso. |
| Tipo ambiguo (deps + tooling) | Propone el más probable con justificación y lo marca para revisar. |
| Mensaje a validar en mayúscula/no-imperativo | Reporta la violación concreta y entrega el mensaje corregido. |
| Título >50 caracteres | Reporta longitud y propone versión más corta. |
| Commit de merge | Avisa que no lleva tipo Conventional Commits y no lo formatea. |
| Diff que solo toca docs | Tipo `docs`, ámbito `docs`. |
| Nombre propio en la descripción | Se conserva la mayúscula del nombre dentro de la minúscula general. |

## Límites

- No commitea, no reescribe historial, no empuja.
- No decide tipos genuinamente ambiguos: marca "revise" y deja la última palabra al autor.
- No valida el contenido del código, solo el formato del mensaje.
- Si el doc de convención se muda o cambia el formato, la skill lo sigue (fuente única).