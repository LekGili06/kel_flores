package cl.duoc.pataverde.dao;

/**
 * Error al leer o guardar los datos de las mascotas.
 * Es una excepción verificada para obligar a quien la usa a manejarla.
 */
public class PersistenciaException extends Exception {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
