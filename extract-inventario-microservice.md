# Guía para extracción del microservicio de inventario

Este documento detalla los pasos necesarios para extraer el microservicio de inventario desde el proyecto actual y moverlo a su propio proyecto Maven independiente.

## 1. Crear nuevo proyecto Spring Boot

Primero, crea un nuevo proyecto Spring Boot usando Spring Initializr con las siguientes dependencias:
- Spring Web
- Spring Data JPA
- H2 Database
- Spring Boot DevTools
- Spring Boot Actuator
- Lombok
- Validation

## 2. Estructura de directorios

Crea la siguiente estructura de directorios en el nuevo proyecto:

```
src/main/java/com/bootcampms/inventario/
├── Config
├── Controller
├── DTO
├── Exception
├── Model
├── Repository
├── Service
└── InventarioApplication.java
```

## 3. Archivos a trasladar

Copia los siguientes archivos desde el proyecto actual al nuevo proyecto:

### Modelos
- `MovimientoInventario.java`
- `TipoMovimiento.java`
- `StockProducto.java`

### Repositorios
- `MovimientoInventarioRepository.java`
- `StockProductoRepository.java`

### Servicios
- `InventarioService.java`
- `InventarioServiceImpl.java`
- `ProductoValidationService.java`

### Controladores
- `InventarioController.java`

### DTOs
- `MovimientoInventarioDTO.java`

### Excepciones
- `StockInsuficienteException.java`
- `ProductoNoEncontradoException.java`
- `TipoMovimientoIncorrectoException.java`
- `GlobalExceptionHandler.java`

### Configuración
- `RestTemplateConfig.java`
- `DataInitializer.java`

### Propiedades
- Copia `application-inventario.properties` como `application.properties` en el nuevo proyecto

## 4. Crear clase principal de aplicación

Crea un archivo `InventarioApplication.java` con el siguiente contenido:

```java
package com.bootcampms.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioApplication.class, args);
    }
}
```

## 5. Actualizar el pom.xml

Asegúrate de que el pom.xml del nuevo proyecto tiene las dependencias correctas. Debería ser similar a:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.bootcampms</groupId>
    <artifactId>inventario</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>inventario</name>
    <description>Microservicio de Inventario para BootcampMS</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 6. Limpiar el proyecto original

Una vez que el nuevo microservicio esté funcionando correctamente, puedes eliminar los paquetes de inventario del proyecto original para evitar duplicaciones y posibles conflictos.

## 7. Ejecutar ambos servicios

Asegúrate de iniciar ambos servicios:
1. El servicio de productos en el puerto 8080
2. El nuevo servicio de inventario en el puerto 8081

## 8. Probar la comunicación

Verifica que la comunicación entre los microservicios funciona correctamente ejecutando operaciones que requieran validación de productos desde el microservicio de inventario. 