package pe.edu.velmore.model;

import jakarta.persistence.*;

/**
 * Entidad JPA que mapea la tabla "productos" en MySQL.
 * Conserva todos los constructores originales para compatibilidad
 * con InMemoryProductoDao y los tests unitarios.
 */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String marca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Column(nullable = false)
    private double precio;

    @Column(length = 300)
    private String descripcion;

    @Column(length = 100)
    private String imagen;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean activo;

    public Producto() {
    }

    public Producto(Long id, String nombre, String marca, Categoria categoria,
                    double precio, String descripcion, int stock, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagen = "nuevo";
        this.stock = stock;
        this.activo = activo;
    }

    public Producto(Long id, String nombre, String marca, Categoria categoria,
                    double precio, String descripcion, String imagen, int stock, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.stock = stock;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
