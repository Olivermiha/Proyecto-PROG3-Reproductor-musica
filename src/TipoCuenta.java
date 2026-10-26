public enum TipoCuenta {
    FREE("Free"),
    PREMIUM("Premium");
 
    private final String nombre;
 
    TipoCuenta(String nombre) {
        this.nombre = nombre;
    }
 
    public String getNombre() {
        return nombre;
    }
}