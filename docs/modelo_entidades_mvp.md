# Modelo de Entidades AgroCortex — MVP

Versión mínima viable del modelo de dominio para **AgroCortex**: foto + descripción libre - diagnóstico con confianza - validación humana. Contiene  la espina dorsal relacional y los atributos necesarios para persistir la información que el MVP produce.


Convenciones: `id` uuid PK para todos los agregados · `creadoEn`/`actualizadoEn` de auditoría en todas las tablas (omitidos por brevedad) · DANE: `departamento` text(2), `municipio` text(5).

---

## Cadena núcleo obligatoria

```
(Agricultor tiene Parcela tiene Cultivo) hace (Consulta tiene Sesión tiene Mensaje)
```

Esta cadena es el esqueleto: sostiene las sesiones y la persistencia. No se recorta en el MVP.

---

## 1. Agricultor

Dueño de las sesiones. Sin él no hay `agricultorId` en consulta ni sesión.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `nombre` | text | No | Máx. 60 caracteres |
| `apellido` | text | No | Máx. 60 caracteres |
| `telefono` | text | No | `+57 3XX XXX XXXX`, único |
| `email` | text | Sí | Formato email, único si existe |
| `passwordHash` | text | No | Hash bcrypt/argon2, una sola vez |
| `estado` | enum | No | `activo` \| `inactivo` |

---

## 2. Parcela

Terreno con el cultivo. Da la ubicación (DANE) y geografía a lo que cuelga de ella.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `agricultorId` | fk → Agricultor | No | 1 — N |
| `nombre` | text | No | "Finca El Tablón" |
| `areaHectareas` | numeric(10,2) | No | > 0 |
| `altitudM` | numeric(10,2) | No | msnm; de aquí se deriva el piso térmico |
| `municipioDane` | text(5) | No | `05001` |
| `departamentoDane` | text(2) | No | `05` |
| `tipoSuelo` | enum | Sí | `arcilloso` \| `arenoso` \| `franco` \| `franco-arcilloso` \| `orgánico` |
| `fechaRegistro` | timestamp | No | ISO-8601 |

Se deriva (no se persiste): `pisoTermico` → se calcula de `altitudM` cuando el motor IA lo necesite.

---

## 3. Cultivo

Especie/variedad sembrada; el "sobre qué" de cada consulta.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `parcelaId` | fk → Parcela | No | N — 1 |
| `tipo` | text | No | "café", "plátano", "tomate", "papa" |
| `variedad` | text | Sí | "Castilla", "Reina" |
| `fechaSiembra` | date | No | ISO-8601 |
| `estado` | enum | No | `en-crecimiento` \| `floración` \| `fructificación` \| `cosecha` \| `finalizado` |

---

## 4. Consulta

El agregado raíz del MVP: reúne evidencias, sesión y diagnóstico.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `agricultorId` | fk → Agricultor | No | Quién pregunta |
| `cultivoId` | fk → Cultivo | No | Sobre qué |
| `descripcionLibre` | text | No | Entrada no estructurada del agricultor |
| `estado` | enum | No | `recibida` \| `en-diagnóstico` \| `diagnosticada` \| `validada` \| `descartada` |
| `creadoEn` | timestamp | No | ISO-8601 |

---

## 5. Evidencia

La foto/archivo que dispara el diagnóstico.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `consultaId` | fk → Consulta | No | N — 1 |
| `tipoArchivo` | enum | No | `imagen` \| `video` \| `audio` \| `documento` |
| `url` | text | No | Referencia al almacenamiento |
| `latitudGps` | numeric(9,6) | Sí | EXIF |
| `longitudGps` | numeric(9,6) | Sí | EXIF |
| `fechaCaptura` | timestamp | Sí | EXIF |

---

## 6. Sesión

Contenedor de la memoria conversacional (1:1 con la consulta).

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `consultaId` | fk → Consulta | No | 1 — 1, único |
| `estado` | enum | No | `abierta` \| `cerrada` |
| `cerradoEn` | timestamp | Sí | ISO-8601 |

### 6.1. Mensaje

El contenido de la memoria. Se persiste con orden exacto.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `sesionId` | fk → Sesión | No | N — 1 |
| `autor` | enum | No | `agricultor` \| `motor-ia` |
| `tipo` | enum | No | `texto` \| `imagen` \| `seleccion` \| `confirmacion` |
| `contenido` | jsonb | No | Texto, elección o estructura de respuesta |
| `orden` | int | No | > 0, único por sesión |
| `enviadoEn` | timestamp | No | ISO-8601 |

