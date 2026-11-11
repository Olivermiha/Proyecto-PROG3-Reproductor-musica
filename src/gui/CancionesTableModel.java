package gui;

import domain.Cancion;

import java.util.List;

import javax.swing.table.AbstractTableModel;

//IAG (herramienta: Claude)
//ADAPTADO: estructura tomada de SolicitudesTableModel (solución del examen parcial 2026-27)

/** Modelo de datos de la tabla de canciones. */
public final class CancionesTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private List<Cancion> canciones;
    private static final String[] CABECERAS = { "Título", "Artista", "Duración" };

    public CancionesTableModel(List<Cancion> canciones) {
        this.canciones = canciones;
    }

    /** Cambia las canciones que se muestran (por ejemplo, al filtrar) y refresca la tabla. */
    public void setCanciones(List<Cancion> canciones) {
        this.canciones = canciones;
        fireTableDataChanged();
    }

    public Cancion getCancion(int fila) {
        return canciones.get(fila);
    }

    @Override
    public int getRowCount() {
        return canciones.size();
    }

    @Override
    public int getColumnCount() {
        return CABECERAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return CABECERAS[columna];
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Cancion cancion = canciones.get(fila);

        switch (columna) {
            case 0: return cancion.getTitulo();
            case 1: return cancion.getArtista();
            case 2: return cancion.getDuracionFormateada();
            default: return null;
        }
    }
}
