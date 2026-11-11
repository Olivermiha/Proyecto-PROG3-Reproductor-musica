package gui;

import domain.Cancion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

//IAG (herramienta: Claude)
//ADAPTADO: estructura de la tabla y del hilo tomada de la solución del examen parcial 2026-27
//(VentanaSolicitudesSolucion y VentanaPreparacionSolucion) y de las prácticas Swing 4 y 5

/**
 * Pantalla de canciones: tabla con todas las canciones, buscador por teclado
 * y un hilo que simula la carga de la biblioteca.
 */
public class PanelCanciones extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Color COLOR_PAR = new Color(250, 249, 249);
    private static final Color COLOR_IMPAR = new Color(235, 242, 250);

    private final List<Cancion> origen;                 // canciones que hay que cargar
    private final List<Cancion> cargadas = new ArrayList<>(); // canciones ya cargadas por el hilo
    private final Consumer<Cancion> alSeleccionar;

    private CancionesTableModel modelo;
    private JTable tabla;
    private JTextField txtBuscar = new JTextField(20);
    private JLabel lblEstado = new JLabel("Biblioteca sin cargar");
    private JProgressBar barraProgreso = new JProgressBar(0, 100);
    private JButton btnCargar = new JButton("Cargar canciones");
    private JButton btnPausar = new JButton("Pausar");

    private int filaRaton = -1;     // fila sobre la que está el ratón (-1 = ninguna)
    private HiloCarga hilo;

    /**
     * @param canciones     canciones de la biblioteca
     * @param alSeleccionar acción a ejecutar al reproducir una canción
     */
    public PanelCanciones(List<Cancion> canciones, Consumer<Cancion> alSeleccionar) {
        this.origen = canciones;
        this.alSeleccionar = alSeleccionar;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(crearCabecera(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        add(crearPanelCarga(), BorderLayout.SOUTH);

        configurarControles(false);
    }

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("Canciones");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));

        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buscador.add(new JLabel("Buscar: "));
        buscador.add(txtBuscar);

        // Evento de teclado: se filtra la tabla cada vez que se escribe en el buscador
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    txtBuscar.setText("");
                }
                filtrar();
            }
        });

        cabecera.add(lblTitulo, BorderLayout.WEST);
        cabecera.add(buscador, BorderLayout.EAST);
        return cabecera;
    }

    private JScrollPane crearTabla() {
        modelo = new CancionesTableModel(new ArrayList<>());
        tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setShowGrid(false);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(80);

        // Renderer de la cabecera: texto en negrita y misma alineación que los datos
        tabla.getTableHeader().setDefaultRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JLabel result = new JLabel(value.toString());
            result.setFont(result.getFont().deriveFont(Font.BOLD));
            result.setHorizontalAlignment(column == 2 ? JLabel.CENTER : JLabel.LEFT);
            result.setBackground(table.getTableHeader().getBackground());
            result.setForeground(table.getTableHeader().getForeground());
            result.setOpaque(true);
            return result;
        });

        // Renderer del contenido: filas alternas, fila del ratón resaltada y texto buscado marcado
        TableCellRenderer rendererContenido = (table, value, isSelected, hasFocus, row, column) -> {
            JLabel celda = new JLabel();
            String texto = value.toString();
            String filtro = txtBuscar.getText().trim();

            if (column != 2 && !filtro.isEmpty()) {
                celda.setText(resaltar(texto, filtro));
            } else {
                celda.setText(texto);
            }

            celda.setHorizontalAlignment(column == 2 ? JLabel.CENTER : JLabel.LEFT);
            celda.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

            if (isSelected || row == filaRaton) {
                celda.setBackground(table.getSelectionBackground());
                celda.setForeground(table.getSelectionForeground());
            } else {
                celda.setBackground(row % 2 == 0 ? COLOR_PAR : COLOR_IMPAR);
                celda.setForeground(table.getForeground());
            }
            celda.setOpaque(true);
            return celda;
        };

        // Asignamos el renderer al contenido de la tabla
        tabla.setDefaultRenderer(Object.class, rendererContenido);

        // Eventos de ratón: doble clic para reproducir y resaltar la fila bajo el ratón
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    reproducirSeleccionada();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                filaRaton = -1;
                tabla.repaint();
            }
        });

        tabla.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                filaRaton = tabla.rowAtPoint(e.getPoint());
                tabla.repaint();
            }
        });

        // Evento de teclado en la tabla: ENTER reproduce la canción seleccionada
        tabla.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    reproducirSeleccionada();
                }
            }
        });

        return new JScrollPane(tabla);
    }

    private JPanel crearPanelCarga() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        barraProgreso.setStringPainted(true);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        botones.add(btnCargar);
        botones.add(btnPausar);
        btnCargar.addActionListener(e -> iniciarCarga());
        btnPausar.addActionListener(e -> pausarReactivar());

        panel.add(lblEstado, BorderLayout.NORTH);
        panel.add(barraProgreso, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.EAST);
        return panel;
    }

    // ---------- Lógica de la tabla ----------

    private void reproducirSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero una canción de la tabla.",
                    "Sin canción", JOptionPane.WARNING_MESSAGE);
            return;
        }
        alSeleccionar.accept(modelo.getCancion(fila));
    }

    /** Muestra solo las canciones cargadas cuyo título o artista contienen el texto del buscador. */
    private void filtrar() {
        String filtro = txtBuscar.getText().trim().toLowerCase();
        List<Cancion> visibles = new ArrayList<>();

        for (Cancion c : cargadas) {
            if (filtro.isEmpty()
                    || c.getTitulo().toLowerCase().contains(filtro)
                    || c.getArtista().toLowerCase().contains(filtro)) {
                visibles.add(c);
            }
        }
        modelo.setCanciones(visibles);
    }

    /** Devuelve el texto en HTML con la parte que coincide con el filtro resaltada. */
    private String resaltar(String texto, String filtro) {
        int inicio = texto.toLowerCase().indexOf(filtro.toLowerCase());
        if (inicio == -1) {
            return texto;
        }
        int fin = inicio + filtro.length();
        return "<html>" + texto.substring(0, inicio)
                + "<strong style='color:yellow; background-color:blue'>" + texto.substring(inicio, fin) + "</strong>"
                + texto.substring(fin) + "</html>";
    }

    // ---------- Hilo de carga ----------

    private void iniciarCarga() {
        if (!cargadas.isEmpty()) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "La biblioteca ya está cargada. ¿Quieres volver a cargarla?",
                    "Recargar canciones", JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        cargadas.clear();
        filtrar();
        mostrarAvance(0);
        configurarControles(true);
        hilo = new HiloCarga(origen);
        hilo.start();
    }

    private void pausarReactivar() {
        if (btnPausar.getText().equals("Pausar")) {
            hilo.pausar();
            btnPausar.setText("Reactivar");
        } else {
            hilo.reactivar();
            btnPausar.setText("Pausar");
        }
    }

    /** Hilo que simula la carga (costosa) de las canciones, una a una. */
    private class HiloCarga extends Thread {
        private final List<Cancion> canciones;
        private boolean enPausa;

        public HiloCarga(List<Cancion> canciones) {
            this.canciones = canciones;
        }

        public synchronized void pausar() {
            enPausa = true;
        }

        public synchronized void reactivar() {
            enPausa = false;
            notifyAll();
        }

        @Override
        public void run() {
            try {
                int total = canciones.size();
                Random random = new Random();

                for (int i = 0; i < total; i++) {
                    // El monitor es este hilo. Solo se comprueba la pausa aquí.
                    synchronized (this) {
                        while (enPausa) {
                            wait();
                        }
                    }

                    final Cancion cancion = canciones.get(i);
                    SwingUtilities.invokeLater(() -> mostrarEstado("Cargando: " + cancion.getTitulo()));

                    // Simula el tiempo de leer la canción
                    Thread.sleep(300 + random.nextInt(700));

                    final int porcentaje = (i + 1) * 100 / total;
                    SwingUtilities.invokeLater(() -> {
                        cargadas.add(cancion);
                        filtrar();
                        mostrarAvance(porcentaje);
                    });
                }

                SwingUtilities.invokeLater(() -> mostrarEstado(total + " canciones cargadas"));
            } catch (InterruptedException e) {
                this.interrupt();
            } finally {
                SwingUtilities.invokeLater(() -> configurarControles(false));
            }
        }
    }

    protected void mostrarAvance(int porcentaje) {
        barraProgreso.setValue(porcentaje);
    }

    protected void mostrarEstado(String texto) {
        lblEstado.setText(texto);
    }

    private void configurarControles(boolean enEjecucion) {
        btnCargar.setEnabled(!enEjecucion);
        btnPausar.setEnabled(enEjecucion);
        btnPausar.setText("Pausar");
    }

    /** Detiene la carga si está en marcha (por ejemplo, al cerrar la ventana). */
    public void detenerCarga() {
        if (hilo != null && hilo.isAlive()) {
            hilo.interrupt();
        }
    }
}
