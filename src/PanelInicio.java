import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;


//IAG (herramienta: Claude)
//SIN CAMBIOS
/** Pantalla de inicio: saludo, plan de la cuenta y canciones recomendadas. */
public class PanelInicio extends JPanel {
    private static final long serialVersionUID = 1L;

    private JList<Cancion> listaRecomendadas;

    /**
     * @param usuario        usuario que ha iniciado sesión
     * @param recomendadas   canciones a mostrar
     * @param alSeleccionar  acción a ejecutar al hacer doble clic en una canción
     */
    public PanelInicio(Usuario usuario, List<Cancion> recomendadas, Consumer<Cancion> alSeleccionar) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Cabecera
        JPanel cabecera = new JPanel(new BorderLayout());
        JLabel lblBienvenida = new JLabel("¡Hola, " + usuario.getNombre() + "!");
        lblBienvenida.setFont(new Font("SansSerif", Font.BOLD, 26));
        JLabel lblPlan = new JLabel("Tu plan: " + usuario.getTipoCuenta().getNombre()
                + (usuario.esPremium() ? " (sin anuncios)" : " (con anuncios)"));
        cabecera.add(lblBienvenida, BorderLayout.NORTH);
        cabecera.add(lblPlan, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        // Lista de canciones recomendadas
        DefaultListModel<Cancion> modelo = new DefaultListModel<>();
        for (Cancion c : recomendadas) {
            modelo.addElement(c);
        }
        listaRecomendadas = new JList<>(modelo);
        listaRecomendadas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaRecomendadas.setFont(new Font("SansSerif", Font.PLAIN, 16));

        // Doble clic -> reproducir
        listaRecomendadas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Cancion seleccionada = listaRecomendadas.getSelectedValue();
                    if (seleccionada != null) {
                        alSeleccionar.accept(seleccionada);
                    }
                }
            }
        });

        JPanel centro = new JPanel(new BorderLayout(5, 5));
        centro.add(new JLabel("Recomendado para ti (doble clic para reproducir)"), BorderLayout.NORTH);
        centro.add(new JScrollPane(listaRecomendadas), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }
}