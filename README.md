# 📋 PDS 2025/26: Tableros Colaborativos (Clon de Trello)

![Java](https://img.shields.io/badge/Java-21-orange)
![Build](https://img.shields.io/badge/Build-Maven-blue)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal_DDD-green)
![Backend](https://img.shields.io/badge/Backend-Spring_Boot_3.2-lightgrey)
![UI](https://img.shields.io/badge/UI-JavaFX-purple)

Proyecto práctico para la construcción de un sistema de gestión de trabajo colaborativo basado en tableros Kanban, inspirado fuertemente en Trello. Desarrollado bajo los más estrictos principios de **Arquitectura Hexagonal (Puertos y Adaptadores)** y **Domain-Driven Design (DDD)**.

Asignatura: **Procesos de desarrollo de software 2025/26**.

---

## 👥 Equipo de Desarrollo

| Nombre | Email | Subgrupo |
| :--- | :--- | :--- |
| **Jorge Torralba Santa Cruz** | `jorge.torralbas@um.es` | 1.3 |
| **Juan Paredes Pardines** | `juan.paredesp@um.es` | 1.3 |
| **Guillermo Fulgencio Parra López** | `gf.parralopez@um.es` | 1.3 |

---

## 📝 Descripción del Sistema y Características

Aplicación de gestión de proyectos mediante tableros y tareas. El sistema permite organizar el trabajo mediante tarjetas que fluyen a través de listas personalizables, garantizando la persistencia de datos y un diseño orientado al dominio puro.

### 🔹 Características Básicas (Core)
* **Gestión de Tableros:** Creación de tableros asociados a un correo electrónico (identidad principal del sistema).
* **Listas y Flujo:** Creación de listas de tareas (columnas) dentro de los tableros.
* **Tarjetas enriquecidas:** Soporte para tarjetas de tipo *Tarea* simple y *Checklist* (ítems de verificación).
* **Clasificación y Completadas:** Uso de etiquetas (nombre y color) para identificar tarjetas y posibilidad de enviarlas automáticamente a una lista designada de "Completadas".
* **Trazabilidad:** Registro en el historial del tablero de todos los eventos y movimientos de las tarjetas.
* **Control de Tablero:** Bloqueo temporal de tableros por parte del dueño (impide la creación de nuevas tarjetas o listas, permitiendo solo el movimiento de las existentes).

### ⭐ Características Opcionales Implementadas (7/7)
Se ha superado el requisito de 4 características opcionales para la nota máxima, implementando la totalidad de las propuestas:

1. **Reglas a nivel de lista:** Restricción de capacidad máxima de tarjetas (`maxCards`) por columna.
2. **Filtrado visual:** Herramienta en la interfaz gráfica para filtrar instantáneamente tarjetas por nombre de etiqueta o color.
3. **Plantillas YAML:** Sistema de creación de tableros base preconfigurados cargados dinámicamente desde ficheros `.yaml`.
4. **Compactación automática:** Tarea programada en segundo plano (`@Scheduled`) para el mantenimiento y limpieza automatizada de los tableros.
5. **Autenticación Passwordless:** Sistema de acceso seguro mediante el envío de un código temporal (válido durante 5 minutos) por correo electrónico, protegido mediante Interceptores REST.
6. **Gestión de Permisos:** El propietario del tablero puede invitar a otros usuarios mediante su email, otorgándoles roles granulares de lectura (`READ`) o escritura (`WRITE`), así como revocar el acceso.
7. **Reglas de Automatización:** Motor de reglas personalizadas por el usuario ("Si ocurre evento X -> hacer Y"). Ejemplo: Si una tarjeta se mueve a la lista "Done", se marca automáticamente como completada.

---

## 🏗 Arquitectura y Decisiones de Diseño (DDD)

El proyecto abandona el patrón MVC tradicional en favor de una **Arquitectura Hexagonal**. Todo el núcleo de la aplicación es agnóstico a la tecnología, lo que garantiza su mantenibilidad y testeabilidad a largo plazo.

* **El Dominio como centro:** Las entidades no son simples contenedores de datos (anémicos), sino objetos ricos que protegen sus invariantes.
    * *Agregados (Aggregate Roots):* `Board` (gestiona listas, historial, permisos y automatizaciones) y `Card` (gestiona sus checklists y etiquetas).
    * *Value Objects:* Identificadores inmutables como `BoardId`, `CardId`, `ListId` y `Email`.
* **Aislamiento Técnico:** Los repositorios y servicios externos se comunican con el dominio exclusivamente a través de **Puertos** (Interfaces). La base de datos (H2 + JPA) reside en la capa de Infraestructura como un **Adaptador de Salida**.
* **Protección de la Interfaz (UI):** La interfaz de escritorio (JavaFX) actúa como un **Adaptador de Entrada**. Interactúa con la lógica de negocio consumiendo Endpoints REST y comunicándose a través de un `SceneManager` inyectado, utilizando únicamente DTOs (*Data Transfer Objects*) y *Command Objects* inmutables para operaciones de escritura.

### 🧪 Pruebas de Software y Calidad
* **Tests Unitarios:** Verificación del comportamiento del dominio y casos de uso aislados mediante `Mockito` y `JUnit 5`.
* **Tests de Integración:** Validación completa de los adaptadores REST, los repositorios JPA y los flujos de seguridad (Interceptores) usando `MockMvc` con bases de datos en memoria.
* **Testing Arquitectónico:** Uso intensivo de **ArchUnit** para garantizar matemáticamente, mediante integración continua, que ninguna clase de infraestructura invade el dominio (Inestabilidad del Dominio = 0.00).

---

## 📂 Estructura del Proyecto

El repositorio refleja la separación lógica de las capas arquitectónicas para garantizar la Inversión de Dependencias:

* `domain/`: Núcleo puro de la aplicación. Contiene el modelo rico (DDD), los puertos (interfaces) y carece totalmente de dependencias de frameworks.
* `application/`: Casos de uso (Servicios de Aplicación) que orquestan el flujo entre los puertos y los comandos de entrada.
* `infrastructure/`: Implementación técnica (Adaptadores). Contiene:
    * `persistence/`: Repositorios JPA, *Mappers* y Entidades de base de datos.
    * `rest/`: Endpoints de la API y DTOs.
    * `javafx/`: Controladores MVC visuales y ficheros FXML.
    * `security/`: Interceptores y gestión de sesiones.

---

## 📚 Documentación Obligatoria y Enlaces de Interés

* [**Créditos y Participación**](./docs/CREDITOS.md): Detalle de la contribución de cada miembro del equipo y referencias a *Commits*/*Pull Requests*.
* [**Historias de Usuario**](./docs/historias-de-usuario.md): Definición de los requisitos funcionales iniciales del sistema.
* [**Documentación de Diseño**](./docs/DOCUMENTACION.md): Documento ampliado con diagramas y decisiones arquitectónicas detalladas.
* [**Manual de Usuario**](./docs/manual-de-usuario.md): Manual de usuario para explicar el funcionamiento de la aplicación.
