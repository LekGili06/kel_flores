package cl.duoc.pataverde.dao;

import cl.duoc.pataverde.model.Mascota;

import java.util.List;

public interface MascotaRepository {
    List<Mascota> cargar() throws PersistenciaException;

    void agregar(Mascota mascota) throws PersistenciaException;

    void eliminar(Mascota mascota) throws PersistenciaException;

    List<Mascota> listar();
}
