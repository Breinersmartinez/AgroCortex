# Modelo de Entidades AgroCortex

Modelo de dominio normalizado para **AgroCortex**: diagnóstico agronómico conversacional para pequeños agricultores en Colombia. Este documento define las entidades, sus atributos y las relaciones necesarias para sostener el flujo del diagrama `docs/public/architecture_flow.drawio` respetando las restricciones de `docs/datos_analisis_agricultura.md` (entrada multimodal no estructurada, output no determinista con confianza, validación humana antes de aplicar químicos).

Convenciones generales:

- **Tipos de dato**: valores orientativos de dominio (`uuid`, `text`, `int`, `numeric`, `timestamp`, `jsonb`, `enum`, `bool`). No se prescribe un motor de BD.
- **`id`**: clave primaria; se sugiere `uuid` para todos los agregados.
- **`creadoEn` / `actualizadoEn`**: campos de auditoría en TODAS las entidades (se omiten en cada tabla por brevedad excepto donde importan).
- **Código DANE**: `departamento` = 2 dígitos, `municipio` = 5 dígitos (2 del departamento + 3 del municipio).
- **Cardinalidad**: se indica en la matriz de relaciones con los dos extremos explícitos.

---

## 1. Agricultor (Usuario)

Productor que usa el sistema para diagnosticar problemas en sus cultivos. Identidad del usuario registrado.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `nombre` | text | No | Máx. 60 caracteres |
| `apellido` | text | No | Máx. 60 caracteres |
| `telefono` | text | No | Formato `+57 3XX XXX XXXX`, único |
| `email` | text | Sí | Formato email, único si existe |
| `passwordHash` | text | No | Hash de bcrypt/argon2; aparece UNA sola vez |
| `estado` | enum | No | `activo` \| `inactivo` (baja, no se borra) |
| `fotoPerfilUrl` | text | Sí | URL del almacenamiento de imágenes |

No se modela `rol`: el análisis solo exige la validación humana en el flujo, que es una relación con un actor externo, no un rol dentro del sistema.

Relaciones: **1 — N** parcela/sembradío · **1 — N** sesión de conversación · **1 — N** consulta agronómica.

---

## 2. Parcela (Sembradío)

Terreno donde está el cultivo. Nodo intermedio que agrupa cultivos y fija el contexto geográfico y de suelo que comparten.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `agricultorId` | fk → Agricultor | No | Relación 1 — N |
| `nombre` | text | No | Nombre local: "Parcela la loma", "Finca El Tablón" |
| `areaHectareas` | numeric(10,2) | No | En hectáreas, > 0 |
| `ancho` | numeric(10,2) | No | En metros, > 0 (referencia de plagas por densidad) |
| `largo` | numeric(10,2) | No | En metros, > 0. Se renombra `longitud` → `largo` para no chocar con coordenadas |
| `altitudM` | numeric(10,2) | No | Metros sobre nivel del mar; se mide en campo con ~20-30 plantas de muestra |
| `municipioDane` | text(5) | No | Código DANE de 5 dígitos (FK a Catálogo DANE, ver §11) |
| `departamentoDane` | text(2) | No | Código DANE de 2 dígitos (denormalizado por consulta común) |
| `tipoSuelo` | enum | No | `arcilloso` \| `arenoso` \| `franco` \| `franco-arcilloso` \| `orgánico` |
| `pisoTermico` | enum | No | Derivado de `altitudM`/zona: `cálido` \| `medio` \| `frío` \| `páramo` |
| `zonaVidaHoldridge` | text | Sí | Detalle técnico opcional, ej. "bh-MBT" (bosque húmedo montano bajo) |
| `fechaRegistro` | timestamp | No | ISO-8601 |

Relaciones: **N — 1** agricultor · **1 — N** cultivo.

---

## 3. Cultivo

Especie/variedad sembrada en una parcela en un ciclo productivo. Contexto agronómico sobre el que se hacen las consultas.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `parcelaId` | fk → Parcela | No | Relación N — 1 |
| `tipo` | text | No | Especie: "café", "plátano", "tomate", "papa" (catálogo o texto, ver nota) |
| `variedad` | text | Sí | "Castilla", "Reina", "Caturra", "Criolla" |
| `fechaSiembra` | date | No | ISO-8601 |
| `fechaCosechaEstimada` | date | Sí | ISO-8601; derivable de fenología |
| `estado` | enum | No | `en-crecimiento` \| `floración` \| `fructificación` \| `cosecha` \| `finalizado` |
| `areaCultivada` | numeric(10,2) | No | Hectáreas dentro de la parcela, ≤ `areaHectareas` |
| `etapaFenologica` | enum | Sí | BBCH simplificado: `germinación` \| `desarrollo-vegetativo` \| `floración` \| `maduración` |

