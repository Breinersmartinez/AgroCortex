# Prefijos en Commits de GitHub (Conventional Commits)

Los prefijos semánticos ayudan a mantener un historial de cambios limpio, legible y fácil de automatizar.

## Prefijos más comunes

* **`feat`**: Añade una nueva funcionalidad al proyecto.
* **`fix`**: Soluciona un error (*bug*) en el código.
* **`docs`**: Modificaciones exclusivas en la documentación (ej. README.md).
* **`style`**: Cambios de formato y estilo del código que no alteran su lógica (espacios, punto y coma).
* **`refactor`**: Reestructuración del código que no corrige errores ni añade funciones.
* **`perf`**: Cambios de código orientados a mejorar el rendimiento.
* **`test`**: Creación, modificación o corrección de pruebas unitarias o de integración.
* **`chore`**: Tareas rutinarias de mantenimiento que no modifican el código de la app (ej. actualizar paquetes).
* **`build`**: Modificaciones en el sistema de compilación o dependencias externas (ej. npm, Webpack, Gradle).
* **`ci`**: Cambios en la configuración de integración y despliegue continuo (ej. GitHub Actions).

## Estructura del mensaje

El formato estándar que debes seguir en la consola o en tu editor es:

```text
<tipo>[ámbito opcional]: <descripción breve en minúsculas y modo imperativo>
```

### Ejemplos prácticos

* `feat(auth): agregar inicio de sesión con Google`
* `fix(api): corregir error de desbordamiento en el contador`
* `docs: actualizar instrucciones de instalación en el readme`
