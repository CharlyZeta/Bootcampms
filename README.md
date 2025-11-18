# BootCamp Microservicios - Código Facilito 2025

> Proyecto final del **BootCamp de Microservicios con Java** de Código Facilito. Sistema completo de microservicios demostrando arquitectura escalable, containerización con Docker y monitoreo con Prometheus.

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)
[![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-E6522C.svg)](https://prometheus.io/)

---

## 📄 Descripción

Sistema de **2 microservicios independientes** que demuestran:
- Arquitectura de microservicios con Spring Boot
- Comunicación entre servicios
- Containerización completa con Docker Compose
- Monitoreo de recursos con Prometheus
- Escalabilidad horizontal
- API REST bien documentada

---

## 🏋️ Arquitectura del Sistema

```
┌─────────────────────┐
│  Docker Compose Orquestación    │
├─────────────────────┤
│                                 │
│ ╯────────────╮         │
│ │ Servicio Productos  │         │
│ │ Puerto: 8001      │         │
│ └────────────┕         │
│                                 │
│ ╯────────────╮         │
│ │ Servicio Inventario │         │
│ │ Puerto: 8002      │         │
│ └────────────┕         │
│                                 │
│ ╯────────────╮         │
│ │ Prometheus Monitor  │         │
│ │ Puerto: 9090      │         │
│ └────────────┕         │
└─────────────────────┘
```

### Servicios

| Servicio | Puerto | Descripción |
|----------|--------|---------------|
| **Productos** | 8001 | API REST para gestión de productos |
| **Inventario** | 8002 | API REST para gestión de inventario |
| **Prometheus** | 9090 | Monitoreo y métricas del sistema |

---

## 🚀 Tecnologías Utilizadas

- **Backend**: Java 17 + Spring Boot 3.x
- **Framework**: Spring Boot Microservices
- **Containerización**: Docker + Docker Compose
- **Monitoreo**: Prometheus
- **Build Tool**: Maven
- **Documentación API**: Postman Collection incluida

---

## 📋 Requisitos Previos

- Docker Desktop instalado ([Descargar](https://www.docker.com/products/docker-desktop))
- Docker Compose (incluido en Docker Desktop)
- Java 17+ (opcional, solo si ejecutas localmente sin Docker)
- Git

---

## 🚀 Cómo Ejecutar

### Opción 1: Con Docker Compose (Recomendado)

```bash
# Clonar el repositorio
git clone https://github.com/CharlyZeta/Bootcampms.git
cd Bootcampms

# Iniciar todos los servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Detener servicios
docker-compose down
```

### URLs de Acceso

- **Servicio Productos**: http://localhost:8001/api/productos
- **Servicio Inventario**: http://localhost:8002/api/inventario
- **Prometheus Dashboard**: http://localhost:9090

---

## 📝 Documentación de APIs

Se incluye **Postman Collection** con todos los endpoints documentados:

```bash
# Importa en Postman:
- BootCamp MS CodigoFacilito.postman_collection.json
```

### Ejemplos de Requests

**Obtener Productos:**
```bash
curl -X GET http://localhost:8001/api/productos
```

**Crear Producto:**
```bash
curl -X POST http://localhost:8001/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Producto Test","precio":99.99}'
```

---

## 📊 Monitoreo con Prometheus

1. Acceder a: http://localhost:9090
2. Queries disponibles:
   - `up` - Estado de servicios
   - `process_uptime_seconds` - Tiempo de ejecución
   - `jvm_memory_used_bytes` - Uso de memoria JVM

---

## 🖥️ Estructura del Proyecto

```
Bootcampms/
├── productos/                    # Microservicio de Productos
│  ├── src/
│  └── pom.xml
├── inventario/                   # Microservicio de Inventario  
│  ├── src/
│  └── pom.xml
├── config/prometheus/            # Configuración de Prometheus
│  └── prometheus.yml
├── docker-compose.yml            # Orquestación de servicios
├── README.md                     # Este archivo
├── BootCamp MS.postman_collection.json
└── BootCamp MS.postman_test_run.json
```

---

## 👨‍💻 Autor

**Gerardo Maidana** (CharlyZeta)
- GitHub: [@CharlyZeta](https://github.com/CharlyZeta)
- LinkedIn: [gerardomaidana](https://www.linkedin.com/in/gerardomaidana/)
- Experiencia: 17+ años en TI/Sistemas, especializado en Backend Development

---

## 📘 Licencia

Este proyecto es de uso educativo. MIT License.

---

## 🙋 Contribuciones

Pullrequests bienvenidos. Para cambios mayores, abre un issue primero.

---

## 🗐️ Tips para Desarrolladores

- **Debugging**: Usa `docker-compose logs <service>` para ver logs
- **Rebuild**: `docker-compose up --build` para reconstruir imágenes
- **Acceso a contenedor**: `docker-compose exec <service> bash`
- **Limpieza**: `docker-compose down -v` para eliminar volúmenes

---

**✨ Ültima actualización**: Noviembre 2025