Nota: `tipo` como enum con catálogo propio evita fragmentación ("café" vs "Cafe") y habilita el cruce plaga-cultivo-región que pide el análisis.

Relaciones: **N — 1** parcela · **1 — N** consulta agronómica.

---

## 4. Consulta Agronómica

Momento en que el agricultor pide diagnóstico sobre un cultivo. Agrega evidencias, contexto y la conversación derivada. Es el agregado raíz del dominio.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `agricultorId` | fk → Agricultor | No | Quién pregunta |
| `cultivoId` | fk → Cultivo | No | Sobre qué cultivo |
| `titulo` | text | No | Resumen del motivo: "Hojas enrolladas con telaraña" |
| `descripcionLibre` | text | No | Descripción no estructurada del agricultor |
| `estado` | enum | No | `recibida` \| `en-diagnóstico` \| `diagnosticada` \| `validada` \| `descartada` |
| `prioridad` | enum | Sí | `baja` \| `media` \| `alta` (si el agricultor marca urgencia) |
| `createdAt` | timestamp | No | ISO-8601 |

Relaciones: **N — 1** agricultor · **N — 1** cultivo · **1 — N** evidencia · **1 — N** contexto ambiental (histórico) · **1 — N** mensaje de conversación · **1 — 1** diagnóstico (una vez generado).

---

## 5. Evidencia multimodal

Cada foto/archivo que acompaña la consulta. Guarda la referencia al archivo y los metadatos EXIF/GPS para contexto geográfico y temporal automático.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `consultaId` | fk → Consulta | No | Relación N — 1 |
| `tipoArchivo` | enum | No | `imagen` \| `video` \| `audio` \| `documento` |
| `url` | text | No | Referencia al bucket/almacenamiento (S3, etc.) |
| `sha256` | text | No | Hash de integridad (evita duplicados, soporta auditoría) |
| `latitudGps` | numeric(9,6) | Sí | EXIF; decimal, ej. 4.711000 |
| `longitudGps` | numeric(9,6) | Sí | EXIF; decimal, ej. -74.072000 |
| `altitudGpsM` | numeric(10,2) | Sí | EXIF |
| `fechaCaptura` | timestamp | Sí | EXIF |
| `orientacion` | text | Sí | EXIF: "landscape", "portrait" |
| `resolucionW` | int | Sí | EXIF, px |
| `resolucionH` | int | Sí | EXIF, px |

El contenido interpretado (qué se ve en la foto) NO se fuerza aquí: vive en el diagnóstico. Eso preserva la flexibilidad multimodal que exige el análisis.

Relaciones: **N — 1** consulta.

---

## 6. Contexto ambiental

Ambiente en que ocurre la consulta: clima y condiciones regionales. Enriquecible con sensores o servicios externos; su propósito es el cruce **plaga-cultivo-clima-región**.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `consultaId` | fk → Consulta | No | Relación N — 1 (histórico por consulta) |
| `parcelaId` | fk → Parcela | No | Hereda ubicación y piso térmico |
| `temperaturaC` | numeric(5,2) | Sí | °C, ej. 18.4 |
| `humedadRelativaPct` | numeric(5,2) | Sí | %, ej. 76.3 |
| `precipitacionMm` | numeric(6,2) | Sí | mm acumulados recientes |
| `velocidadVientoMs` | numeric(5,2) | Sí | m/s |
| `faseLunar` | text | Sí | Relevante en prácticas tradicionales |
| `municipioDane` | text(5) | No | Código DANE (snapshot del catálogo) |
| `pisoTermico` | enum | No | `cálido` \| `medio` \| `frío` \| `páramo` |
| `zonaVidaHoldridge` | text | Sí | Detalle técnico, ej. "bh-MBT" |
| `fuenteDatos` | enum | Sí | `sensor` \| `origen-externo` \| `referencia-local` |

Piso térmico tanto en parcela como en contexto: en parcela es el del predio; en contexto es el del momento de la consulta (ambos pueden diferir si hay variación estacional).

Relaciones: **N — 1** consulta · **N — 1** parcela.

---

## 7. Sesión de conversación

