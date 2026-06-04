Épica 1: Acceso y Gestión de Tableros
Esta épica cubre la creación del espacio de trabajo y cómo los usuarios acceden a él.

HU 1.1 - Creación de tablero: Como usuario, quiero crear un tablero introduciendo mi correo electrónico para obtener una URL única y privada de acceso.

HU 1.2 - Colaboración: Como creador de un tablero, quiero compartir la URL única con otras personas para que puedan acceder y colaborar en mi tablero.

HU 1.3 - Edición de tablero: Como usuario, quiero poder modificar los detalles de un tablero existente para mantener su información actualizada.

HU 1.4 - Bloqueo de tablero: Como usuario, quiero poder bloquear temporalmente un tablero para evitar que se añadan nuevas tarjetas, permitiendo únicamente mover las ya existentes.


Épica 2: Gestión de Listas y Tarjetas
El núcleo del sistema. Aquí se define la estructura de trabajo y el contenido.

HU 2.1 - Gestión de Listas: Como usuario, quiero crear y modificar listas dentro de un tablero para definir las diferentes fases de mi flujo de trabajo.

HU 2.2 - Creación de Tarjetas de Tarea: Como usuario, quiero crear tarjetas de tipo "tarea" dentro de una lista para asignar un trabajo o anotar información relevante.

HU 2.3 - Creación de Tarjetas de Checklist: Como usuario, quiero crear tarjetas de tipo "checklist" dentro de una lista para desglosar una tarea en múltiples pasos.

HU 2.4 - Flujo de Tarjetas: Como usuario, quiero mover una tarjeta entre diferentes listas para reflejar el avance de la tarea en el flujo de trabajo.

HU 2.5 - Finalización de Tarjetas: Como usuario, quiero marcar una tarjeta como completada para que el sistema la mueva automáticamente a una lista especial de "tarjetas completadas".


Épica 3: Clasificación y Trazabilidad
Funcionalidades para organizar visualmente la información y saber qué ha pasado en el tablero.

HU 3.1 - Etiquetas con Color: Como usuario, quiero crear etiquetas definiendo un nombre y un color, y asignarlas a las tarjetas para clasificarlas visualmente.

HU 3.2 - Historial de Acciones: Como usuario, quiero consultar el historial del tablero para revisar todas las acciones realizadas (como el movimiento de una tarjeta entre listas).


Épica 4: Características Opcionales (A elegir por el equipo)
Aquí están las HU de los requisitos extra.

HU Opcional A - Límite tarjetas: Como usuario, quiero configurar un límite máximo de "N" tarjetas para una lista, para evitar cuellos de botella en esa fase.

HU Opcional B - Flujo Obligatorio: Como usuario, quiero definir que una tarjeta deba pasar obligatoriamente por ciertas listas previas antes de llegar a una específica, para forzar un proceso estricto.

HU Opcional C - Filtrado Visual: Como usuario, quiero filtrar las tarjetas del tablero por sus etiquetas para visualizar únicamente la información que me interesa en ese momento.

HU Opcional D - Plantillas YAML: Como usuario, quiero crear un tablero subiendo un fichero YAML para inicializar rápidamente listas y tarjetas predeterminadas.

HU Opcional E - Compactación: Como usuario, quiero que el sistema archive automáticamente las tarjetas (ej. completadas hace más de 1 semana) para mantener el tablero limpio.

HU Opcional F - Autenticación por Email: Como usuario, quiero solicitar un código de acceso por email (válido por 5 minutos) para entrar a la aplicación con una capa extra de seguridad.

HU Opcional G - Permisos para Invitados: Como dueño del tablero, quiero configurar permisos de lectura/escritura a nivel de tarjeta para los usuarios invitados, protegiendo información sensible.

HU Opcional H - Reglas de Automatización: Como usuario, quiero crear reglas del tipo "Si ocurre X -> hacer Y" para automatizar flujos repetitivos en mi tablero.