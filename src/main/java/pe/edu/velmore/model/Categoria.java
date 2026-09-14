package pe.edu.velmore.model;

public enum Categoria {
    MASCULINO("Masculino"),
    FEMENINO("Femenino"),
    UNISEX("Unisex");

    private final String etiqueta;

    Categoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
