package pe.edu.velmore.dto;

public class CarritoItemDto {
    private Long productoId;
    private int cantidad;

    public CarritoItemDto() {
    }

    public CarritoItemDto(Long productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
