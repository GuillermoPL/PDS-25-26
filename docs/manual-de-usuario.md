# 📖 Manual de Usuario - Gestor de Tableros Kanban

---

## 🚀 1. Acceso a la Aplicación

Para simplificar el acceso y mantener la seguridad, el sistema utiliza un método de inicio de sesión ágil sin contraseñas.
1. Abre la aplicación.
2. Introduce tu **correo electrónico**.
3. Si es tu primera vez, se creará tu cuenta. Si ya tienes una, recuperarás tu sesión con todos tus tableros.

![Pantalla de inicio de sesión](./docs/Images/Pantalla_inicial.png)

---

## 📋 2. El Menú Principal y Creación de Tableros

Al entrar, verás tu panel principal ("Dashboard"). Desde aquí puedes gestionar tus espacios de trabajo.

### Crear un Tablero Vacío
Haz clic en el botón azul **+ Crear Nuevo Tablero** y dale un título para empezar con un espacio en blanco.

![Pantalla de creación de tablero](./docs/Images/Pantalla_crear_tablero.png)

### Crear desde Plantilla
Si no quieres empezar de cero, puedes pulsar **Crear desde plantilla**. Esto cargará un archivo base (por ejemplo, `basica.yaml`) que configurará automáticamente las listas iniciales (como "Por Hacer", "En Progreso" y "Hecho").

![Seleccionar plantilla YAML](./docs/Images/Crear_plantilla.png)
![Tablero generado desde plantilla](./docs/Images/Plantilla.png)

---

## 🧭 3. Navegación y Gestión del Tablero

Una vez dentro de un tablero, encontrarás una barra superior de herramientas.

* **Volver a Mis Tableros:** Este botón en la esquina superior izquierda te permite regresar al menú principal en cualquier momento.

* **Compartir y Dar Permisos:** Invita a otros usuarios a colaborar pulsando en **Compartir**. Introduce su correo y asígnales permisos de Lectura (`READ`) o Escritura (`WRITE`). 

![Menú para compartir tablero](./docs/Images/Compartir.png)

---

## 🗂️ 4. Estructura: Listas y Tareas Completadas

El trabajo se organiza en columnas verticales llamadas Listas.

### Añadir Listas
Haz clic en **+ Añadir Lista** en la esquina superior derecha y asígnale un nombre para crear una nueva fase en tu flujo de trabajo.

![Creación de una nueva lista](./docs/Images/Pantalla_crear_lista.png)

### Configurar Lista de Completadas
Puedes designar una lista específica como la de tareas finalizadas desde el menú de opciones (botón con tres puntos `...`). Las tarjetas que se muevan a esta lista se darán por concluidas automáticamente y cambiarán su aspecto visual para reflejar su estado.

![Configuración de la lista de completadas](./docs/Images/Seleccionar_completadas.png)
![Aspecto visual de una tarjeta completada](./docs/Images/Tarjeta_completada.png)

---

## 📝 5. Creación de Tarjetas (Tareas)

Haz clic en **+ Añadir tarjeta** dentro de cualquier lista para crear una nueva tarea. Existen dos modalidades:

### Tarea Estándar (TASK)
Ideal para anotaciones rápidas, descripciones y asignación de etiquetas de color.

![Creación de tarjeta tipo TASK](./docs/Images/Crear_TASK.png)

### Tarea por Pasos (CHECKLIST)
Diseñada para tareas complejas. Te permite añadir múltiples ítems o subtareas que deben ser revisados uno por uno.

![Creación de tarjeta tipo CHECKLIST](./docs/Images/Crear_CHECKLIST.png)

---

## 🔍 6. Organización: Filtros y Etiquetas

A medida que tu tablero crezca, necesitarás localizar tareas rápidamente.

* **Filtros de Color/Texto:** Utiliza los menús desplegables de la barra superior para mostrar únicamente las tarjetas que contengan una etiqueta específica (ej. "URGENTE" de color rojo).
* **Botón Limpiar:** Cuando termines de buscar, pulsa el botón rojo **Limpiar** para quitar los filtros y volver a ver todas las tareas del tablero.

![Uso de filtros por color y etiqueta](./docs/Images/Filtro.png)

* **Bloquear Tablero:** Si necesitas pausar temporalmente el trabajo del equipo, utiliza el botón de bloqueo. Esto impedirá la creación de nuevas tareas, permitiendo solo mover las existentes.

![Aspecto del tablero y botón bloquear](./docs/Images/Boton_bloquear.png)

---

## ⚡ 7. Funciones Avanzadas

### 📜 Historial de Acciones
Pulsa el botón **Historial** para ver un registro detallado y cronológico de todo lo que ocurre en el tablero: quién crea listas, las tareas que se añaden y todos los movimientos entre columnas.

![Visualización del historial del tablero](./docs/Images/Historial.png)

### 🤖 Automatizaciones
Configura reglas inteligentes para que la aplicación haga el trabajo repetitivo por ti. 
1. Pulsa en **Automatizaciones**.
2. Crea una regla (Ejemplo: *Si una tarjeta se mueve a la lista "PLAYA" -> Añadir etiqueta ROJA*).

![Creación de una regla de automatización](./docs/Images/Automatizacion.png)
![Demostración visual de la regla aplicada automáticamente](./docs/Images/Demostracion_automatizacion.png)

### 🧹 Compactación Automática (Limpieza Invisible)
Para mantener tus espacios organizados, el sistema cuenta con una rutina de mantenimiento en segundo plano. Automáticamente escaneará el tablero y archivará aquellas tareas que lleven más de 7 días inactivas y que no hayan sido completadas, registrando la limpieza en el Historial.
