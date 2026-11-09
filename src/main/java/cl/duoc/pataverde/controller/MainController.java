package cl.duoc.pataverde.controller;

import cl.duoc.pataverde.dao.MascotaRepository;
import cl.duoc.pataverde.dao.MascotaRepositoryJson;
import cl.duoc.pataverde.dao.PersistenciaException;
import cl.duoc.pataverde.model.Gato;
import cl.duoc.pataverde.model.Mascota;
import cl.duoc.pataverde.model.Perro;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.util.Locale;

public class MainController {
    private static final String PERRO = "Perro";
    private static final String GATO = "Gato";

    private static final Locale LOCALE_CL = Locale.of("es", "CL");
    private static final NumberFormat FORMATO_ENTERO = NumberFormat.getIntegerInstance(LOCALE_CL);
    private static final NumberFormat FORMATO_PESO = NumberFormat.getNumberInstance(LOCALE_CL);

    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEdad;
    @FXML private TextField txtPeso;
    @FXML private TextField txtRaza;
    @FXML private CheckBox chkVacunado;
    @FXML private CheckBox chkAdopcion;
    @FXML private CheckBox chkExterior;

    @FXML private TableView<Mascota> tablaMascotas;
    @FXML private TableColumn<Mascota, String> colTipo;
    @FXML private TableColumn<Mascota, String> colNombre;
    @FXML private TableColumn<Mascota, Integer> colEdad;
    @FXML private TableColumn<Mascota, String> colPeso;
    @FXML private TableColumn<Mascota, String> colCosto;

    @FXML private Label lblMensaje;

    private final MascotaRepository repositorio = new MascotaRepositoryJson();
    private final ObservableList<Mascota> mascotas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        FORMATO_PESO.setMinimumFractionDigits(1);
        FORMATO_PESO.setMaximumFractionDigits(2);

        cbTipo.setItems(FXCollections.observableArrayList(PERRO, GATO));
        cbTipo.valueProperty().addListener((obs, anterior, nuevo) -> actualizarCamposPorTipo());
        actualizarCamposPorTipo();

        colTipo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTipo()));
        colNombre.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNombre()));
        colEdad.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEdad()));
        colPeso.setCellValueFactory(c -> new ReadOnlyStringWrapper(FORMATO_PESO.format(c.getValue().getPeso())));
        colCosto.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                "$" + FORMATO_ENTERO.format(c.getValue().calcularCostoConsulta())));
        tablaMascotas.setItems(mascotas);
        tablaMascotas.setPlaceholder(new Label("No hay mascotas registradas."));

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            mascotas.setAll(repositorio.cargar());
            mostrarInfo("Datos cargados: " + mascotas.size() + " mascota(s) registrada(s).");
        } catch (PersistenciaException e) {
            mascotas.setAll(repositorio.listar());
            mostrarError(e.getMessage());
        }
    }

    private void actualizarCamposPorTipo() {
        String tipo = cbTipo.getValue();
        boolean esPerro = PERRO.equals(tipo);
        boolean esGato = GATO.equals(tipo);

        txtRaza.setDisable(!esPerro);
        chkVacunado.setDisable(!esPerro);
        chkAdopcion.setDisable(!esPerro);
        chkExterior.setDisable(!esGato);
    }

    @FXML
    private void onRegistrar() {
        String tipo = cbTipo.getValue();
        if (tipo == null) {
            mostrarError("Tipo: debe seleccionar Perro o Gato.");
            return;
        }

        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("Nombre: no puede estar vacío ni contener solo espacios.");
            txtNombre.requestFocus();
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(txtEdad.getText().trim());
        } catch (NumberFormatException e) {
            mostrarError("Edad: debe ser un número entero entre 0 y 30.");
            txtEdad.requestFocus();
            return;
        }
        if (edad < 0 || edad > 30) {
            mostrarError("Edad: debe estar entre 0 y 30 años.");
            txtEdad.requestFocus();
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(txtPeso.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            mostrarError("Peso: debe ser un número mayor que cero (acepta decimales).");
            txtPeso.requestFocus();
            return;
        }
        if (Double.isNaN(peso) || Double.isInfinite(peso) || peso <= 0) {
            mostrarError("Peso: debe ser mayor que cero.");
            txtPeso.requestFocus();
            return;
        }

        Mascota mascota;
        try {
            if (PERRO.equals(tipo)) {
                String raza = txtRaza.getText() == null ? "" : txtRaza.getText().trim();
                if (raza.isEmpty()) {
                    mostrarError("Raza: no puede estar vacía ni contener solo espacios.");
                    txtRaza.requestFocus();
                    return;
                }
                Perro perro = new Perro(nombre, edad, peso, raza, chkVacunado.isSelected());
                if (chkAdopcion.isSelected()) {
                    perro.marcarDisponibleParaAdopcion();
                }
                mascota = perro;
            } else {
                mascota = new Gato(nombre, edad, peso, chkExterior.isSelected());
            }
        } catch (IllegalArgumentException e) {
            mostrarError("Dato no válido: " + e.getMessage());
            return;
        }

        try {
            repositorio.agregar(mascota);
        } catch (PersistenciaException e) {
            mostrarError(e.getMessage() + " La mascota no fue registrada.");
            return;
        }

        mascotas.add(mascota);
        mostrarExito(mascota.getNombre() + " (" + mascota.getTipo() + ") registrado correctamente.");
        limpiarFormulario();
    }

    @FXML
    private void onEliminar() {
        Mascota seleccionada = tablaMascotas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Debe seleccionar una mascota en la tabla para eliminarla.");
            return;
        }

        try {
            repositorio.eliminar(seleccionada);
        } catch (PersistenciaException e) {
            mostrarError(e.getMessage() + " La mascota no fue eliminada.");
            return;
        }

        mascotas.remove(seleccionada);
        tablaMascotas.getSelectionModel().clearSelection();
        mostrarExito(seleccionada.getNombre() + " (" + seleccionada.getTipo() + ") eliminado correctamente.");
    }

    private void limpiarFormulario() {
        cbTipo.getSelectionModel().clearSelection();
        cbTipo.setValue(null);
        txtNombre.clear();
        txtEdad.clear();
        txtPeso.clear();
        txtRaza.clear();
        chkVacunado.setSelected(false);
        chkAdopcion.setSelected(false);
        chkExterior.setSelected(false);
        actualizarCamposPorTipo();
    }

    private void mostrarExito(String texto) {
        mostrar(texto, "#1b5e20", "#e8f5e9");
    }

    private void mostrarError(String texto) {
        mostrar(texto, "#b71c1c", "#ffebee");
    }

    private void mostrarInfo(String texto) {
        mostrar(texto, "#0d47a1", "#e3f2fd");
    }

    private void mostrar(String texto, String colorTexto, String colorFondo) {
        lblMensaje.setText(texto);
        lblMensaje.setStyle("-fx-text-fill: " + colorTexto + "; -fx-background-color: " + colorFondo
                + "; -fx-padding: 8; -fx-background-radius: 4;");
    }
}
