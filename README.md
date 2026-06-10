# 📋 PDS 2025/26: Tableros Colaborativos (Clon de Trello)

![Java](https://img.shields.io/badge/Java-orange)
![Build](https://img.shields.io/badge/Build-Maven-blue)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal_DDD-green)
![Backend](https://img.shields.io/badge/Backend-Spring_Boot-lightgrey)

Proyecto práctico para la construcción de un sistema de gestión de trabajo colaborativo basado en tableros Kanban, inspirado fuertemente en Trello. Desarrollado bajo los principios de **Arquitectura Hexagonal** y **Domain-Driven Design (DDD)**.

Asignatura: **Procesos de desarrollo de software 2025/26**.

---

## 👥 Equipo de Desarrollo

| Nombre | Email | Subgrupo |
| :--- | :--- | :--- |
| **Jorge Torralba Santa Cruz** | `jorge.torralbas@um.es` | 1.3 |
| **Juan Paredes Pardines** | `juan.paredesp@um.es` | 1.3 |
| **Guillermo Fulgencio Parra López** | `gf.parralopez@um.es` | 1.3 |

---

## 📝 Descripción del Sistema

Aplicación de gestión de proyectos mediante tableros y tareas. El sistema permite organizar el trabajo mediante tarjetas que fluyen a través de listas personalizables, garantizando la persistencia de datos y un diseño orientado al dominio.

### Características Básicas
* **Gestión de Tableros:** Creación mediante correo electrónico obteniendo una URL única y pública.
* **Organización:** Creación de listas de tareas dentro de los tableros.
* **Tarjetas:** Soporte para tareas simples y *checklists*.
* **Clasificación:** Uso de etiquetas de diferentes colores para identificar las tarjetas.
* **Trazabilidad:** Registro histórico de los movimientos de las tarjetas entre listas.
* **Control de Flujo:** Bloqueo temporal de tableros (permite mover tarjetas existentes, pero no crear nuevas).

### Características Opcionales Implementadas
* **Reglas a nivel de lista (Fácil):** Restricción del límite de N ítems por lista.
* **Reglas de flujo (Fácil):** Definición de rutas obligatorias para las tarjetas entre listas.
* **Plantillas (Fácil):** Creación de tableros preconfigurados a través de ficheros YAML.
* **Filtrado (Fácil):** Búsqueda y filtrado visual de tarjetas por sus etiquetas.

---

## 🏗 Arquitectura y Stack Tecnológico

El proyecto abandona el patrón MVC tradicional en favor de una **Arquitectura Hexagonal (Puertos y Adaptadores)** centrada en el Dominio.

* **Lenguaje:** Java.
* **Gestor de dependencias:** Maven.
* **Capa de Dominio:** Aislada al 100%, sin dependencias de frameworks. Implementa Entidades, *Value Objects* y Servicios de Dominio siguiendo DDD.
* **Backend (Infraestructura):** Spring Boot.
* **Frontend (Interfaces):** Interfaz gráfica de escritorio desarrollada con JavaFX.
* **Persistencia:** JPA.
* **Calidad:** Alta cobertura de pruebas de software, prestando especial atención a la validación del modelo de dominio.

---

## 📂 Estructura del Proyecto (Maven Multi-module)

El repositorio refleja la separación física y estricta de las capas arquitectónicas para garantizar la Inversión de Dependencias:

* `tableros-backend-parent/`: Proyecto orquestador principal (POM).
* `domain/`: Núcleo puro de la aplicación. Contiene reglas de negocio y puertos (interfaces). Cero dependencias de Spring.
* `application/`: Casos de uso que orquestan el flujo entre los puertos y el dominio.
* `infrastructure/`: Implementación técnica (Adaptadores). Contiene los controladores REST y los repositorios de Spring Data JPA.

---

## 📚 Documentación Obligatoria y Enlaces de Interés

* [**Historias de Usuario**](./docs/historias-de-usuario.md): Definición de los requisitos funcionales del sistema.
* [**Créditos y participación**](./docs/CREDITOS.md): Detalle de la contribución de cada miembro.
