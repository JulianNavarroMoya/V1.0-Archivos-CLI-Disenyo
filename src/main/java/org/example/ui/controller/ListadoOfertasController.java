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

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controlador del listado de ofertas: muestra, filtra, ordena y borra ofertas.
 */
public class ListadoOfertasController implements Initializable {

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
    private final ObservableList<Oferta> ofertas = FXCollections.observableArrayList();
    private final FilteredList<Oferta> filtradas = new FilteredList<>(ofertas, o -> true);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().titulo()));
        colEmpresa.setCellValueFactory(c ->
                new SimpleStringProperty(Objects.toString(c.getValue().nombreEmpresa(), "")));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estado().valorBd()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fechaPostulacion().toString()));
        colSalario.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().salario() == null ? "" : c.getValue().salario().toPlainString()));

        cmbEstado.getItems().setAll(EstadoOferta.values());
        cmbEstado.setOnAction(e -> filtrar());

        SortedList<Oferta> ordenadas = new SortedList<>(filtradas);
        ordenadas.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenadas);

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
        mostrarAviso("El formulario de alta llega en el siguiente commit.");
    }

    @FXML
    private void verOferta() {
        mostrarAviso("El detalle de la oferta llega en un commit posterior.");
    }

    @FXML
    private void editarOferta() {
        mostrarAviso("La edición llega en el siguiente commit.");
    }

    /**
     * Vuelve a pedir al servicio las ofertas que cumplen el estado seleccionado.
     */
    private void recargar() {
        ofertas.setAll(servicio.listarOfertas(
                new FiltroOferta(null, cmbEstado.getValue(), null, null, null)));
    }

    private void mostrarAviso(String mensaje) {
        new Alert(Alert.AlertType.INFORMATION, mensaje).showAndWait();
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
    }
}