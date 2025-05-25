package com.bootcampms.productos.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Representa un producto en el catálogo.
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa un producto en el sistema.")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del producto generado automáticamente.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El SKU no puede estar vacío")
    @Column(unique = true, nullable = false)
    @Schema(description = "Stock Keeping Unit (SKU) único para el producto.", example = "SKU001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sku;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false)
    @Schema(description = "Nombre del producto.", example = "Laptop Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Descripción detallada del producto.", example = "Laptop de alto rendimiento con 16GB RAM.")
    private String descripcion;

    @NotNull(message = "El precio no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    @Digits(integer=10, fraction=2, message = "Formato de precio inválido (máx 10 enteros, 2 decimales)")
    @Column(nullable = false)
    @Schema(description = "Precio regular del producto.", example = "1200.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal precio;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de oferta no puede ser negativo")
    @Digits(integer=10, fraction=2, message = "Formato de precio de oferta inválido (máx 10 enteros, 2 decimales)")
    @Schema(description = "Precio de oferta del producto (opcional).", example = "1100.50")
    private BigDecimal precioOferta; // Puede ser nulo

    @Size(min = 8, max = 13, message = "El código de barras debe tener entre 8 y 13 caracteres")
    @NotBlank(message = "El código de barras no puede estar vacío")
    @Column(nullable = false, unique = true)
    @Schema(description = "Código de barras universal del producto (EAN, UPC).", example = "1234567890123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String codBar;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    @Schema(description = "Cantidad de stock actual del producto. Generalmente gestionado por un servicio de inventario.", example = "10", defaultValue = "0")
    private Integer stock = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull(message = "La categoría no puede ser nula")
    @Schema(description = "Categoría a la que pertenece el producto.", requiredMode = Schema.RequiredMode.REQUIRED)
    private Categoria categoria;

    @NotBlank(message = "La URL de la imagen no puede estar vacía")
    @Column(nullable = false)
    @Schema(description = "URL de la imagen principal del producto.", example = "http://example.com/laptop.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imagenUrl;

    @NotNull(message = "El estado no puede estar vacío")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Estado actual del producto en el sistema (BORRADOR, PUBLICADO, PRIVADO).", example = "PUBLICADO", requiredMode = Schema.RequiredMode.REQUIRED)
    private Estado estado;
}