package com.bootcampms.productos.DTO;

import com.bootcampms.productos.Model.Categoria;
import com.bootcampms.productos.Model.Estado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la actualización de un producto.
 * Contiene solo los campos que se permite modificar a través del endpoint de actualización.
 */
@Data
@NoArgsConstructor
@Schema(description = "DTO para actualizar los detalles de un producto existente.")
public class ProductoUpdateRequestDTO {

    @NotBlank(message = "El SKU no puede estar vacío")
    @Schema(description = "Nuevo Stock Keeping Unit (SKU) para el producto.", example = "SKU001-MOD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sku;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Schema(description = "Nuevo nombre del producto.", example = "Laptop Pro X", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Nueva descripción detallada del producto.", example = "Laptop ultra delgada y potente.")
    private String descripcion; // La descripción puede ser opcional o vacía

    @Size(min = 8, max = 13, message = "El código de barras debe tener entre 8 y 13 caracteres")
    @NotBlank(message = "El código de barras no puede estar vacío")
    @Schema(description = "Nuevo código de barras universal del producto.", example = "9876543210987", requiredMode = Schema.RequiredMode.REQUIRED)
    private String codBar;

    @NotNull(message = "La categoría no puede ser nula")
    @Schema(description = "Nueva categoría a la que pertenece el producto. Se debe enviar el objeto Categoría completo o solo el ID si el backend lo soporta de esa forma.", requiredMode = Schema.RequiredMode.REQUIRED)
    private Categoria categoria; // O podrías enviar solo Long categoriaId si prefieres

    @NotBlank(message = "La URL de la imagen no puede estar vacía")
    @Schema(description = "Nueva URL de la imagen principal del producto.", example = "http://example.com/new_laptop.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imagenUrl;

    @NotNull(message = "El estado no puede estar vacío")
    @Schema(description = "Nuevo estado del producto en el sistema (BORRADOR, PUBLICADO, PRIVADO).", example = "PUBLICADO", requiredMode = Schema.RequiredMode.REQUIRED)
    private Estado estado;

    // NOTA: No incluimos id, precio, precioOferta, stock ya que no se actualizan por este DTO.
}