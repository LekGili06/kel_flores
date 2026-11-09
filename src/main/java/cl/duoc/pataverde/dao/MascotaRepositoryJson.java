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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MascotaRepositoryJson implements MascotaRepository {

    private static class ContenedorMascotas {
        List<Perro> perros = new ArrayList<>();
        List<Gato> gatos = new ArrayList<>();
    }

    private final Path archivoMascotas;
    private final Gson gson;

    private final List<Perro> perros = new ArrayList<>();
    private final List<Gato> gatos = new ArrayList<>();

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
        if (!Files.exists(archivoMascotas)) {
            return listar();
        }
        try (Reader lector = Files.newBufferedReader(archivoMascotas, StandardCharsets.UTF_8)) {
            ContenedorMascotas contenedor = gson.fromJson(lector, ContenedorMascotas.class);
            if (contenedor != null) {
                if (contenedor.perros != null) perros.addAll(contenedor.perros);
                if (contenedor.gatos != null) gatos.addAll(contenedor.gatos);
            }
        } catch (IOException | JsonParseException e) {
            throw new PersistenciaException("Error al leer el archivo " + archivoMascotas.getFileName() + ".", e);
        }
        return listar();
    }

    @Override
    public void agregar(Mascota mascota) throws PersistenciaException {
        if (mascota instanceof Perro perro) {
            perros.add(perro);
        } else if (mascota instanceof Gato gato) {
            gatos.add(gato);
        }
        guardarTodo();
    }

    @Override
    public void eliminar(Mascota mascota) throws PersistenciaException {
        if (mascota instanceof Perro perro) {
            perros.remove(perro);
        } else if (mascota instanceof Gato gato) {
            gatos.remove(gato);
        }
        guardarTodo();
    }

    @Override
    public List<Mascota> listar() {
        List<Mascota> todas = new ArrayList<>(perros);
        todas.addAll(gatos);
        return todas;
    }

    private void guardarTodo() throws PersistenciaException {
        ContenedorMascotas contenedor = new ContenedorMascotas();
        contenedor.perros = perros;
        contenedor.gatos = gatos;
        try (Writer escritor = Files.newBufferedWriter(archivoMascotas, StandardCharsets.UTF_8)) {
            gson.toJson(contenedor, escritor);
        } catch (IOException | JsonParseException e) {
            throw new PersistenciaException("Error al guardar el archivo " + archivoMascotas.getFileName() + ".", e);
        }
    }
}
