package pe.edu.velmore.model;

public class Usuario {
    private String usuario;
    private String clave;
    private String nombre;
    private String rol;

    public Usuario() {
    }

    public Usuario(String usuario, String clave, String nombre, String rol) {
        this.usuario = usuario;
        this.clave = clave;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean esAdministrador() {
        return "ADMIN".equalsIgnoreCase(rol);
    }

    public boolean esCliente() {
        return "CLIENTE".equalsIgnoreCase(rol);
    }
}
