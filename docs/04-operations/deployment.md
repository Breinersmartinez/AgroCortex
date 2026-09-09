# Despliegue — AgroCortex

Repositorio monorepo con dos aplicaciones independientes:

| Aplicación | Ubicación | Tecnología | Destino |
|---|---|---|---|
| API (backend) | `backend/` | Spring Boot 3.4.1 · Java 17 · Maven | Heroku (stack container) |
| SPA (frontend) | `frontend/` | Angular 19.2 · TypeScript | Vercel |

Cada una se despliega por su cuenta, con su propio pipeline y sus propias variables de entorno. No existe despliegue acoplado.

---

## 1. Backend — Heroku

### 1.1. Modelo de despliegue

Se usa el **Container Registry de Heroku** (`heroku container:push`), no el `git push`.

**Contexto / por qué:** Heroku auto-detecta el buildpack buscando `pom.xml` en la raíz del repositorio. En un monorepo el `pom.xml` vive en `backend/`, así que la detección falla (`No default language could be detected`). Las dos alternativas que se probaron y se descartaron:

1. **`git push heroku main`** — Heroku registra cada versión de código (SHA) que recibe, aunque el build falle. Un commit reintentado dispara `Duplicate Build Version Detected` y el push es rechazado incluso con un commit nuevo. El remote de Heroku queda sin refs (`git ls-remote heroku` vacío) pero el bloqueo persiste a nivel de builds.
2. **`heroku.yml` + Docker (git push)** — define `build.docker.web: backend/Dockerfile`, pero su presencia interfiere con `container:push` (`Error: No images to push`). Se eliminó.

El camino estable es el Container Registry: no pasa por la detección de versiones de git y permite apuntar a cualquier subdirectorio con `--context-path`.

### 1.2. Requisitos (se configuran una sola vez)

```bash
# Stack container (Docker, no buildpacks)
heroku stack:set container -a agro-cortex

# No hay buildpacks configurados (verificable con `heroku buildpacks`)

# Profile de Spring en producción
heroku config:set SPRING_PROFILES_ACTIVE=prod -a agro-cortex

# PostgreSQL (el addon setea PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD)
heroku addons:create heroku-postgresql:essential-0 -a agro-cortex
```

Verificación de stack: `heroku apps:info -a agro-cortex` debe mostrar `Stack: container`.

### 1.3. Desplegar

```bash
# 1. Verificar el build local (contexto = backend/)
docker build -f backend/Dockerfile -t agrocortex:test .

# 2. Login en el registry
heroku container:login

# 3. Construir y pushear la imagen (desde backend/ — OJO con el cwd)
cd backend
heroku container:push web --context-path . -a agro-cortex

# 4. Liberar el release
heroku container:release web -a agro-cortex
```

> **Importante:** `container:push --context-path .` debe ejecutarse con el directorio actual en `backend/`. Si se corre desde la raíz con `--context-path backend`, el CLI falla con `No images to push`.

### 1.4. Verificación

```bash
heroku ps -a agro-cortex        # → web.1: up
heroku logs --num 50 -a agro-cortex
curl -s -o /dev/null -w "%{http_code}\n" https://agro-cortex-e8efa9bac1c8.herokuapp.com/
```

**Nota sobre el `401`:** con el scaffold actual (sin `SecurityConfig` todavía) la API responde `401` en `/` y `/swagger-ui.html` porque Spring Security genera una password aleatoria por defecto. Es esperado hasta que exista configuración de seguridad real.

---

## 2. Frontend — Vercel

### 2.1. Modelo de despliegue

`git push` al repositorio de GitHub; Vercel importa el proyecto desde `frontend/` (root directory) y corre `npm run build`.

### 2.2. Configuración

- **Root directory:** `frontend`
- **Build command:** `npm run build`
- **Output directory:** `dist/agrocortex-frontend/browser`
- **Base framework preset:** Angular

`frontend/vercel.json` aplica los rewrites SPA (toda ruta → `index.html`) y el cacheo de assets:

```json
{
  "buildCommand": "npm run build",
  "outputDirectory": "dist/agrocortex-frontend/browser",
  "framework": "angular",
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
}
```

### 2.3. Variables de entorno

| Variable | Uso |
|---|---|
| `API_URL` | URL base de la API (ej: `https://agro-cortex-*.herokuapp.com`) |

> A diferencia del backend, en Angular las variables de entorno no se inyectan en tiempo de construcción desde `process.env`: se definen en `src/environments/` (aún no creados) o se leen en runtime. `API_URL` en Vercel se usa para generar los artefactos en el build.

---

## 3. Monorepo — reglas de oro

- **No mover `pom.xml` a la raíz.** Heroku Java buildpack no entiende subdirectorios; por eso el backend usa Docker + Container Registry y no el buildpack.
- **`heroku.yml` NO debe existir** en la raíz: rompe `container:push`.
- **`frontend/` está excluida del contexto Docker** vía `.dockerignore` de la raíz.
- **Cada app tiene su propio `.env`** documentado en `.env.example` (raíz).

---

## 4. Errores comunes y su solución

| Error | Causa | Solución |
|---|---|---|
| `No default language could be detected for this app` | `pom.xml` no está en la raíz | Usar Container Registry (no git push) |
| `you have triggered a build ... at least twice` | Misma SHA ya registrada en builds de Heroku | Desplegar con `heroku container:push` (nueva imagen) |
| `No images to push` | `heroku.yml` presente o `--context-path` mal usado | Eliminar `heroku.yml`; correr `--context-path .` desde `backend/` |
| App arranca y muere (H10) | Perfil `dev` sin PostgreSQL local | `heroku config:set SPRING_PROFILES_ACTIVE=prod` |