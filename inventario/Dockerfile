# Etapa 1: Construcción - Usando una imagen de Maven con JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Establecer el directorio de trabajo para la compilación
WORKDIR /build

# Copiar solo el pom.xml para cachear las dependencias de Maven
# si no cambian entre compilaciones.
COPY pom.xml .
# Descargar todas las dependencias
RUN mvn dependency:go-offline -B

# Copiar el resto del código fuente del proyecto
COPY src ./src

# Compilar la aplicación y empaquetarla en un JAR.
# Se omiten los tests para acelerar la construcción de la imagen Docker.
# El JAR se generará en target/inventario-VERSION.jar (ej. target/inventario-0.0.1-SNAPSHOT.jar)
RUN mvn package -DskipTests

# Crear una copia del JAR generado con un nombre genérico ('app.jar') dentro de la etapa de construcción.
# Esto facilita la copia a la etapa de ejecución sin necesidad de conocer la versión exacta.
# Ajusta 'inventario-*.jar' si el artifactId de tu microservicio de inventario es diferente.
RUN cp target/inventario-*.jar target/app.jar

# Etapa 2: Ejecución - Usando una imagen JRE 17 ligera, que solo contiene lo necesario para ejecutar
FROM eclipse-temurin:17-jre-jammy

# Establecer el directorio de trabajo en el contenedor final
WORKDIR /app

# Copiar el JAR renombrado ('app.jar') desde la etapa de 'builder'
COPY --from=builder /build/target/app.jar app.jar

# Exponer el puerto en el que la aplicación Spring Boot se ejecuta
# Asegúrate de que este puerto coincida con el server.port de tu microservicio de inventario
# (por ejemplo, 8081 si es diferente al de productos)
EXPOSE 8081

# Comando para ejecutar la aplicación cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]