package main;

import domain.Cancion;
import domain.TipoCuenta;
import domain.Usuario;
import gui.VentanaPrincipal;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Swing debe arrancarse en el hilo de eventos
        SwingUtilities.invokeLater(() -> {
            Usuario usuario = new Usuario("Roberto", "roberto.c@mail.com", TipoCuenta.FREE);
            VentanaPrincipal ventana = new VentanaPrincipal(usuario, crearCancionesDemo());
            ventana.setVisible(true);
        });
    }

    /** Datos de prueba hasta que se conecte la base de datos. */
    private static List<Cancion> crearCancionesDemo() {
        List<Cancion> lista = new ArrayList<>();
        lista.add(new Cancion("La casa por el tejado", "Fito y  Fitipaldis", 244));
        lista.add(new Cancion("So payaso", "Extremoduro", 283));
        lista.add(new Cancion("La Cancion", "J Balvin", 251));
        lista.add(new Cancion("La vereda de la puerta de atrás ", "Extremoduro", 244));
        lista.add(new Cancion("Columbia", "Quevedo", 206));
        return lista;
    }
}
