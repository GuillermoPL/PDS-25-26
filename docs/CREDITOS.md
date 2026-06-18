# Créditos y Participación

Este documento detalla la contribución de cada miembro del equipo al proyecto.

## Miembros del equipo
- **Jorge Torralba Santa Cruz**
- **Juan Paredes Pardines**
- **Guillermo Fulgencio Parra López**

---

## Detalle de contribuciones

### Jorge Torralba Santa Cruz
### 🛠️ Tareas Realizadas

**🎯 Modelado del Dominio y Servicios**
* Diseño e implementación de la capa de dominio siguiendo DDD (entidades principales, Value Objects y reglas de negocio).
* Desarrollo del servicio de dominio para el movimiento de tarjetas y los servicios de aplicación encargados de coordinar los casos de uso principales.

**🏗️ Persistencia y Testing**
* Creación de las entidades JPA para mapear la información a la base de datos y definición de los puertos de salida.
* Implementación de pruebas automatizadas (unitarias y de integración) para el Dominio, los Servicios de Aplicación, los Endpoints y las validaciones de la Arquitectura Hexagonal.
* Desarrollo de los tests restantes de los Servicios de Aplicación, incluyendo las pruebas de seguridad y lógica de negocio para la edición de tableros.

**✨ Funcionalidades de la App y Extra Opcional**
* Desarrollo visual y lógico de las tarjetas tipo `CHECKLIST` y la funcionalidad de la lista de tareas "Completadas" (con soporte en el historial).
* Implementación de mejoras de navegación en la interfaz (botón para volver al Dashboard).
* Desarrollo del servicio de **Compactación automática de tableros**.
* Implementación de la funcionalidad para editar el título de los tableros con validación de permisos.
* Modificación de la lógica visual y de comandos para permitir añadir límites de tarjetas en la creación de listas.

**📚 Documentación**
* Redacción completa del Manual de Usuario (incluyendo capturas de pantalla), Historias de Usuario y el Documento de Decisiones de Diseño Técnico.

---

### 📂 Evidencias de Trabajo

**📌 Ejemplos de Commits en Rama Principal**

* **Dominio y Servicios:** *Añado la entidad TaskList* / *Implemento la clase Board* / *Implemento el servicio de movimiento de tarjetas* (Junio 4 - 8)
* **Persistencia:** *Implemento las clases Entity para la persistencia JPA* (Junio 9)
* **Testing:** *Añado tests para el dominio* / *Implemento las pruebas de la arquitectura del proyecto* (Junio 9 - 13)
* **UI y Opcionales:** *Corrijo la UI para añadir pasos en CHECKLIST* / *Añado lista completadas* / *Añado servicio de Compactación* (Junio 10 - 12)
* **Nuevas Funcionalidades y Tests:** *Añado funcionalidad para editar título de un tablero* / *Implemento la funcionalidad para que se pueda añadir un límite de tarjetas al crear una lista de tareas* / *Añado dos funciones al test del servicio de aplicacion del tablero* (Junio 18)
* **Documentación:** *Add manual de usuario e imágenes* / *Update historias-de-usuario.md* (Junio 18)

**🔀 Pull Requests (PRs) Integrados**

| Bloque Funcional | Pull Requests | Descripción Resumida |
| :--- | :--- | :--- |
| **Dominio y Core** | PR #2 a #10, #14, #15, #17 | Entidades, Value Objects y servicio de dominio principal. |
| **Arquitectura y JPA** | PR #18, #22 | Entidades de persistencia y configuración de dependencias. |
| **Testing Automatizado** | PR #26 a #31, #83, #88 | Pruebas de Dominio, Aplicación, Endpoints y Arquitectura. |
| **Interfaz y Opcionales** | PR #53, #54, #56, #63, #64, #65 | UI de Checklists, lista de completadas, historiales y servicio de Compactación automática. |
| **Mejoras UI y Lógica** | PR #91, #92, #93 | Inserción de límites WIP, edición de títulos de tablero y tests unitarios asociados. |
| **Documentación y Refactor** | PR #89, #90 | Reestructuración de paquetes, Manual de Usuario y Documentación técnica. |

### Juan Paredes Pardines
## 🛠️ Tareas Realizadas

### 🏗️ Modelado del Dominio y Puertos de Salida
* **Descripción:** Definición y diseño de los contratos del dominio (puertos de salida), estableciendo las interfaces fundamentales de persistencia `BoardRepository` y `CardRepository` dentro del módulo *domain*, garantizando el desacoplamiento total respecto a los frameworks de infraestructura.

### ⚙️ Lógica de Aplicación y Casos de Uso Iniciales
* **Descripción:** Desarrollo e implementación de la primera fase de los servicios de aplicación centrales (`BoardServiceImpl` y `CardServiceImpl`). Orquestación de los flujos de negocio fundamentales para la creación y consulta de tableros, columnas y gestión inicial de tarjetas previa a la adición de módulos de seguridad avanzados.

### 🌐 Diseño de APIs y Controladores REST
* **Descripción:** Co-diseño, primeras implementaciones y análisis estructural de los endpoints HTTP orientados a la API REST de tableros (`BoardEndpoint`) y tarjetas (`CardEndpoint`), asegurando la correcta exposición de recursos y la comunicación limpia con los puertos de entrada.

### 📄 Documentación Técnica del Sistema
* **Descripción:** Redacción y estructuración exhaustiva de la documentación técnica interna de la aplicación, incluyendo la generación de comentarios enriquecidos bajo el estándar Doxygen en múltiples capas del sistema (agregados, servicios y adaptadores) para asegurar la mantenibilidad del software.

---

## 📂 Evidencias de Trabajo

### 🔀 Pull Requests (PRs) Integrados

