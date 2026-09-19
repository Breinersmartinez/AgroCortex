# Plan — reserva de puestos del laboratorio

**Spec fuente:** [especificacion-reserva-puestos-laboratorio.html](especificacion-reserva-puestos-laboratorio.html)  
**Responsable:** Breiner Martínez  
**Alcance de este ciclo:** permitir reservar un puesto libre y conservar la reserva en el navegador.

## Tareas

| # | Resultado | Responsable | Depende de | Estimación | Criterio de aceptación |
|---|---|---|---|---:|---|
| 1 | Estructura visual de 20 puestos y 7 franjas de dos horas. | Breiner Martínez | — | 1 h | Al abrir el HTML se ven los puestos 1–20 y las franjas 06–08 a 18–20. |
| 2 | Reserva de un puesto libre asociada a un nombre. | Breiner Martínez | 1 | 2 h | Con un nombre escrito, un clic en una celda libre la marca como reservada y muestra el nombre; sin nombre no reserva. |
| 3 | Persistencia local y recuperación segura de reservas. | Breiner Martínez | 2 | 1 h | Tras reservar y recargar, la celda continúa reservada; si el almacenamiento guardado es inválido, la vista sigue disponible y muestra un aviso. |
| 4 | Verificación manual de los criterios del ciclo. | Breiner Martínez | 1, 2, 3 | 30 min | Se registra evidencia de apertura local, reserva, bloqueo de una celda ya reservada y persistencia tras recargar. |

## Orden y dependencias

Primero se construye la cuadrícula, porque es la superficie sobre la cual se selecciona una reserva. La reserva depende de esa cuadrícula; la persistencia depende de que exista una reserva; la verificación solo empieza cuando las tres anteriores pueden comprobarse.

## Primera tarea

**Tarea 1.** No tiene dependencias y puede empezar de inmediato.

## Fuera de este ciclo

- Cancelación de reservas: se difiere para mantener el mínimo verificable del reto.
- Inicio de sesión, servidor y base de datos: están fuera del alcance de una aplicación HTML autónoma.
- Reglas institucionales de sanciones por inasistencia: requieren decisión de producto adicional.
