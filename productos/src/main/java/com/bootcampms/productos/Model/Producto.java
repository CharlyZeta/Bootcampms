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

    @Size(min = 8, max = 13, message = "El código de barras debe tener entre 8 y 13 caracteres") // Ajustado para EAN8/EAN13/UPC
    @NotBlank(message = "El código de barras no puede estar vacío")
    @Column(nullable = false, unique = true) // Generalmente los códigos de barras son únicos
    private String codBar; // Código de barras, código universal de producto, pensado para utilizar EAN13

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock = 0; // Stock actual, gestionado por el servicio de inventario

    @ManyToOne(fetch = FetchType.EAGER) // o LAZY si se prefiere
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull(message = "La categoría no puede ser nula")
    private Categoria categoria;

    @NotBlank(message = "La URL de la imagen no puede estar vacía") // Eliminar si la URL es opcional
    @Column(nullable = false) // Permitir que la URL sea nula si es opcional
    private String imagenUrl; // URL de la imagen

    @NotNull(message = "El estado no puede estar vacío")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

}