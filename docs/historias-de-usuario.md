# 📖 Historias de Usuario - Tableros Kanban Colaborativos

Este documento define los requisitos funcionales del sistema mediante Historias de Usuario (HU), agrupadas en Épicas según su ámbito de aplicación. Se han cubierto tanto los requisitos obligatorios como las características opcionales propuestas en el enunciado del proyecto.

---

## 🚀 Épica 1: Acceso y Gestión de Tableros
*Esta épica cubre la creación del espacio de trabajo base y el sistema de acceso y control a nivel de tablero.*

* **HU 1.1 - Creación de tablero:** **Como** usuario, **quiero** crear un tablero introduciendo mi correo electrónico, **para** obtener una URL única y privada de acceso.
* **HU 1.2 - Colaboración:** **Como** creador de un tablero, **quiero** compartir la URL única con otras personas, **para** que puedan acceder y colaborar en mi espacio de trabajo.
* **HU 1.3 - Edición de tablero:** **Como** usuario, **quiero** poder modificar los detalles de un tablero existente, **para** mantener su información y título actualizados.
* **HU 1.4 - Bloqueo de tablero:** **Como** usuario, **quiero** poder bloquear temporalmente un tablero, **para** evitar que se añadan nuevas tarjetas, permitiendo únicamente el movimiento de las ya existentes (Ej: durante un cierre de Sprint).

---

## 📋 Épica 2: Gestión de Listas y Tarjetas
*El núcleo del sistema Kanban. Define la estructura del trabajo, los tipos de tareas y el flujo de estados.*

* **HU 2.1 - Gestión de Listas:** **Como** usuario, **quiero** crear y modificar listas dentro de un tablero, **para** definir las diferentes fases de mi flujo de trabajo (Ej: *To Do, Doing, Done*).
* **HU 2.2 - Creación de Tarea Simple:** **Como** usuario, **quiero** crear tarjetas de tipo "tarea" dentro de una lista, **para** asignar un trabajo o anotar información relevante de forma rápida.
* **HU 2.3 - Creación de Checklist:** **Como** usuario, **quiero** crear tarjetas de tipo "checklist" dentro de una lista, **para** desglosar una tarea compleja en múltiples pasos de verificación.
* **HU 2.4 - Flujo de Tarjetas:** **Como** usuario, **quiero** mover una tarjeta libremente entre diferentes listas, **para** reflejar el avance de la tarea en el flujo de trabajo visual.
* **HU 2.5 - Finalización de Tarjetas:** **Como** usuario, **quiero** marcar una tarjeta como completada, **para** que el sistema la mueva automáticamente a una lista especial designada como "tarjetas completadas".

---

## 🔍 Épica 3: Clasificación y Trazabilidad
*Funcionalidades orientadas a organizar visualmente la información y mantener un registro de auditoría.*

* **HU 3.1 - Etiquetas de Color:** **Como** usuario, **quiero** crear etiquetas definiendo un nombre y un color, y asignarlas a mis tarjetas, **para** clasificarlas visualmente por temática o prioridad.
* **HU 3.2 - Historial de Acciones:** **Como** usuario, **quiero** consultar el historial del tablero, **para** revisar una traza de todas las acciones realizadas (como la creación o el movimiento de una tarjeta entre listas).

---

## ⭐ Épica 4: Características Avanzadas (Opcionales)
*Historias de usuario correspondientes a los requisitos extra implementados para maximizar la calidad y funcionalidad del sistema.*

* **HU Opcional A - Límites:** **Como** usuario, **quiero** configurar un límite máximo de "N" tarjetas para una lista, **para** evitar cuellos de botella en una fase concreta del proyecto.
* **HU Opcional B - Filtrado Visual:** **Como** usuario, **quiero** filtrar instantáneamente las tarjetas del tablero mediante sus etiquetas, **para** visualizar únicamente la información que me interesa en ese momento.
* **HU Opcional C - Plantillas YAML:** **Como** usuario, **quiero** crear un tablero cargando un fichero YAML, **para** inicializar rápidamente un espacio de trabajo con listas y tarjetas predeterminadas.
* **HU Opcional D - Compactación Automática:** **Como** usuario, **quiero** que el sistema archive automáticamente las tarjetas antiguas (ej. no completadas en más de una semana), **para** mantener el tablero limpio mediante un mantenimiento en segundo plano.
* **HU Opcional E - Autenticación (Passwordless):** **Como** usuario, **quiero** solicitar un código de acceso temporal por email (válido por 5 minutos), **para** entrar a la aplicación con una capa extra de seguridad sin necesidad de recordar contraseñas.
* **HU Opcional F - Gestión de Permisos Granulares:** **Como** dueño del tablero, **quiero** asignar roles de lectura (`READ`) o escritura (`WRITE`) a los usuarios invitados, **para** proteger la información y restringir quién puede editar las tarjetas.
* **HU Opcional G - Reglas de Automatización:** **Como** usuario, **quiero** crear reglas lógicas del tipo *"Si ocurre el evento X -> hacer la acción Y"*, **para** que el sistema realice tareas repetitivas por mí.
