# Generador del diagrama entidad-relación (diagram-as-code)

Genera el diagrama ER de AgroCortex a partir de una **fuente única de verdad**,
de forma determinista y validada.

## Fuente única de verdad

`docs/02-data/er/model.yaml` — modelo de datos MVP (entidades, atributos,
relaciones, versión semver). **Este es el único archivo que se edita.**

| Artefacto generado | Uso |
|---|---|
| `docs/02-data/data-model-er.drawio` | Diagrama editable en draw.io |
| Bloque `erDiagram` en `docs/02-data/data-model-mvp.md` | Vista en GitHub / docs |

Prohibido editar a mano los artefactos: cualquier divergencia con `model.yaml`
hace fallar el job `diagram` de CI.

## Comandos

```bash
python3 -m venv .venv && .venv/bin/pip install -r requirements.txt
.venv/bin/python generate.py          # regenera los artefactos
.venv/bin/python generate.py --check  # valida sync sin escribir (CI)
```

## Cómo cambiar el modelo

1. Editar `docs/02-data/er/model.yaml` (nombres `UPPER_SNAKE`, atributos
   `camelCase`, subir `version` semver con cada cambio).
2. `python generate.py` y revisar el diff generado en el PR.
3. CI valida: XML bien formado, ids únicos, sin `<mxCell>` anidados, edges
   referenciando entidades existentes y artefactos en sync.

## Convenciones del diagrama

- Notación crow's foot. Relaciones `(1 — N)` / `(1 — 1)`; `PK` clave primaria,
  `FK` clave foránea, `UQ` único. `LEGEND` en el lienzo lo documenta.
- Layout Stack Layout (UML class): cabecera de color + filas como celdas planas
  (sin `shape=table` ni `<mxCell>` anidados, compatibles con cualquier cliente).
- La salida es determinista: sin fechas ni rutas locales, para CI con diff.

## Validación

- Local: `generate.py --check` + apertura en draw.io.
- CI: job `diagram` en `.github/workflows/ci.yml` corre `--check`.
- Opcional (desarrollador): decodificar el `.drawio` con el runtime mxGraph
  (`node` + `mxgraph@4.2.2` en jsdom) para detectar incompatibilidades de carga.