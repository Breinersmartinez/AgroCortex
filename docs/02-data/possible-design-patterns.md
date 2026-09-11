| Patrón                | Dónde                      | Para qué                                                           |
| --------------------- | -------------------------- | ------------------------------------------------------------------ |
| **Repository**        | Application/Infrastructure | Desacoplar dominio/casos de uso de PostgreSQL/JPA                  |
| **Adapter**           | Infrastructure             | Conectar puertos con JPA, JWT, APIs externas, almacenamiento, etc. |
| **Strategy**          | Aplicación/dominio         | Cambiar algoritmos/proveedores sin modificar el caso de uso        |
| **Factory**           | Domain/Application         | Crear objetos complejos de forma controlada                        |
| **Facade / Use Case** | Application                | Exponer una operación de negocio como una unidad clara             |


Justificacion del porque el uso de estos patrones de diseño: 

| Patrón                | Justificación                                                                                                                                                                                                                         |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Repository**        | Permite que los casos de uso trabajen con abstracciones y no dependan directamente de JPA o PostgreSQL. Esto facilita cambiar la persistencia y mantener el dominio aislado de detalles técnicos.                                     |
| **Adapter**           | Se utilizará para conectar los puertos de la aplicación con tecnologías externas, como JPA, JWT, almacenamiento o proveedores de IA. Así, los cambios de infraestructura no obligan a modificar la lógica de negocio.                 |
| **Strategy**          | AgroCortex puede necesitar diferentes formas de análisis o distintos proveedores de IA. Strategy permite intercambiarlos sin modificar el caso de uso que coordina el diagnóstico.                                                    |
| **Factory**           | Se empleará cuando la creación de una entidad implique validaciones o reglas que hagan conveniente centralizar su construcción, evitando objetos inválidos y manteniendo esas reglas dentro del dominio.                              |
| **Facade / Use Case** | Cada operación del sistema se expone como un caso de uso claramente definido, concentrando la coordinación de la lógica necesaria para completar una acción de negocio y evitando que los controladores contengan lógica empresarial. |
