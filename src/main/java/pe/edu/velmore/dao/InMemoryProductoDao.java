package pe.edu.velmore.dao;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import pe.edu.velmore.model.Categoria;
import pe.edu.velmore.model.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProductoDao implements ProductoDao {
    private static final Logger log = LoggerFactory.getLogger(InMemoryProductoDao.class);
    private final List<Producto> productos = Lists.newArrayList();
    private final AtomicLong secuencia = new AtomicLong(8);

    public InMemoryProductoDao() {
        productos.add(new Producto(1L, "Khamrah", "Lattafa", Categoria.UNISEX, 169, "Fragancia dulce, cálida y especiada.", "khamrah", 12, true));
        productos.add(new Producto(2L, "Khamrah Qahwa", "Lattafa", Categoria.UNISEX, 175, "Versión intensa con toque de café.", "qahwa", 8, true));
        productos.add(new Producto(3L, "9PM", "Afnan", Categoria.MASCULINO, 159, "Aroma nocturno, juvenil e intenso.", "9pm", 10, true));
        productos.add(new Producto(4L, "Sublime", "Lattafa", Categoria.FEMENINO, 149, "Fragancia frutal y floral.", "sublime", 9, true));
        productos.add(new Producto(5L, "Club de Nuit Intense", "Armaf", Categoria.MASCULINO, 175, "Fragancia cítrica y amaderada.", "club", 6, true));
        productos.add(new Producto(6L, "Yara Candy", "Lattafa", Categoria.FEMENINO, 155, "Aroma dulce, moderno y juvenil.", "yara", 7, true));
        productos.add(new Producto(7L, "Amber Oud Gold", "Al Haramain", Categoria.UNISEX, 210, "Fragancia frutal, dulce y potente.", "amber", 5, true));
        productos.add(new Producto(8L, "Asad Bourbon", "Lattafa", Categoria.MASCULINO, 165, "Aroma especiado y cálido.", "asad", 8, true));
        log.info("Productos iniciales cargados: {}", productos.size());
    }

    @Override
    public List<Producto> findAll() {
        return new ArrayList<>(productos);
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return productos.stream().filter(producto -> producto.getId().equals(id)).findFirst();
    }

    @Override
    public Producto save(Producto producto) {
        if (producto.getId() == null || producto.getId() == 0) {
            producto.setId(secuencia.incrementAndGet());
            productos.add(producto);
            log.info("Producto registrado: {}", producto.getNombre());
            return producto;
        }

        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId().equals(producto.getId())) {
                productos.set(i, producto);
                log.info("Producto actualizado: {}", producto.getNombre());
                return producto;
            }
        }

        productos.add(producto);
        return producto;
    }

    @Override
    public void deleteById(Long id) {
        productos.removeIf(producto -> producto.getId().equals(id));
        log.warn("Producto eliminado con id: {}", id);
    }
}
