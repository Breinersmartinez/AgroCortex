# AGENTS.md — AgroCortex

## Que es esto
- Diagnóstico agronómico conversacional para pequeños agricultores: foto + descripción libre → diagnóstico con nivel de confianza → validación humana (MVP documentado en `docs/02-data/data-model-mvp.md`).
- Monorepo: `backend/` (Spring Boot 3.4, Java 17, Maven) + `frontend/` (Angular 19, Node 20) + CI/CD GitHub Actions (Heroku backend, Vercel frontend).
- Estado: scaffold — backend sin controllers ni entidades (solo `AgroCortexApplication.java`), frontend sin rutas ni servicios, `db/agroCortex.sql` vacío, `k8s/` y `monitoring/` con configs vacías.

## Como se corre
- Backend (dev): PostgreSQL ya corriendo en `localhost:5432` con DB/user/pass `agrocortex`; `cd backend && mvn spring-boot:run`. Perfil `dev` por defecto; Swagger en `/swagger-ui.html`.
- Frontend: `cd frontend && npm start` (`ng serve`; la URL del API va por la env `API_URL`).
- Tests: backend `mvn -B verify` (desde `backend/`); frontend `npx ng test --watch=false --browsers=ChromeHeadless --code-coverage` (desde `frontend/`).
- Docker local: `docker build -f backend/Dockerfile -t agrocortex:test .` (contexto la raíz).
- Deploy: rama `main` protegida (solo PRs); el merge dispara el workflow `Deploy` → backend a Heroku vía `heroku.yml` (`build.docker.web: backend/Dockerfile`, `run.web: java -jar app.jar`; el contexto del build de Heroku es `backend/`) y frontend a Vercel (`vercel deploy --prebuilt --prod`). Ambiente único: sin staging/production. Fallback de emergencia: `git push heroku main`.
- No hay `mvnw`: usa el `mvn` del sistema.

## Convenciones
- Backend: paquete raíz `com.agrocortex`; perfil `dev` = postgres local + `ddl-auto: update`; perfil `prod` = vars `PGHOST/PGPORT/PGDATABASE/PGUSER/PGPASSWORD` + `ddl-auto: validate`.
- Frontend: componentes standalone con su propio CSS (`app.component.ts/html/css`), `appConfig` en `app.config.ts`, rutas en `app.routes.ts`.
- Modelo de datos: fuente única `docs/02-data/data-model-mvp.md` (tablas); diagrama visual `docs/02-data/data-model-er.drawio` se edita a mano en draw.io.
- Datos (de `data-model-mvp.md`): `id` uuid PK, auditoría `creadoEn`/`actualizadoEn` en todas las tablas, DANE `departamentoDane` text(2)/`municipioDane` text(5), enums en español.
- Nombre canónico de la entidad terreno: **Sembradio**/`sembradioId`.
- Docs: filenames en inglés kebab-case, prefijo numérico por categoría (`docs/0X-*`), cada doc referencia a sus dependientes, assets en `docs/assets/`.
- Workflows: acciones ancladas a commit SHA, con `[sha] # vX` de referencia. Despliegue: merge a `main` → `Deploy` (Heroku + Vercel), ambiente único; `main` protegida (PR + checks `CI`/`Security`/`Qodana`).

## Que NO hacer
- No hardcodear credenciales ni URLs de despliegue; `.env` y `*.env` están gitignored (solo `!*.env.example`).
- No correr con perfil `prod` fuera de Heroku: sin las vars `PG*` la app muere (H10).
- No commitear `backend/target/`, `frontend/dist|coverage|node_modules/`, `.idea/`, `uploads/` (gitignored).
- No te fijes en el diff actual de whitespace (trailing spaces) en `backend/.../AgroCortexApplication.java`: es ruido, sin lógica.

## PENDIENTE (preguntar al dueño)
- PostgreSQL local: hoy se conecta a **Neon** vía env vars ya configuradas en el entorno de dev; falta decidir/implementar docker-compose para dev y/o H2 (JUnit) para pruebas — ¿cuál conviene más?
- `npm run lint` está roto: falta instalar y configurar `@angular-eslint`.
- BD de prod en Heroku: hace falta poner las vars `PG*` en `heroku config` (hoy el app arranca sin BD porque no hay entidades; con el MVP se caerá).
- Vercel: faltan los secrets `VERCEL_TOKEN`/`VERCEL_ORG_ID`/`VERCEL_PROJECT_ID` para que el job `Frontend → Vercel` pase.