---

## 7. Diagnóstico

Resultado del motor IA: veredicto probabilístico, no absoluto.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `consultaId` | fk → Consulta | No | 1 — 1 |
| `resumen` | text | No | "Probablemente es X o Y" |
| `nivelConfianza` | numeric(4,2) | No | 0.00 – 1.00 |
| `estado` | enum | No | `hipotesis` \| `recomendado` \| `validado` \| `descartado` |
| `creadoEn` | timestamp | No | ISO-8601 |

---

## 8. Hipótesis

Causa probable, con su confianza propia.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `diagnosticoId` | fk → Diagnóstico | No | N — 1 |
| `nombre` | text | No | "Roña del café (*Hemileia vastatrix*)" |
| `tipoCausa` | enum | No | `plaga` \| `enfermedad` \| `hongo` \| `deficiencia-nutricional` \| `estres-ambiental` |
| `probabilidad` | numeric(4,2) | No | 0.00 – 1.00 |
| `comoDiferenciar` | text | Sí | Clave para decidir entre X o Y |

---

## 9. Recomendación

Acción sugerida + **validación humana embebida** (no se requiere entidad separada en MVP).

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `diagnosticoId` | fk → Diagnóstico | No | N — 1 |
| `tipo` | enum | No | `manejo-integrado` \| `control-ecologico` \| `control-quimico` \| `referencia-extensionista` |
| `titulo` | text | No | "Aplicar manejo integrado contra la roya" |
| `detalle` | text | No | Pasos y dosis orientativas |
| `riesgo` | enum | Sí | `bajo` \| `medio` \| `alto` |
| `requiereValidacion` | bool | No | `true` para control químico |
| `estadoValidacion` | enum | No | `pendiente` \| `aprobada` \| `ajustada` \| `rechazada` |
| `validador` | text | Sí | "Ing. María López" |
| `observaciones` | text | Sí | Ajustes del extensionista |
| `validadoEn` | timestamp | Sí | ISO-8601 |

---

## Matriz de relaciones (MVP)

| Desde | Hacia | Cardinalidad | Descripción |
|---|---|---|---|
| Agricultor | Parcela | 1 — N | Un agricultor tiene muchas parcelas |
| Parcela | Cultivo | 1 — N | Una parcela contiene varios cultivos |
| Agricultor | Consulta | 1 — N | Un agricultor hace muchas consultas |
| Cultivo | Consulta | 1 — N | Un cultivo genera varias consultas |
| Consulta | Evidencia | 1 — N | Una consulta lleva varias fotos |
| Consulta | Sesión | 1 — 1 | Una consulta tiene una sesión |
| Sesión | Mensaje | 1 — N | La sesión conserva el historial |
| Consulta | Diagnóstico | 1 — 1 | Genera un diagnóstico |
| Diagnóstico | Hipótesis | 1 — N | Propone varias hipótesis |
| Diagnóstico | Recomendación | 1 — N | Deriva varias recomendaciones |

---

## Diferido al post-MVP

| Ítem | Estado | Por qué se difiere |
|---|---|---|
| Contexto ambiental (tabla) | Fuera | En MVP el contexto es parcela (DANE + altitudM) + `fechaSiembra`/`creadoEn`. La tabla aparece cuando exista una fuente real de clima |
| Validación agronómica (tabla) | Embestido en Recomendación | Basta `estadoValidacion` + `validador` + `observaciones` |
| Catálogo DANE (tabla) | Fuera | `municipioDane` + `departamentoDane` se guardan como códigos en Parcela; el catálogo entra cuando se necesite el select dependiente |
| `mundoPersistente` (sesión) | Fuera | El resumen de memoria lo genera el motor IA, no la BD |
| Atributos recortados | Fuera | `fotoPerfilUrl`, `zonaVidaHoldridge`, `ancho`, `largo`, `areaCultivada`, `etapaFenologica`, `fechaCosechaEstimada`, `prioridad`, `titulo`, `sha256`, EXIF detallado, `modeloVersion`, `faseLunar`, `velocidadVientoMs`, `porcentajeHumedad`, `temperaturaC` |