import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Swing debe arrancarse en el hilo de eventos
        SwingUtilities.invokeLater(() -> {
            Usuario usuario = new Usuario("Ana", "ana@mail.com", TipoCuenta.FREE);
            VentanaPrincipal ventana = new VentanaPrincipal(usuario, crearCancionesDemo());
            ventana.setVisible(true);
        });
    }

    /** Datos de prueba hasta que se conecte la base de datos. */
    private static List<Cancion> crearCancionesDemo() {
        List<Cancion> lista = new ArrayList<>();
        lista.add(new Cancion("Noche de verano", "Los Faroles", 15));
        lista.add(new Cancion("Camino a casa", "Marta Vega", 20));
        lista.add(new Cancion("Eco", "Neon Lights", 12));
        lista.add(new Cancion("Lluvia en Bilbao", "Aitor Sendas", 18));
        lista.add(new Cancion("Última parada", "Los Faroles", 25));
        return lista;
    }
}