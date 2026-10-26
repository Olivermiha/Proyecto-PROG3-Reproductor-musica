//IAG (herramienta: Claude)
//SIN CAMBIOS
public class Usuario {
    private String nombre;
    private String email;
    private TipoCuenta tipoCuenta;

    public Usuario(String nombre, String email, TipoCuenta tipoCuenta) {
        this.nombre = nombre;
        this.email = email;
        this.tipoCuenta = tipoCuenta;
    }

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public TipoCuenta getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(TipoCuenta tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public boolean esPremium() {
        return tipoCuenta == TipoCuenta.PREMIUM;
    }
}