---
name: deploy-heroku-springboot
description: Despliega el backend Spring Boot del monorepo AgroCortex a Heroku (modelo heroku.yml + Docker, stack container), verifica que la app quedó arriba y diagnostica los fallos de despliegue. Usar cuando el usuario pida "desplegar a Heroku", "deploy del backend", "subir la API a Heroku", "pushear a heroku", "ver por qué la app está caída" o cuando el despliegue a Heroku devuelva errores como H10, "No default language could be detected" o "COPY failed".
---

# deploy-heroku-springboot

Despliegue del backend Spring Boot del monorepo AgroCortex a Heroku. Fuentica de verdad:
`docs/04-operations/deployment.md` y `AGENTS.md`. Este skill no decide qué desplegar: ejecuta
el despliegue y su verificación con confirmación previa, y no toca nada que no se le pida.

## Qué despliega y qué no

- **Despliega:** el backend en `backend/` (Spring Boot 3.4.1, Java 17, Maven), vía `heroku.yml`,
  a Heroku. El frontend (Angular) va a **Vercel** y queda fuera de este skill.
- **Modelo de despliegue:** `heroku.yml` en la raíz + `git push` al remote de Heroku. NO se usa
  `heroku container:push` ni buildpacks (su presencia rompe el CLI con `No images to push`).
- **Remote:** el push a Heroku no pasa por la protección de rama de GitHub. No se hace
  `git push origin main` como parte de esto.

## Reglas de oro (no negociables)

1. **Confirmación antes de desplegar.** El push a Heroku es irreversible contra su `main`.
   Antes de ejecutar, mostrar el comando exacto, a qué rama/commit y qué implica, y esperar un
   "sí" explícito del usuario. Si el usuario no confirma, no se despliega.
2. **No correr con perfil `prod` fuera de Heroku.** Sin las vars `PG*` la app muere con H10.
3. **No hardcodear credenciales ni URLs de despliegue.** Todo va por env vars, secrets o el CLI
   de Heroku. La URL de la app se obtiene con `heroku apps:info`, no se escribe a mano.
4. **No desplegar con el build roto.** Si `mvn -B verify` falla, se reporta el error y no se
   despliega.
5. **Un solo ambiente.** No existe staging/producción: se despliega a lo único que hay.

## Flujo de trabajo

### Paso 0 — Confirmar
Preguntar: ¿qué rama/commit se despliega y se confirma el push? Responder el plan (comandos
exactos) y esperar aprobación. Sin aprobación, terminar aquí.

### Paso 1 — Pre-flight (todo debe pasar antes del push)
1. `git status`: árbol limpio y en la rama/commit acordado.
2. `heroku.yml` existe en la raíz con `build.docker.web: backend/Dockerfile` y
   `run.web: java -jar app.jar` (el Procfile ya no existe y no se debe crear).
3. Build local: `mvn -B verify` desde `backend/`. Si falla, reportar y parar.
4. Build Docker correcto (contexto = `backend/`): `docker build -f backend/Dockerfile -t agrocortex:test .`
   desde la raíz. Un `COPY failed: ... pom.xml: not found` significa contexto equivocado → parar.
5. Stack correcto: `heroku apps:info -a agro-cortex` debe mostrar `Stack: container`.
   Si no, `heroku stack:set container -a agro-cortex` (config que se hace una sola vez).
6. Perfil prod: `heroku config:get SPRING_PROFILES_ACTIVE -a agro-cortex` debe devolver `prod`.
   Si está vacío o `dev`, `heroku config:set SPRING_PROFILES_ACTIVE=prod -a agro-cortex`.
7. Red remota: verificar el remote con `git remote -v` (debe existir `heroku`). Si falta,
   `heroku git:remote -a agro-cortex` (config de una sola vez).

### Paso 2 — Desplegar
```bash
git push heroku <rama>:main
```
El build de Docker ocurre en Heroku, no localmente. Si el push falla con
`[rejected] ... (fetch first)`, es un checkout shallow: rehacer con historial completo
(fetch) y no forzar.

### Paso 3 — Verificar
1. `heroku ps -a agro-cortex` → `web.1` debe estar `up`.
2. `heroku logs --num 50 -a agro-cortex` → sin excepciones fatales ni crash loop.
3. Probar la URL (obtenida de `heroku apps:info -a agro-cortex`, campo Web URL):
   `curl -s -o /dev/null -w "%{http_code}\n" "<web-url>/"`.
   - `401` es **esperado** mientras no haya `SecurityConfig` real (Spring Security genera password
     por defecto). No es un fallo.
   - `503`, `404` persistente o `app crashed` sí son fallos → ir a la matriz.

### Paso 4 — Si falla
Consultar la matriz de errores y aplicar la solución indicada. No repetir pushes ciegos:
cada reintento necesita una causa identificada.

## Matriz de errores

| Error | Causa | Solución |
|---|---|---|
| `COPY failed: ... pom.xml: not found` | Contexto de build Docker = raíz en vez de `backend/` | `docker build -f backend/Dockerfile -t agrocortex:test .` desde la raíz (el contexto es la carpeta del Dockerfile) |
| `No default language could be detected` | Se usó buildpack en monorepo (el `pom.xml` vive en `backend/`, no en la raíz) | Solo hay `heroku.yml`; stack `container`; nunca buildpack |
| `Your app does not include a heroku.yml build manifest` | Push sin `heroku.yml` commiteado | Commitear `heroku.yml` en la raíz; `heroku apps:info` debe dar `Stack: container` |
| `No images to push` | Presencia de `heroku container:push` en el flujo | Usar `git push` vía `heroku.yml`; no usar `container:push` |
| App arranca y muere (H10) | Perfil `dev` sin PostgreSQL local en Heroku | `heroku config:set SPRING_PROFILES_ACTIVE=prod -a agro-cortex` y redeploy |
| `[rejected] ... (fetch first)` en CI | Checkout shallow sin historial completo | `actions/checkout` con `fetch-depth: 0` (ya presente en `deploy.yml`); para push manual, hacer fetch completo |
| `you have triggered a build ... at least twice` | Misma SHA ya registrada en builds de Heroku | Esperar commit nuevo (o `git commit --allow-empty`) y volver a push |
| App con build OK pero sin responder (`crashed`/`503`) | Depende del log | Leer `heroku logs`; si falta PostgreSQL, revisar addon `heroku-postgresql` y vars `PG*` |

## Reglas del monorepo (por qué es así)

- `heroku.yml` en la raíz define el proceso web (`java -jar app.jar`); reemplazó al Procfile.
- El contexto del build Docker es `backend/` (carpeta del Dockerfile): el `COPY pom.xml` del
  Dockerfile funciona aunque el `pom.xml` no esté en la raíz.
- **No mover `pom.xml` a la raíz**: el buildpack Java no entiende subdirectorios; el backend va
  por Docker vía `heroku.yml`, nunca por buildpack.
- `frontend/` queda excluida del contexto Docker vía `.dockerignore` de la raíz.

## Límites

- No se despliega el frontend (Vercel), ni se toca `main` de GitHub.
- No se configuran secrets ni addons sin que el usuario lo pida (aunque el skill reporta si
  `SPRING_PROFILES_ACTIVE` o el stack están mal).
- No se repiten deploys sin causa: cada reintento responde a un error identificado.
- Ante cualquier duda sobre credenciales, permisos o URLs, se pregunta al usuario; nunca se
  inventan.