package com.bootcampms.productos.Config;

import com.bootcampms.productos.Model.Categoria;
import com.bootcampms.productos.Model.Estado;
import com.bootcampms.productos.Model.Producto;
import com.bootcampms.productos.Repository.CategoriaRepository;
import com.bootcampms.productos.Repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Componente para inicializar datos de prueba en la base de datos al arrancar la aplicación.
 * Se ejecuta solo si no existen datos previos en las tablas de categorías y productos.
 * Activo principalmente en perfiles de desarrollo o prueba.
 */
@Component
@Profile("dev") // Opcional: Activar solo para el perfil 'dev' o perfiles específicos
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    /**
     * Constructor para inyección de dependencias.
     * @param productoRepository Repositorio de productos.
     * @param categoriaRepository Repositorio de categorías.
     */
    @Autowired
    public DataInitializer(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Método que se ejecuta al iniciar la aplicación.
     * Carga datos iniciales si la base de datos está vacía.
     * @param args Argumentos de línea de comandos (no utilizados).
     * @throws Exception Si ocurre algún error durante la carga.
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("Verificando necesidad de carga de datos iniciales...");

        if (categoriaRepository.count() == 0 && productoRepository.count() == 0) {
            log.info("Base de datos vacía. Iniciando carga de datos iniciales...");

            // Crear Categorías
            Categoria electronica = categoriaRepository.save(new Categoria(null, "Electrónica", "Dispositivos y accesorios electrónicos"));
            Categoria ropa = categoriaRepository.save(new Categoria(null, "Ropa", "Prendas de vestir"));
            Categoria hogar = categoriaRepository.save(new Categoria(null, "Hogar", "Artículos para el hogar"));
            log.info("Categorías creadas: {}", categoriaRepository.count());

            // Crear Productos
            Producto p1 = new Producto(null, "SKU001", "Laptop Pro", "Laptop de alto rendimiento", new BigDecimal("1200.99"), null, "1234567890123", 10, electronica, "http://example.com/laptop.jpg", Estado.PUBLICADO);
            Producto p2 = new Producto(null, "SKU002", "Teclado Gamer", "Teclado mecánico RGB", new BigDecimal("75.50"), new BigDecimal("69.99"), "2345678901234", 20, electronica, "http://example.com/keyboard.jpg", Estado.PUBLICADO);
            Producto p3 = new Producto(null, "SKU003", "Camiseta Básica", "Camiseta de algodón", new BigDecimal("19.99"), null, "3456789012345", 50, ropa, "http://example.com/tshirt.jpg", Estado.PUBLICADO);
            Producto p4 = new Producto(null, "SKU004", "Taza Café", "Taza de cerámica", new BigDecimal("9.95"), null, "4567890123456", 30, hogar, "http://example.com/mug.jpg", Estado.BORRADOR);

            productoRepository.saveAll(Arrays.asList(p1, p2, p3, p4));
            log.info("Productos creados: {}", productoRepository.count());

            log.info("Carga de datos iniciales completada.");
        } else {
            log.info("La base de datos ya contiene datos. No se realizó la carga inicial.");
        }
    }
}