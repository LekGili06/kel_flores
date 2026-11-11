package cl.duoc.pataverde.dao;

import cl.duoc.pataverde.model.Gato;
import cl.duoc.pataverde.model.Mascota;
import cl.duoc.pataverde.model.Perro;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio que guarda las mascotas en un archivo JSON usando Gson.
 * Los perros y gatos se guardan en listas separadas para poder
 * reconstruir el tipo correcto de cada objeto al leer el archivo.
 */
public class MascotaRepositoryJson implements MascotaRepository {

    private static class ContenedorMascotas {
        List<Perro> perros = new ArrayList<>();
        List<Gato> gatos = new ArrayList<>();
    }

    private final Path archivoMascotas;
    private final Gson gson;

    private final List<Perro> perros = new ArrayList<>();
    private final List<Gato> gatos = new ArrayList<>();

    /**
     * Queda en true si el archivo existe pero no se pudo leer o tiene datos inválidos.
     * Mientras esté en true no se guarda nada, para no sobrescribir el archivo
     * original y perder los datos que contiene.
     */
    private boolean cargaFallida = false;

    public MascotaRepositoryJson() {
        this(Paths.get("mascotas.json"));
    }

    public MascotaRepositoryJson(Path archivoMascotas) {
        this.archivoMascotas = archivoMascotas;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public List<Mascota> cargar() throws PersistenciaException {
        perros.clear();
        gatos.clear();
        cargaFallida = false;

        if (!Files.exists(archivoMascotas)) {
            return listar();
        }

        List<Perro> perrosLeidos = new ArrayList<>();
        List<Gato> gatosLeidos = new ArrayList<>();
        try (Reader lector = Files.newBufferedReader(archivoMascotas, StandardCharsets.UTF_8)) {
            ContenedorMascotas contenedor = gson.fromJson(lector, ContenedorMascotas.class);
            if (contenedor != null) {
                if (contenedor.perros != null) perrosLeidos.addAll(contenedor.perros);
                if (contenedor.gatos != null) gatosLeidos.addAll(contenedor.gatos);
            }
        } catch (IOException | JsonParseException e) {
            cargaFallida = true;
            throw new PersistenciaException("Error al leer el archivo " + archivoMascotas.getFileName()
                    + ". No se guardarán cambios para no sobrescribirlo.", e);
        }

        // Gson crea los objetos sin pasar por los métodos set, por lo que
        // las reglas del modelo se validan aquí, después de leer.
        try {
            for (int i = 0; i < perrosLeidos.size(); i++) {
                validar(perrosLeidos.get(i), "perro", i + 1);
            }
            for (int i = 0; i < gatosLeidos.size(); i++) {
                validar(gatosLeidos.get(i), "gato", i + 1);
            }
        } catch (PersistenciaException e) {
            cargaFallida = true;
            throw e;
        }

        perros.addAll(perrosLeidos);
        gatos.addAll(gatosLeidos);
        return listar();
    }

    private void validar(Mascota mascota, String tipo, int posicion) throws PersistenciaException {
        if (mascota == null) {
            throw new PersistenciaException("El archivo " + archivoMascotas.getFileName()
                    + " tiene un " + tipo + " vacío (posición " + posicion + ").");
        }
        try {
            // Se reutilizan los set del modelo, que ya rechazan los datos inválidos.
            mascota.setNombre(mascota.getNombre());
            mascota.setEdad(mascota.getEdad());
            mascota.setPeso(mascota.getPeso());
            if (mascota instanceof Perro perro
                    && (perro.getRaza() == null || perro.getRaza().trim().isEmpty())) {
                throw new IllegalArgumentException("La raza no puede ser nula ni vacía.");
            }
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Dato inválido en " + archivoMascotas.getFileName()
                    + " (" + tipo + " " + posicion + "): " + e.getMessage()
                    + " No se guardarán cambios para no sobrescribirlo.", e);
        }
    }

    @Override
    public void agregar(Mascota mascota) throws PersistenciaException {
        verificarQueSePuedeGuardar();
        if (mascota instanceof Perro perro) {
            perros.add(perro);
            try {
                guardarTodo();
            } catch (PersistenciaException e) {
                perros.remove(perro); // se deshace para que memoria y archivo coincidan
                throw e;
            }
        } else if (mascota instanceof Gato gato) {
            gatos.add(gato);
            try {
                guardarTodo();
            } catch (PersistenciaException e) {
                gatos.remove(gato);
                throw e;
            }
        }
    }

    @Override
    public void eliminar(Mascota mascota) throws PersistenciaException {
        verificarQueSePuedeGuardar();
        if (mascota instanceof Perro perro) {
            int posicion = perros.indexOf(perro);
            if (posicion < 0) return;
            perros.remove(posicion);
            try {
                guardarTodo();
            } catch (PersistenciaException e) {
                perros.add(posicion, perro);
                throw e;
            }
        } else if (mascota instanceof Gato gato) {
            int posicion = gatos.indexOf(gato);
            if (posicion < 0) return;
            gatos.remove(posicion);
            try {
                guardarTodo();
            } catch (PersistenciaException e) {
                gatos.add(posicion, gato);
                throw e;
            }
        }
    }

    @Override
    public List<Mascota> listar() {
        List<Mascota> todas = new ArrayList<>(perros);
        todas.addAll(gatos);
        return todas;
    }

    private void verificarQueSePuedeGuardar() throws PersistenciaException {
        if (cargaFallida) {
            throw new PersistenciaException("No se pueden guardar cambios porque el archivo "
                    + archivoMascotas.getFileName() + " no se pudo leer. Revíselo y reinicie la aplicación.");
        }
    }

    /**
     * Escribe primero en un archivo temporal y luego lo reemplaza por el original.
     * Así, si algo falla a mitad de la escritura, el archivo original queda intacto.
     */
    private void guardarTodo() throws PersistenciaException {
        ContenedorMascotas contenedor = new ContenedorMascotas();
        contenedor.perros = perros;
        contenedor.gatos = gatos;

        Path temporal = archivoMascotas.resolveSibling(archivoMascotas.getFileName() + ".tmp");
        try {
            try (Writer escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                gson.toJson(contenedor, escritor);
            }
            try {
                Files.move(temporal, archivoMascotas,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, archivoMascotas, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | JsonParseException e) {
            try {
                Files.deleteIfExists(temporal);
            } catch (IOException ignorada) {
                // si no se puede borrar el temporal no afecta a los datos
            }
            throw new PersistenciaException("Error al guardar el archivo " + archivoMascotas.getFileName() + ".", e);
        }
    }
}