| Bloque Funcional | Pull Requests | Descripción / Cobertura |
| :--- | :--- | :--- |
| **Dominio y Puertos** | `PR #11`, `PR #12` | Definición de las interfaces de los puertos de entrada (`BoardService`, `CardService`) y puertos de salida (`BoardRepository`, `CardRepository`) en el modelo de dominio. |
| **Servicios de Aplicación** | `PR #13`, `PR #20` | Desarrollo e implementación de la primera fase de los servicios de aplicación centrales (`CardServiceImpl` y `BoardServiceImpl`) para la orquestación de casos de uso. |
| **Diseño REST e Interfaces** | `PR #21`, `PR #32` | Primeras versiones y estructuración de los endpoints controladores REST (`BoardEndpoint` y `CardEndpoint`) y acoplamiento inicial de las vistas. |
| **Documentación Doxygen** | `PR #84`, `PR #85`, `PR #86`, `PR #87` | Puesta a punto y generación exhaustiva de la documentación en comentarios técnicos de las capas de dominio, aplicación e infraestructura bajo el estándar Doxygen. |

### Guillermo Fulgencio Parra López

## 🛠️ Tareas Realizadas

### 🏗️ Configuración y Arquitectura Base
* **Descripción:** Creación de la estructura inicial del proyecto utilizando arquitectura hexagonal y un sistema multimodular con Maven. Refactorización constante de la arquitectura, reorganización de imports y corrección de nombres de paquetes.

### 🗄️ Infraestructura y Persistencia
* **Descripción:** Implementación de repositorios mediante Spring Data (`SpringDataBoardRepository`, `SpringDataCardRepository`) y persistencia en disco. Corrección de errores críticos de base de datos como problemas de `lazyJPA` en la creación de tableros y ajustes en la cascada (`CascadeType`) de la entidad `TaskListEntity`.

### 🔄 Mapeo de Datos
* **Descripción:** Desarrollo de los mappers (`toEntity` y `toModel`) para la conversión de datos entre las capas de dominio e infraestructura de `Board`, `Card` y `TaskList`.

### 🎨 Interfaz de Usuario (UI) y Vistas
* **Descripción:** Implementación de la Vista V2, Historial V1 y vistas de etiquetas. Solución de bugs visuales (errores CSS, visualización de tarjetas "blanco sobre blanco").

### 🏷️ Gestión de Tarjetas y Etiquetas
* **Descripción:** Desarrollo de la funcionalidad para crear tarjetas con etiquetas y filtrado avanzado por color, incluyendo mejoras estéticas. Resolución de bugs que impedían crear o mover tarjetas en estado de bloqueo.

### 🔒 Autenticación, Seguridad y Sesiones
* **Descripción:** Implementación del servicio de Autenticación (Auth), envío y depuración de correos, y mantenimiento de la sesión del usuario (persistencia del correo al navegar por tableros sin necesidad de reiniciar). Creación y depuración del sistema de Permisos (V1).

### 🤖 Automatización y Plantillas
* **Descripción:** Implementación de la versión 1 de automatización y adición del sistema de plantillas.

### 🧪 Testing
* **Descripción:** Corrección y mantenimiento de pruebas automatizadas, específicamente para `BoardEndpoint`, `CardEndpoint` y `CardServiceImpl`.

### 📄 Documentación y Gestión de Repositorio
* **Descripción:** Actualización del archivo `README.md` (versiones de Java, características, enlaces a documentación, créditos del equipo).

---

## 📂 Evidencias de Trabajo

### 📌 Commits en Rama Principal
* **Commits del 29 de Marzo:**
  * `chore: estructura inicial Hexagonal multimodulo Maven`
  * `Readme`
* **Commits de Documentación (10 de Junio - 13 de Junio):**
  * `Add team credits and contribution details`
  * `Update documentation links in README.md`
  * `Revise README for Java version and feature details`

### 🔀 Pull Requests (PRs) Integrados

| Bloque Funcional | Pull Requests | Descripción / Cobertura |
| :--- | :--- | :--- |
| **Infraestructura y Datos** | `PR #23`, `PR #24`, `PR #25`, `PR #62` | Modelado de mappers, implementación de `SpringDataCardRepository`, `SpringDataBoardRepository` y persistencia en disco. |
| **UI y Capa Visual** | `PR #33`, `PR #35`, `PR #38` | Implementación de VISTA v2, HISTORIAL v1, configuración de `SceneManager` y corrección de errores CSS / contraste de tarjetas. |
| **Etiquetas y Filtros** | `PR #40` a `PR #44`, `PR #51`, `PR #52` | Preparación de vistas de etiquetas, asignación a tarjetas y lógica de filtrado avanzado por paleta de colores. |
| **Seguridad y Sesión** | `PR #66`, `PR #67`, `PR #68`, `PR #69`, `PR #71`, `PR #72` | Control de ciclo de vida de sesión (MailAuth), resolución de bugs de permisos (V1) y depuración del servicio de correo. |
| **Automatización** | `PR #61`, `PR #70` | Despliegue del motor de Automatización V1 y optimizaciones en el sistema de plantillas del sistema. |
| **Mantenimiento y QA** | `PR #73` a `PR #78`, `PR #79` a `PR #82` | Refactorizaciones de arquitectura, limpieza de consola, reestructuración de paquetes, optimización de imports y corrección de tests de endpoints/servicios. |

Pull Requests de Testing y Arquitectura: PR #73 a #78 (Fixes de arquitectura, limpieza de consola y paquetes), PR #79 a #82 (Arreglo de tests e imports).

Commits de Documentación (Jun 10 - Jun 13): "Add team credits and contribution details", "Update documentation links in README.md", "Revise README for Java version and feature details".