Memoria persistida del diálogo entre el agricultor y el motor IA. Una sesión agrupa una consulta y conserva el historial completo de mensajes (la memoria que el usuario pidió persistir).

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `consultaId` | fk → Consulta | No | Relación 1 — 1 (una sesión por consulta) |
| `mundoPersistente` | jsonb | Sí | Resumen/estado acumulado que el motor usa como memoria de contexto entre mensajes |
| `estado` | enum | No | `abierta` \| `cerrada` |
| `cerradoEn` | timestamp | Sí | ISO-8601 |

El **historial de mensajes** es parte de la memoria: se modela como entidad `MensajeConversación` (ver §7.1). La sesión es el contenedor, los mensajes el contenido.

Relaciones: **1 — 1** consulta · **1 — N** mensaje.

### 7.1. Mensaje conversación

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `sesionId` | fk → Sesión | No | Relación N — 1 |
| `autor` | enum | No | `agricultor` \| `motor-ia` |
| `tipo` | enum | No | `texto` \| `imagen` \| `seleccion` \| `confirmacion` |
| `contenido` | jsonb | No | Texto libre, elección, o estructura de la respuesta |
| `orden` | int | No | Secuencia entera (> 0, única por sesión) para reconstruir el orden exacto |
| `enviadoEn` | timestamp | No | ISO-8601 |

Relaciones: **N — 1** sesión.

---

## 8. Diagnóstico

Resultado del motor IA sobre una consulta. Es un veredicto **probabilístico, no absoluto** (con nivel de confianza), coherente con `datos_analisis_agricultura.md`.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `consultaId` | fk → Consulta | No | Relación 1 — 1 |
| `resumen` | text | No | Síntesis legible: "Probablemente es X o Y" |
| `nivelConfianza` | numeric(4,2) | No | 0.00 – 1.00 |
| `modeloVersion` | text | No | "agrocortex-v1.2", para trazabilidad |
| `estado` | enum | No | `hipotesis` \| `recomendado` \| `validado` \| `descartado` |
| `creadoEn` | timestamp | No | ISO-8601 |

Relaciones: **1 — 1** consulta · **1 — N** hipótesis · **1 — N** recomendación.

---

## 9. Hipótesis

Causa probable propuesta por el motor: plaga, enfermedad o condición. Cada hipótesis tiene su propio nivel de confianza.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `diagnosticoId` | fk → Diagnóstico | No | Relación N — 1 |
| `nombre` | text | No | Ej. "Roña del café (*Hemileia vastatrix*)" |
| `tipoCausa` | enum | No | `plaga` \| `enfermedad` \| `hongo` \| `deficiencia-nutricional` \| `estres-ambiental` |
| `probabilidad` | numeric(4,2) | No | 0.00 – 1.00; suma acotada con las demás |
| `evidenciaResumida` | text | Sí | Qué la apoya: "manchas cloróticas en el envés" |
| `comoDiferenciar` | text | Sí | Clave para decidir entre X o Y |
| `validaPorAgronomo` | bool | No | `false` por defecto |

Relaciones: **N — 1** diagnóstico · **1 — N** validación agronómica (la de una hipótesis).

---

## 10. Recomendación

Acción sugerida derivada del diagnóstico, **condicionada a validación humana**. Nunca es instrucción definitiva de aplicación de químicos.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `diagnosticoId` | fk → Diagnóstico | No | Relación N — 1 |
| `tipo` | enum | No | `manejo-integrado` \| `control-ecologico` \| `control-quimico` \| `referencia-extensionista` \| `seguimiento` |
| `titulo` | text | No | "Aplicar manejo integrado contra la roya" |
| `detalle` | text | No | Pasos, dosis orientativas, momentos |
| `riesgo` | enum | Sí | `bajo` \| `medio` \| `alto` (peligro químico) — se marca antes de aplicar |
| `requiereValidacion` | bool | No | `true` para control químico, `false` para ecológico |

Relaciones: **N — 1** diagnóstico · **1 — 1** validación agronómica.

---

## 11. Catálogo DANE (referencia)

Catálogo normalizado de departamento + municipio con código DANE. Es un refactor de la opción "texto libre" hacia una entidad de referencia que permite el cruce regional que pide el análisis.

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `departamentoDane` | text(2) | No | Código DANE de 2 dígitos, ej. `05` (Antioquia) |
| `municipioDane` | text(5) | No | Código DANE de 5 dígitos, ej. `05001` (Medellín) |
| `nombreDepartamento` | text | No | "Antioquia" |
| `nombreMunicipio` | text | No | "Medellín" |
| `pisoTermicoDefault` | enum | Sí | Por municipio, según rango altitudinal |
| `zonaHoldridgeDefault` | text | Sí | si está disponible |

