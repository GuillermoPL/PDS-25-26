# 🏛️ Documentación de Decisiones de Diseño Técnico

Este documento detalla las soluciones técnicas y patrones de diseño aplicados para resolver los retos de negocio, integración, persistencia y seguridad del proyecto, respetando rigurosamente las restricciones de la Arquitectura Hexagonal y el Diseño Orientado al Dominio (DDD).

---

## 1. Modelado del Dominio (Domain-Driven Design)
El núcleo de la aplicación se ha construido ignorando por completo los frameworks externos (ni Spring, ni JPA, ni JavaFX), enfocándose exclusivamente en las reglas de negocio puras.

* **Agregados y Raíces (Aggregate Roots):** Hemos definido `Board` y `Card` como los dos Agregados principales. El tablero (`Board`) es el guardián de sus listas, su historial y sus reglas. Cualquier modificación (como añadir una lista o registrar un evento) pasa obligatoriamente por los métodos de la entidad `Board`, garantizando que sus reglas de negocio no se rompan.
* **Objetos de Valor (Value Objects):** Hemos utilizado la característica de `records` de Java para modelar identidades inmutables (`BoardId`, `CardId`, `ListId`) y conceptos como el `Email`. Esto erradica el problema de la "obsesión por los primitivos" (pasar *Strings* genéricos por toda la aplicación) y hace que el código sea predecible y seguro frente a errores de tipado.
* **Modelo Rico vs Anémico:** Nuestras entidades no son simples contenedores de datos con *getters* y *setters* públicos. Su estado interno está protegido. Las transiciones de estado se realizan a través de métodos de negocio con semántica clara (ej. `board.lock()`, `card.marcarCompletada()`), los cuales lanzan excepciones (`IllegalStateException`) si se intenta una acción no permitida.
* **Patrón de Comandos Inmutables:** La entrada de datos desde la capa de Aplicación hacia el Dominio se realiza empaquetando los parámetros en objetos de Comando inmutables (ej. `MoverCardCommand`). Esto asegura que las operaciones reciban siempre datos íntegros y válidos desde su creación.

---

## 2. Integración de la Interfaz Gráfica (Spring Boot y JavaFX)
Unir el motor visual (JavaFX) con el núcleo de la aplicación presentaba varios retos que resolvimos priorizando la limpieza del código y la experiencia de usuario:

* **Arranque Coordinado:** Configuramos el sistema para que la interfaz visual y el motor interno de la aplicación se inicien a la vez y de forma entrelazada. Así, las ventanas pueden comunicarse de forma natural con la lógica del programa.
* **Conexión Automática:** En lugar de que cada pantalla visual tenga que construir manualmente los servicios que necesita para funcionar, el sistema se los "inyecta" automáticamente al abrirse. Esto mantiene el código de las ventanas muy limpio y centrado solo en pintar la interfaz.
* **Gestión Limpia de Ventanas Emergentes:** Configuramos los cuadros de diálogo (como la ventana para crear una nueva tarjeta) para que nazcan como instancias totalmente nuevas desde cero cada vez que se abren. Esto evita "fugas de memoria" o que aparezcan textos y selecciones residuales de la vez anterior que la abriste.
* **Interfaz "Tonta" (Desacoplada):** La interfaz visual no toma decisiones de negocio. Cuando el usuario hace algo complejo, como arrastrar y soltar una tarjeta entre listas, la vista simplemente traduce ese gesto en una orden sencilla y se la envía al núcleo de la aplicación para que valide si el movimiento está permitido.
---

## 3. Persistencia y Mapeo Relacional (JPA/Hibernate)
Mapear el modelo de dominio a una base de datos relacional (H2) sin contaminar las entidades de negocio requirió estrategias muy específicas de aislamiento.

* **Triple Mapeo de Aislamiento:** Hemos construido clases `Mapper` (`BoardMapper`, `CardMapper`) que centralizan la traducción en la capa de Infraestructura. El flujo de datos es: `Entity (JPA) <-> Modelo (Dominio) <-> DTO (REST/UI)`. Esto evita que el dominio deba cumplir con requisitos técnicos de JPA (como constructores vacíos obligatorios).
* **Optimización con `@Embeddable`:** En lugar de crear tablas completas con claves primarias artificiales para conceptos dependientes, hemos mapeado los *Value Objects* del dominio usando colecciones embebidas (`AutomationRuleEmbeddable`, `EtiquetaEmbeddable`). Esto reduce drásticamente los JOINs de Hibernate y refleja que, si se borra una tarjeta, sus etiquetas deben desaparecer físicamente con ella.
* **Límites Transaccionales Estrictos:** La relación entre el `BoardEntity` y sus listas se ha configurado con eliminación huérfana (`orphanRemoval = true`). Toda persistencia pasa obligatoriamente por el repositorio del Tablero (`BoardRepository`), respetando su rol de *Aggregate Root*.

---

## 4. Seguridad y Gestión de Sesión Volátil
Para el sistema de autenticación *passwordless* (OTP por correo), implementamos un mecanismo de sesión seguro sin depender de bases de datos externas.

* **Almacenamiento Concurrente en RAM:** Las sesiones se almacenan en la memoria del servidor utilizando un `ConcurrentHashMap` dentro de `AuthSessionManager`, haciéndolo *thread-safe* (seguro para peticiones simultáneas) y extremadamente rápido.
* **Ventana Deslizante de Expiración:** Cada sesión guarda una marca temporal de expiración. Cada vez que el usuario realiza una acción válida, el sistema intercepta el token y renueva la validez de forma automática por otros 5 minutos.
* **Aduana de Seguridad Centralizada:** Implementamos un `AuthInterceptor` registrado en `WebConfig` que actúa como filtro previo para toda la ruta de la API (`/api/v1/**`). Esto libera a los controladores de la responsabilidad de validar tokens.

---

## 5. Automatización y Tareas en Segundo Plano
Hemos diseñado mecanismos para que el sistema trabaje de forma autónoma sin depender siempre de los clics del usuario, pero respetando siempre la arquitectura central:

* **Creación Segura desde Plantillas:** Cuando la aplicación lee un archivo de plantilla para generar un tablero, no "hace trampas" inyectando los datos directamente en la base de datos. En su lugar, el sistema reutiliza las mismas funciones que usaría una persona normal. Esto garantiza que un tablero generado automáticamente cumple con las mismas reglas, permisos y límites que uno creado a mano.
* **Mantenimiento Invisible (Compactación):** Para mantener los tableros limpios de tareas antiguas, configuramos un proceso autónomo guiado por el reloj del sistema operativo mediante la anotación `@Scheduled` de Spring. Este mecanismo actúa como un disparador que despierta periódicamente la tarea de limpieza en segundo plano, manteniendo el orden sin interrumpir ni ralentizar la experiencia del usuario.
