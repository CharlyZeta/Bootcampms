package com.bootcampms.productos.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El SKU no puede estar vacío")
    @Column(unique = true, nullable = false)
    private String sku;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    @Digits(integer=10, fraction=2, message = "Formato de precio inválido (máx 10 enteros, 2 decimales)")
    @Column(nullable = false)
    private BigDecimal precio;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de oferta no puede ser negativo")
    @Digits(integer=10, fraction=2, message = "Formato de precio de oferta inválido (máx 10 enteros, 2 decimales)")
    private BigDecimal precioOferta; // Puede ser nulo

    private String codBar; // Código de barras

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock = 0; // Stock actual, gestionado por el servicio de inventario

    @Column(nullable = false)
    private Boolean visible = true; // Por defecto, visible

    @ManyToOne(fetch = FetchType.EAGER) // o LAZY si se prefiere
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull(message = "La categoría no puede ser nula")
    private Categoria categoria;

    private String imagenUrl; // URL de la imagen
}