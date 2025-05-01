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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component // Para que Spring la detecte y gestione
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando carga de datos iniciales...");

        // Solo cargar si no hay datos (opcional, para evitar duplicados en reinicios)
        if (categoriaRepository.count() == 0 && productoRepository.count() == 0) {

            // Crear Categorías
            Categoria electronica = categoriaRepository.save(new Categoria(null, "Electrónica", "Dispositivos y accesorios electrónicos"));
            Categoria ropa = categoriaRepository.save(new Categoria(null, "Ropa", "Prendas de vestir"));
            Categoria hogar = categoriaRepository.save(new Categoria(null, "Hogar", "Artículos para el hogar"));

            log.info("Categorías creadas: {}", categoriaRepository.findAll());

            // Crear Productos
            Producto p1 = new Producto(null, "SKU001", "Laptop Pro", "Laptop de alto rendimiento", new BigDecimal("1200.99"), null, "1234567890123", 0, electronica, "http://example.com/laptop.jpg", Estado.PUBLICADO);
            Producto p2 = new Producto(null, "SKU002", "Teclado Gamer", "Teclado mecánico RGB", new BigDecimal("75.50"), new BigDecimal("69.99"), "2345678901234", 0, electronica, "http://example.com/keyboard.jpg", Estado.PUBLICADO);
            Producto p3 = new Producto(null, "SKU003", "Camiseta Básica", "Camiseta de algodón", new BigDecimal("19.99"), null, "3456789012345", 0, ropa, "http://example.com/tshirt.jpg", Estado.PUBLICADO);
            Producto p4 = new Producto(null, "SKU004", "Taza Café", "Taza de cerámica", new BigDecimal("9.95"), null, "4567890123456", 0, hogar, "http://example.com/mug.jpg", Estado.BORRADOR); // Producto en borrador

            productoRepository.saveAll(Arrays.asList(p1, p2, p3, p4));

            log.info("Productos creados: {}", productoRepository.findAll());

            log.info("Carga de datos iniciales completada.");
        } else {
            log.info("La base de datos ya contiene datos. No se realizó la carga inicial.");
        }
    }
}