# AgroCortex

![alt text](docs/public/assets/problem_domain_definition_layer/Problem_Domain_Definition_Layer.jpeg)


Diagnóstico agronómico conversacional para pequeños agricultores (foto + descripción de la plaga/cultivo)






Entidades:

Usuario (agricultor)

id

nombre
 
apellido

telefono

email

passwordHash

passwordHash

estado

Relaciones:

Un agricultor tiene muchos semdradios

un agricultor hace muchas consultas (sesiones)


sembradio (Terreno donde esta el cultivo ):


id

nombre

areaHectareas

ancho

longitud (largo)

altitud (Se miden minimo unas 20-30 plantas)

municipio 

departamento

tipoSuelo

fechaRegistro

relaciones:

Agricultor 1 - N sembradios

sembradio 1 - N cultivos



cultivo:

id

tipo

variedad

fechaSiembra

fechaCosechaEstimada

estado

areaCultivada