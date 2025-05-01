package com.bootcampms.productos.DTO;

import com.bootcampms.productos.Model.Categoria;
import com.bootcampms.productos.Model.Estado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

// Usamos Lombok para reducir boilerplate, igual que en Producto
@Data
@NoArgsConstructor
public class ProductoUpdateRequestDTO {

    // Incluimos solo los campos que SÍ queremos permitir actualizar
    // y sus validaciones correspondientes para la actualización.

    @NotBlank(message = "El SKU no puede estar vacío")
    private String sku;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String descripcion; // La descripción puede ser opcional o vacía

    @Size(min = 8, max = 13, message = "El código de barras debe tener entre 8 y 13 caracteres")
    @NotBlank(message = "El código de barras no puede estar vacío")
    private String codBar;

    @NotNull(message = "La categoría no puede ser nula")
    private Categoria categoria; // O podrías enviar solo Long categoriaId si prefieres

    @NotBlank(message = "La URL de la imagen no puede estar vacía") // Mantener si es obligatoria al actualizar
    private String imagenUrl;

    @NotNull(message = "El estado no puede estar vacío")
    private Estado estado;

    // NOTA: No incluimos id, precio, precioOferta, stock
}