package org.example.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.exception.TrackerException;
import org.example.model.EstadoOferta;
import org.example.model.FiltroOferta;
import org.example.model.Oferta;
import org.example.service.OfertaService;
import org.example.ui.Servicios;
import org.example.ui.dialogo.OfertaDialogo;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controlador del listado de ofertas: muestra, filtra, ordena y borra ofertas.
 */
public class ListadoOfertasController implements Initializable {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<EstadoOferta> cmbEstado;
    @FXML private TableView<Oferta> tabla;
    @FXML private TableColumn<Oferta, String> colId;
    @FXML private TableColumn<Oferta, String> colTitulo;
    @FXML private TableColumn<Oferta, String> colEmpresa;
    @FXML private TableColumn<Oferta, String> colEstado;
    @FXML private TableColumn<Oferta, String> colFecha;
    @FXML private TableColumn<Oferta, String> colSalario;

    private final OfertaService servicio = Servicios.ofertas();
    private final OfertaDialogo dialogo = new OfertaDialogo(servicio, Servicios.empresas());
    private final ObservableList<Oferta> ofertas = FXCollections.observableArrayList();
    private final FilteredList<Oferta> filtradas = new FilteredList<>(ofertas, o -> true);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().titulo()));
        colEmpresa.setCellValueFactory(c ->
                new SimpleStringProperty(Objects.toString(c.getValue().nombreEmpresa(), "")));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estado().valorBd()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fechaPostulacion() == null
                ? "" : c.getValue().fechaPostulacion().format(FECHA)));
        colSalario.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().salario() == null ? "" : c.getValue().salario().toPlainString()));

        cmbEstado.getItems().setAll(EstadoOferta.values());
        cmbEstado.setOnAction(e -> filtrar());

        SortedList<Oferta> ordenadas = new SortedList<>(filtradas);
        ordenadas.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenadas);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        recargar();
    }

    /**
     * Aplica el texto y el estado seleccionados.
     */
    @FXML
    private void filtrar() {
        recargar();
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        filtradas.setPredicate(o -> texto.isEmpty()
                || o.titulo().toLowerCase().contains(texto)
                || Objects.toString(o.nombreEmpresa(), "").toLowerCase().contains(texto));
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbEstado.setValue(null);
        filtrar();
    }

    /**
     * Pide confirmación y borra la oferta seleccionada.
     */
    @FXML
    private void borrarOferta() {
        Oferta seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAviso("Selecciona una oferta para borrarla.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Borrar la oferta '" + seleccionada.titulo() + "' y sus entrevistas?");
        confirmacion.setResizable(true);
        confirmacion.initOwner(ventana());
        confirmacion.showAndWait().filter(ButtonType.OK::equals).ifPresent(boton -> {
            try {
                servicio.borrarOferta(seleccionada.id());
                recargar();
            } catch (TrackerException e) {
                mostrarError(e.getMessage());
            }
        });
    }

    @FXML
    private void nuevaOferta() {
        dialogo.alta(ventana()).ifPresent(oferta -> {
            recargar();
            seleccionar(oferta);
        });
    }

    @FXML
    private void verOferta() {
        Oferta seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAviso("Selecciona una oferta para ver su detalle.");
            return;
        }
        dialogo.mostrarDetalle(ventana(), seleccionada);
    }

    @FXML
    private void editarOferta() {
        Oferta seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAviso("Selecciona una oferta para editarla.");
            return;
        }
        dialogo.edicion(ventana(), seleccionada).ifPresent(oferta -> {
            recargar();
            seleccionar(oferta);
        });
    }

    /**
     * Vuelve a pedir al servicio las ofertas que cumplen el estado seleccionado.
     */
    private void recargar() {
        try {
            ofertas.setAll(servicio.listarOfertas(
                    new FiltroOferta(null, cmbEstado.getValue(), null, null, null)));
        } catch (TrackerException e) {
            mostrarError("No se pudieron cargar las ofertas: " + e.getMessage());
        }
    }

    private void seleccionar(Oferta oferta) {
        if (oferta == null) {
            return;
        }
        tabla.getItems().stream()
                .filter(o -> Objects.equals(o.id(), oferta.id()))
                .findFirst()
                .ifPresent(o -> {
                    tabla.getSelectionModel().select(o);
                    tabla.scrollTo(o);
                });
    }

    private javafx.stage.Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }

    private void mostrarAviso(String mensaje) {
        mostrarAlerta(Alert.AlertType.INFORMATION, mensaje);
    }

    private void mostrarError(String mensaje) {
        mostrarAlerta(Alert.AlertType.ERROR, mensaje);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setResizable(true);
        alerta.initOwner(ventana());
        alerta.showAndWait();
    }
}