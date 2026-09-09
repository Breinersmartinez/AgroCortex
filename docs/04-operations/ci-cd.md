# CI/CD — AgroCortex (GitHub Actions)

Pipeline de integración y despliegue continuo del monorepo. Complementa `deployment.md` (que describe el despliegue manual) con la versión automatizada.

## Workflows

| Workflow | Archivo | Cuándo corre | Qué hace |
|---|---|---|---|
| **CI** | `.github/workflows/ci.yml` | PR a `main` y push a `main` | Lint de workflows, compila y testea backend (Maven) y frontend (Angular/Karma headless) con coverage |
| **Deploy** | `.github/workflows/deploy.yml` | Push a `main` → staging · tag `v*` → producción · `workflow_dispatch` manual | Construye imagen y libera en Heroku (backend) y desplega en Vercel (frontend) |
| **Security** | `.github/workflows/security.yml` | Push/PR a `main` + semanal (lunes 02:00) | CodeQL (SAST), Dependency Review (SCA) y Gitleaks (secretos) |

## Estándares aplicados

- **Pinning por SHA**: toda action referencia commit completo `@<sha>` con el tag de versión como comentario. Mitiga supply-chain (verifica integridad del código ejecutado).
- **Mínimo privilegio**: cada job declara `permissions:` explícitas (p. ej. `contents: read`); CodeQL agrega `security-events: write` solo donde lo necesita.
- **Concurrency**: cancela runs obsoletos del mismo ref en CI; bloquea desplegues paralelos (`cancel-in-progress: false`) para no liberar releases fuera de orden.
- **Auditoría de flujo**: job `lint-workflows` valida la sintaxis de los YAML con actionlint antes de correr el resto.
- **Secrets por ambiente**: los secretos se leen de los *environments* `staging` y `production`, no del repositorio.
- **Entornos protegidos**: `production` debe habilitarse con `environment protection rules` (revisores requeridos) para que una tag no libere solo.

## Secretos requeridos

Configurar en Settings → Environments (`staging` y `production`):

| Secreto | Dónde | Uso |
|---|---|---|
| `HEROKU_API_KEY` | staging y production | Login + release en el Container Registry de Heroku |
| `HEROKU_APP_NAME` | staging y production | Nombre de la app Heroku (ej. `agro-cortex`) |
| `STAGING_APP_URL` / `PRODUCTION_APP_URL` | según ambiente | Smoke test del backend desplegado |
| `STAGING_API_URL` / `PRODUCTION_API_URL` | según ambiente | Variable de build del frontend (URL base de la API) |
| `VERCEL_TOKEN` | staging y production | Deploy CLI de Vercel |
| `VERCEL_ORG_ID` | staging y production | Org de Vercel |
| `VERCEL_PROJECT_ID` | staging y production | Proyecto Vercel del frontend |

## Flujo de liberación (release train)

```
── feature → PR → CI (build+test) → merge a main
                                        │
                                        ├── push a main → Deploy staging (Heroku + Vercel preview)
                                        │
                                        └── tag v1.0.0 → Deploy production (aprobación en environment)
```

1. **PR**: CI corre y actúa de *required status check* (se configura en branch protection de `main`).
2. **Merge a `main`** → despliegue automático a **staging**.
3. Cuando staging está validado, crear tag:
   ```bash
   git tag v1.0.0 && git push origin v1.0.0
   ```
   → Despliegue a **production** (requiere aprobación si el environment está protegido).

Despliegue manual alternativo: Actions → *Deploy* → *Run workflow* → elegir `staging` o `production`.

## Branch protection recomendada (Settings → Branches → `main`)

- [x] Require a pull request before merging (1 revisor)
- [x] Require status checks: `CI / Lint GitHub Actions`, `CI / Backend (Java 17 / Maven)`, `CI / Frontend (Node 20 / Angular)`, `Security / CodeQL (SAST)`
- [x] Require conversation resolution
- [x] Delete head branches after merge
- [x] Enforce admins

## Notas

- **Backend** se despliega vía el Container Registry (no `git push` a Heroku), igual que el flujo manual de `deployment.md`. No debe existir `heroku.yml` en la raíz.
- **Frontend**: el build ocurre en el runner (`npm run build`) y `vercel deploy --prebuilt` sube `dist/`. Vercel no necesita re-build.
- **Gitleaks** falla el run si detecta secretos commiteados; corre solo en push (los PRs se cubren con Dependency Review).
- El archivo `deployment.md` sigue siendo la referencia operativa de troubleshooting (H10, `No images to push`, etc.).