| Eje | Análisis |
|---|---|
| **Entrada** | Foto de la hoja/planta + descripción libre del agricultor ("las hojas se están enrollando y hay como una telaraña"). Es multimodal y no estructurada — no se puede meter en un formulario de opción múltiple sin perder información valiosa. |
| **Reglas** | Existen miles de combinaciones plaga-cultivo-clima-región-etapa fenológica. Una plaga en café en clima frío no se ve ni se comporta igual que en clima cálido. Es imposible escribir un árbol de decisión que cubra todo Colombia. |
| **Determinismo** | Se tolera variación: el output ideal es "esto probablemente es X o Y, con esta confianza, aquí tienes cómo diferenciarlos" — no un veredicto absoluto. |
| **Volumen** | Bajo: un agricultor consulta cuando tiene un problema, no millones de veces por segundo. |
| **Latencia** | Segundos a minutos es totalmente aceptable — el agricultor no está tomando una decisión de milisegundos. |
| **Costo del error** | Medio, pero reversible si el diseño es correcto: el sistema da hipótesis + recomendación de confirmación con un ingeniero agrónomo o extensionista antes de aplicar cualquier químico costoso o peligroso. |