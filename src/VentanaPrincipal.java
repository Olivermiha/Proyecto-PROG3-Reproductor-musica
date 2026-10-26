import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;


/** Ventana principal: barra superior, menú lateral, contenido central y reproductor abajo. */
public class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final String PANEL_INICIO = "inicio";
    private static final String PANEL_CANCIONES = "canciones";
    private static final String PANEL_LISTAS = "listas";

    private Usuario usuario;
    private List<Cancion> canciones;

    private JPanel panelContenido;
    private CardLayout cardLayout;

    private JLabel lblCuenta;
    private JButton btnCambiarPlan;

    // Reproductor
    private JLabel lblCancionActual;
    private JButton btnPlayPausa;
    private JProgressBar barraProgreso;
    private Timer temporizador;       // Timer de Swing: simula el avance de la canción
    private Cancion cancionActual;
    private int segundosActuales;

    public VentanaPrincipal(Usuario usuario, List<Cancion> canciones) {
        this.usuario = usuario;
        this.canciones = canciones;

        setTitle("Simulador de música");
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearMenuLateral(), BorderLayout.WEST);
        add(crearContenidoCentral(), BorderLayout.CENTER);
        add(crearReproductor(), BorderLayout.SOUTH);

        crearTemporizador();
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JLabel lblTitulo = new JLabel("Simulador de música");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(18f));

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        lblCuenta = new JLabel();
        btnCambiarPlan = new JButton();
        btnCambiarPlan.addActionListener(e -> cambiarPlan());
        derecha.add(lblCuenta);
        derecha.add(btnCambiarPlan);

        barra.add(lblTitulo, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        actualizarTextoCuenta();
        return barra;
    }

    private JPanel crearMenuLateral() {
        JPanel menu = new JPanel(new GridLayout(0, 1, 5, 5));
        menu.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        menu.setPreferredSize(new Dimension(160, 0));

        JButton btnInicio = new JButton("Inicio");
        JButton btnCanciones = new JButton("Canciones");
        JButton btnListas = new JButton("Mis listas");

        btnInicio.addActionListener(e -> cardLayout.show(panelContenido, PANEL_INICIO));
        btnCanciones.addActionListener(e -> cardLayout.show(panelContenido, PANEL_CANCIONES));
        btnListas.addActionListener(e -> cardLayout.show(panelContenido, PANEL_LISTAS));

        menu.add(btnInicio);
        menu.add(btnCanciones);
        menu.add(btnListas);

        // Para que los botones no se estiren hasta abajo
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(menu, BorderLayout.NORTH);
        return contenedor;
    }

    private JPanel crearContenidoCentral() {
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);

        panelContenido.add(new PanelInicio(usuario, canciones, this::reproducir), PANEL_INICIO);
        panelContenido.add(crearPanelProvisional("Aquí irá la gestión de canciones"), PANEL_CANCIONES);
        panelContenido.add(crearPanelProvisional("Aquí irán tus listas de reproducción"), PANEL_LISTAS);

        return panelContenido;
    }

    private JPanel crearPanelProvisional(String texto) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(texto, SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearReproductor() {
        JPanel reproductor = new JPanel(new BorderLayout(10, 5));
        reproductor.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        lblCancionActual = new JLabel("Ninguna canción en reproducción");
        lblCancionActual.setPreferredSize(new Dimension(300, 25));

        btnPlayPausa = new JButton("▶");
        btnPlayPausa.addActionListener(e -> alternarPlayPausa());

        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("0:00");

        reproductor.add(lblCancionActual, BorderLayout.WEST);
        reproductor.add(barraProgreso, BorderLayout.CENTER);
        reproductor.add(btnPlayPausa, BorderLayout.EAST);
        return reproductor;
    }

    // ---------- Lógica del reproductor ----------

    private void crearTemporizador() {
        temporizador = new Timer(1000, e -> {
            segundosActuales++;
            barraProgreso.setValue(segundosActuales);
            barraProgreso.setString(String.format("%d:%02d", segundosActuales / 60, segundosActuales % 60));
            if (segundosActuales >= cancionActual.getDuracionSegundos()) {
                temporizador.stop();
                btnPlayPausa.setText("▶");
                segundosActuales = 0;
                barraProgreso.setValue(0);
            }
        });
    }

    private void reproducir(Cancion cancion) {
        // En el plan Free se avisa de que hay anuncios
        if (!usuario.esPremium()) {
            JOptionPane.showMessageDialog(this,
                    "Anuncio: ¡Hazte Premium para escuchar sin interrupciones!",
                    "Plan Free", JOptionPane.INFORMATION_MESSAGE);
        }
        temporizador.stop();
        cancionActual = cancion;
        segundosActuales = 0;
        barraProgreso.setMaximum(cancion.getDuracionSegundos());
        barraProgreso.setValue(0);
        barraProgreso.setString("0:00");
        lblCancionActual.setText(cancion.getTitulo() + " - " + cancion.getArtista());
        btnPlayPausa.setText("⏸");
        temporizador.start();
    }

    private void alternarPlayPausa() {
        if (cancionActual == null) {
            JOptionPane.showMessageDialog(this, "Selecciona primero una canción en la pantalla de inicio.",
                    "Sin canción", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (temporizador.isRunning()) {
            temporizador.stop();
            btnPlayPausa.setText("▶");
        } else {
            temporizador.start();
            btnPlayPausa.setText("⏸");
        }
    }

    // ---------- Gestión de la cuenta ----------

    private void cambiarPlan() {
        boolean haciaPremium = !usuario.esPremium();
        String mensaje = haciaPremium
                ? "¿Quieres cambiar al plan Premium?"
                : "¿Quieres volver al plan Free?";
        int respuesta = JOptionPane.showConfirmDialog(this, mensaje, "Cambiar plan",
                JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            usuario.setTipoCuenta(haciaPremium ? TipoCuenta.PREMIUM : TipoCuenta.FREE);
            actualizarTextoCuenta();
            // Reconstruimos la pantalla de inicio para que muestre el nuevo plan
            panelContenido.add(new PanelInicio(usuario, canciones, this::reproducir), PANEL_INICIO);
            cardLayout.show(panelContenido, PANEL_INICIO);
        }
    }

    private void actualizarTextoCuenta() {
        lblCuenta.setText(usuario.getNombre() + " | " + usuario.getTipoCuenta().getNombre());
        btnCambiarPlan.setText(usuario.esPremium() ? "Pasar a Free" : "Hazte Premium");
    }
}