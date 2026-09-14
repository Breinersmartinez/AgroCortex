---
name: documenta-codigo
description: Detecta código backend Java sin documentación pública y propone Javadoc de contrato en español (dominio, puertos, casos de uso, adaptadores, controllers), respetando la regla de no comentar lo obvio y sin mencionar Spring/JPA en el dominio; read-only por defecto, aplica con confirmación por archivo y no toca nada más del archivo. Usar cuando el usuario pida "documenta el código", "completa el javadoc del backend", "qué hay sin documentar" o tras crear entidades/puertos/casos de uso.
---

# documenta-codigo

Detecta elementos públicos de backend Java sin Javadoc y propone documentación de contrato en
español. Solo backend: el frontend TypeScript no se toca.

## Qué hace y qué no

- **Escanea** `backend/src/main/java` (`*.java`) y detecta qué público carece de Javadoc.
- **Propone** por archivo un Javadoc de 1-3 líneas que describe el contrato/capacidad (qué hace y
  qué garantiza), no la implementación, con `@param`/`@return` solo cuando aportan.
- **Read-only por defecto.** La salida es un reporte; la edición ocurre únicamente con confirmación
  por archivo y solo inserta los bloques Javadoc.
- **No refactoriza**, no corrige estilo/nombres/imports, no documenta lógica obvia ni genera
  documentación de API HTTP (eso corre por springdoc/Swagger).

## Qué se documenta y qué no

**Se documenta (públicos):**
- Clases e interfaces de dominio: entidades, value objects, enums de negocio.
- Puertos: interfaces de aplicación (salidas hacia adaptadores) y de entrada de casos de uso.
- Casos de uso, adaptadores (mencionando "hacia qué sistema": BD, LLM, tiempo…) y controllers
  (API fina).
- `package-info.java` si el paquete lo tiene.

**NO se documenta:**
- Getters/setters triviales y elementos privados/protegidos.
- Overrides que ya heredan el contrato (salvo que aporten algo nuevo).
- Implementaciones que se explican solas: la regla "no comentar lo obvio" gana.
- Contradicción: los comentarios de línea existentes que contradigan el contrato se reportan pero
  no se duplican ni se borran.

## Flujo de trabajo

### Paso 1 — Escanear
`glob backend/src/main/java/**/*.java` y leer los archivos; clasificar por paquete (dominio /
aplicación / infraestructura / adapters / controller).

### Paso 2 — Clasificar públicos
Listar por archivo los elementos públicos sin Javadoc y los ya documentados. Reportar archivos sin
públicos como exentos.

### Paso 3 — Redactar propuestas
Para cada elemento, redactar 1-3 líneas en español:
- Dominio/entidades: describe el concepto de negocio y su invariante (nada de Persistencia).
- Puertos/casos de uso: describe la capacidad que expone.
- Adaptadores: menciona "hacia qué sistema" se conecta.
- Solo incluir `@param`/`@return` cuando aporten (no `@param nombre el nombre`).

### Paso 4 — Aplicar (solo con confirmación)
Mostrar el reporte y por cada archivo confirmado insertar únicamente los bloques Javadoc en su
posición; no reformatear, reordenar ni editar nada más del archivo.

### Paso 5 — Verificar
Re-escaneo: los archivos confirmados ya no deben reportar los elementos documentados.

## Casos borde

| Caso | Comportamiento esperado |
|---|---|
| Archivo sin elementos públicos | Se reporta como exento. |
| Clase ya totalmente documentada | No se toca; figura como "ya documentada". |
| Override con contrato heredado | Sin Javadoc nuevo salvo comportamiento distinto. |
| Javadoc parcial (falta `@param` informativo) | Propone solo completar lo faltante. |
| Comentarios de línea contradictorios | Se reporta la contradicción; nada se borra ni se duplica. |
| Elemento en paquete de dominio | Javadoc sin menciones a Spring/JPA (el dominio no se acopla). |

## Límites

- Solo backend Java; el frontend queda fuera.
- Solo documentación; el refactor que el archivo necesite se menciona, no se ejecuta.
- No genera doc de API HTTP ni corrige estilo.
- Ante cualquier duda (qué documentar, cómo nombrar la capacidad), se pregunta en vez de inventar.