Relaciones: referencia para parcela y contexto ambiental.

---

## 12. Contexto colombiano (decisión de captura)

**Ubicación por código DANE, con interfaz fácil para el usuario.** Tres opciones documentadas:

1. **Select dependiente (recomendada).** El usuario elige departamento y luego municipio en un desplegable filtrado. El código DANE se guarda de forma transparente. Cero tipeo, cero errores de escritura.
2. **Autocompletado con búsqueda por texto.** "arm..." → "Armenia"; al elegir se guarda el código. Requiere índice de búsqueda.
3. **Sugerencia por GPS (foto EXIF).** Si la foto tiene GPS, se sugiere el municipio y el usuario confirma; combina la opción 1 con los metadatos ya modelados.

## 13. Matriz de relaciones

| Desde | Hacia | Cardinalidad | Descripción |
|---|---|---|---|
| Agricultor | Parcela | 1 — N | Un agricultor tiene muchas parcelas |
| Parcela | Cultivo | 1 — N | Una parcela contiene varios cultivos |
| Agricultor | Consulta | 1 — N | Un agricultor hace muchas consultas |
| Cultivo | Consulta | 1 — N | Un cultivo genera varias consultas en su ciclo |
| Consulta | Evidencia | 1 — N | Una consulta lleva varias fotos/archivos |
| Consulta | Contexto ambiental | 1 — N | Histórico de contexto por consulta |
| Consulta | Sesión | 1 — 1 | Una consulta tiene una sesión de conversación |
| Sesión | Mensaje | 1 — N | La sesión conserva el historial de mensajes |
| Consulta | Diagnóstico | 1 — 1 | Genera un diagnóstico (tras el motor IA) |
| Diagnóstico | Hipótesis | 1 — N | Un diagnóstico propone varias hipótesis |
| Diagnóstico | Recomendación | 1 — N | Un diagnóstico deriva varias recomendaciones |
| Recomendación | Validación agronómica | 1 — 1 | Antes de aplicar químicos |
| Hipótesis | Validación agronómica | 1 — N | Cada hipótesis es validable por un agrónomo |
| Parcela | Catálogo DANE | N — 1 | La parcela referencia municipio DANE |
| Contexto ambiental | Catálogo DANE | N — 1 | El contexto referencia municipio DANE |

## 14. Validación agronómica

Registro de la confirmación humana por un agrónomo/extensionista antes de cualquier químico. Es el punto que hace reversible el costo del error (mitigación del riesgo del análisis).

| Atributo | Tipo | ¿Opcional? | Restricción / Ejemplo |
|---|---|---|---|
| `id` | uuid | No | PK |
| `recomendacionId` | fk → Recomendación | No | Relación 1 — 1 |
| `hipotesisValidadaId` | fk → Hipótesis | Sí | Qué hipótesis confirma (opcional si valida solo la recomendación) |
| `validador` | text | No | Nombre/entidad: "Ing. María López – Corpoica" |
| `dictamen` | enum | No | `aprobada` \| `ajustada` \| `rechazada` |
| `observaciones` | text | Sí | Ajustes del extensionista |
| `validadoEn` | timestamp | No | ISO-8601 |

Relaciones: **1 — 1** recomendación · **N — 1** hipótesis.

---

## 15. Trazabilidad del diagrama (.drawio)

| Nodo del diagrama | Entidad en este modelo |
|---|---|
| Agricultor | §1 Agricultor |
| Parcela | §2 Parcela (Sembradío) |
| Cultivo | §3 Cultivo |
| Consulta Agronómica | §4 Consulta Agronómica |
| Evidencias multimodal | §5 Evidencia multimodal |
| Contexto ambiental | §6 Contexto ambiental |
| Conversación | §7 Sesión + §7.1 Mensaje |
| MOTOR IA AgroCortex | Lógica de proceso (no se persiste) — la memoria persiste en §7 |
| Diagnóstico | §8 Diagnóstico |
| Hipótesis | §9 Hipótesis |
| Recomendación | §10 Recomendación |
| Validación agronómica | §14 Validación agronómica |

Figura: del diagrama, el nodo **MOTOR IA** no tiene tabla propia; lo que se persiste es la sesión que alimenta y la que guarda sus outputs (Diagnóstico). El resto del grafo tiene correspondencia 1:1.