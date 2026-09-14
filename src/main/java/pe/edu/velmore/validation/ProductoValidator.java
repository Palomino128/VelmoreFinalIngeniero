package pe.edu.velmore.validation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pe.edu.velmore.model.Producto;

/**
 * Validador de productos.
 * Usa Google Guava (Preconditions, Strings) y Apache Commons Lang (StringUtils)
 * para validaciones seguras y legibles.
 */
@Component
public class ProductoValidator {

    public void validar(Producto producto) {
        Preconditions.checkNotNull(producto, "El producto no puede ser nulo");

        if (Strings.isNullOrEmpty(StringUtils.trimToNull(producto.getNombre()))) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (Strings.isNullOrEmpty(StringUtils.trimToNull(producto.getMarca()))) {
            throw new IllegalArgumentException("La marca es obligatoria");
        }
        if (producto.getCategoria() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        if (producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (!Strings.isNullOrEmpty(producto.getDescripcion())
                && producto.getDescripcion().length() > 300) {
            throw new IllegalArgumentException("La descripción no puede superar 300 caracteres");
        }
    }

    public String limpiarTexto(String texto) {
        return StringUtils.trimToEmpty(texto);
    }
}
