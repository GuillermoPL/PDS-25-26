package es.um.pds.tableros.infrastructure.javafx.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.CambiarBloqueoBoardCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CompartirBoardCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearReglaCommand;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.infrastructure.javafx.SceneManager;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;
import es.um.pds.tableros.infrastructure.mappers.CardMapper;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import es.um.pds.tableros.infrastructure.rest.dto.CardDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @brief Controlador de la vista del tablero (JavaFX Controller).
 * Clase de la capa de infraestructura encargada de gestionar y pintar la vista interactiva 
 * de un tablero Kanban, sus listas de tareas (columnas) y las tarjetas. Controla la lógica visual, 
 * los filtros por etiquetas/colores, las operaciones drag-and-drop y la delegación de comandos.
 */
@Component
public class BoardViewController {

    // ── Constantes de "sin filtro" ─────────────────────────────────────────────
    private static final String SIN_FILTRO_NOMBRE = "Todas";
    private static final String SIN_FILTRO_COLOR  = "Todos";

    // ── Dependencias inyectadas por Spring ────────────────────────────────────
    private final BoardService       boardService;
    private final CardService        cardService;
    private final BoardMapper        boardMapper;
    private final CardMapper         cardMapper;
    private final ApplicationContext springContext;
    private final SceneManager       sceneManager;

    // ── Nodos FXML ─────────────────────────────────────────────────────────────
    @FXML private Label              lblTituloTablero;
    @FXML private Button             btnBloqueo;
    @FXML private HBox               hboxColumnas;
    @FXML private ComboBox<String>   cbFiltroEtiquetas; // filtra por nombre
    @FXML private ComboBox<String>   cbFiltroColor;     // filtra por color hex

    // ── Estado ────────────────────────────────────────────────────────────────
    private String  boardIdActual;
    private boolean estadoBloqueoActual;
    private boolean tienePermisoEscrituraActual;
    private String  filtroNombreActual = SIN_FILTRO_NOMBRE;
    private String  filtroColorActual  = SIN_FILTRO_COLOR;
    private String listCompletadasId;

    /**
     * @brief Constructor con inyección automática de dependencias de la infraestructura y el dominio.
     * @param boardService Puerto de entrada para gestionar la lógica de tableros.
     * @param cardService Puerto de entrada para gestionar la lógica de tarjetas.
     * @param boardMapper Componente de mapeo para DTOs y Entidades de tablero.
     * @param cardMapper Componente de mapeo para DTOs y Entidades de tarjeta.
     * @param springContext Contexto de Spring utilizado para la factoría de controladores secundarios.
     * @param sceneManager Gestor de navegación de escenas de la aplicación.
     */
    public BoardViewController(BoardService boardService, CardService cardService,
                               BoardMapper boardMapper, CardMapper cardMapper,
                               ApplicationContext springContext, SceneManager sceneManager) { // <--- NUEVO: Inyectamos
        this.boardService  = boardService;
        this.cardService   = cardService;
        this.boardMapper   = boardMapper;
        this.cardMapper    = cardMapper;
        this.springContext = springContext;
        this.sceneManager  = sceneManager;
    }

    // ── Punto de entrada desde SceneManager ───────────────────────────────────
    
    /**
     * @brief Inicializa el estado del tablero visual basándose en un identificador único.
     * @param boardId Identificador de la base de datos del tablero a renderizar.
     */
    public void inicializarTablero(String boardId) {
        this.boardIdActual = boardId;
        configurarCeldaColor(); // configura la cellFactory UNA sola vez
        renderizarTodo();
    }

    // ── Renderizado principal ─────────────────────────────────────────────────
    
    /**
     * @brief Redibuja por completo la interfaz gráfica del tablero Kanban.
     * Limpia los componentes visuales anteriores, lee el estado del tablero actual, 
     * calcula los permisos de escritura del usuario activo, aplica los filtros a las tarjetas 
     * y genera dinámicamente los elementos VBox de las listas en el contenedor horizontal.
     */
    private void renderizarTodo() {
        hboxColumnas.getChildren().clear();

        BoardDTO tablero = boardService.obtenerTableroPorId(boardIdActual)
                .map(boardMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado"));
        String emailUsuario = sceneManager.getCurrentUserEmail();
        boolean esDueno = tablero.getEmail().equals(emailUsuario);
        String rolInvitado = (tablero.getPermisos() != null) ? tablero.getPermisos().get(emailUsuario) : null;
        this.tienePermisoEscrituraActual = esDueno || "WRITE".equals(rolInvitado);
        List<CardDTO> todasLasTarjetas = cardService
                .obtenerTarjetasPorTablero(boardIdActual)
                .stream().map(cardMapper::toDTO).toList();

        lblTituloTablero.setText(tablero.getTitulo());
        this.estadoBloqueoActual = tablero.isLocked();
        btnBloqueo.setText(this.estadoBloqueoActual ? "Desbloquear 🔓" : "Bloquear 🔒");

        this.listCompletadasId = tablero.getListCompletadasId();

        actualizarComboNombre(todasLasTarjetas);
        actualizarComboColor(todasLasTarjetas);

        List<CardDTO> tarjetasFiltradas = todasLasTarjetas.stream()
                .filter(this::pasaFiltroNombre)
                .filter(this::pasaFiltroColor)
                .toList();

        for (BoardDTO.ListaDTO listaInfo : tablero.getListas()) {
            List<CardDTO> tarjetasDeEstaLista = tarjetasFiltradas.stream()
                    .filter(c -> listaInfo.getId().equals(c.getListIdActual()))
                    .toList();

            boolean esListaCompletadas = listaInfo.getId().equals(this.listCompletadasId);

            hboxColumnas.getChildren().add(
                crearColumnaVisual(listaInfo.getId(), listaInfo.getNombre(),
                                   tarjetasDeEstaLista, esListaCompletadas));
        }
    }

    // ── Actualización de ComboBoxes ───────────────────────────────────────────
    
    /**
     * @brief Refresca los ítems del filtro por nombre de etiqueta basándose en los datos actuales.
     * @param tarjetas Lista completa de tarjetas del tablero.
     */
    private void actualizarComboNombre(List<CardDTO> tarjetas) {
        List<String> nombres = tarjetas.stream()
                .filter(c -> c.getEtiquetas() != null)
                .flatMap(c -> c.getEtiquetas().stream())
                .map(CardDTO.EtiquetaDTO::getNombre)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        nombres.add(0, SIN_FILTRO_NOMBRE);
        cbFiltroEtiquetas.setOnAction(null);
        cbFiltroEtiquetas.getItems().setAll(nombres);
        
        if (!nombres.contains(filtroNombreActual)) filtroNombreActual = SIN_FILTRO_NOMBRE;
        cbFiltroEtiquetas.setValue(filtroNombreActual);
        
        cbFiltroEtiquetas.setOnAction(e -> {
            String v = cbFiltroEtiquetas.getValue();
            if (v != null) { filtroNombreActual = v; renderizarTodo(); }
        });
    }

    /**
     * @brief Refresca los ítems del filtro cromático basándose en los códigos de color actuales.
     * @param tarjetas Lista completa de tarjetas del tablero.
     */
    private void actualizarComboColor(List<CardDTO> tarjetas) {
        List<String> colores = tarjetas.stream()
                .filter(c -> c.getEtiquetas() != null)
                .flatMap(c -> c.getEtiquetas().stream())
                .map(CardDTO.EtiquetaDTO::getColor)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        colores.add(0, SIN_FILTRO_COLOR);
        cbFiltroColor.setOnAction(null);
        cbFiltroColor.getItems().setAll(colores);
        
        if (!colores.contains(filtroColorActual)) filtroColorActual = SIN_FILTRO_COLOR;
        cbFiltroColor.setValue(filtroColorActual);
        
        cbFiltroColor.setOnAction(e -> {
            String v = cbFiltroColor.getValue();
            if (v != null) { filtroColorActual = v; renderizarTodo(); }
        });
    }

    /**
     * @brief Configura las celdas personalizadas del ComboBox de color.
     * Modifica el renderizado nativo de JavaFX instalando una factoría para pintar un 
     * Rectangle con el color web correspondiente al lado del texto identificativo.
     */
    private void configurarCeldaColor() {
        cbFiltroColor.setCellFactory(lv -> new ListCell<>() {
            private final Rectangle rect = new Rectangle(16, 16);
            private final Label     lbl  = new Label();
            private final HBox      box  = new HBox(8, rect, lbl);
            { 
                box.setStyle("-fx-alignment: CENTER_LEFT;"); 
                lbl.setStyle("-fx-text-fill: #333333;");
            }

            @Override
            protected void updateItem(String colorHex, boolean empty) {
                super.updateItem(colorHex, empty);
                if (empty || colorHex == null) {
                    setGraphic(null);
                    setText(null);
                } else if (SIN_FILTRO_COLOR.equals(colorHex)) {
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.LIGHTGRAY);
                    lbl.setText("Todos los colores");
                    setGraphic(box);
                    setText(null);
                } else {
                    try {
                        rect.setFill(Color.web(colorHex));
                        rect.setStroke(Color.TRANSPARENT);
                    } catch (Exception ex) {
                        rect.setFill(Color.GRAY);
                    }
                    lbl.setText(colorHex);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        cbFiltroColor.setButtonCell(new ListCell<>() {
            private final Rectangle rect = new Rectangle(14, 14);
            private final Label     lbl  = new Label();
            private final HBox      box  = new HBox(6, rect, lbl);
            { 
                box.setStyle("-fx-alignment: CENTER_LEFT;"); 
                lbl.setStyle("-fx-text-fill: #333333;");
            }

            @Override
            protected void updateItem(String colorHex, boolean empty) {
                super.updateItem(colorHex, empty);
                if (empty || colorHex == null) {
                    setGraphic(null);
                    setText(null);
                } else if (SIN_FILTRO_COLOR.equals(colorHex)) {
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.LIGHTGRAY);
                    lbl.setText("Todos");
                    setGraphic(box);
                    setText(null);
                } else {
                    try {
                        rect.setFill(Color.web(colorHex));
                        rect.setStroke(Color.TRANSPARENT);
                    } catch (Exception ex) {
                        rect.setFill(Color.GRAY);
                    }
                    lbl.setText(colorHex);
                    setGraphic(box);
                    setText(null);
                }
            }
        });
    }

    // ── Predicados de filtro ──────────────────────────────────────────────────
    
    /**
     * @brief Evalúa si una tarjeta cumple con la selección del filtro por nombre de etiqueta.
     * @param c Instancia de tarjeta DTO.
     * @return true si supera el filtro, false en caso contrario.
     */
    private boolean pasaFiltroNombre(CardDTO c) {
        if (SIN_FILTRO_NOMBRE.equals(filtroNombreActual)) return true;
        return c.getEtiquetas() != null &&
               c.getEtiquetas().stream().anyMatch(e -> filtroNombreActual.equals(e.getNombre()));
    }

    /**
     * @brief Evalúa si una tarjeta cumple con la selección del filtro por color de etiqueta.
     * @param c Instancia de tarjeta DTO.
     * @return true si supera el filtro, false en caso contrario.
     */
    private boolean pasaFiltroColor(CardDTO c) {
        if (SIN_FILTRO_COLOR.equals(filtroColorActual)) return true;
        return c.getEtiquetas() != null &&
               c.getEtiquetas().stream().anyMatch(e -> filtroColorActual.equals(e.getColor()));
    }

    // ── Construcción de columnas ───────────────────────────────────────────────
    
    /**
     * @brief Fabrica programáticamente la estructura gráfica VBox de una columna (Lista de tareas).
     * Aplica estilos condicionales si es la columna Done, renderiza sus tarjetas e inicializa los 
     * escuchadores DragOver y DragDropped para procesar movimientos interactivos mediante comandos.
     * @param listId ID único de la lista.
     * @param nombreLista Nombre legible de la lista.
     * @param tarjetas Colección filtrada de tarjetas que contiene.
     * @param esListaCompletadas Indica si es la lista especial designada para tareas finalizadas.
     * @return El contenedor VBox de la columna listo para añadirse a la escena.
     */
    private VBox crearColumnaVisual(String listId, String nombreLista, List<CardDTO> tarjetas, boolean esListaCompletadas) {
    	VBox columna = new VBox(8);
    	columna.setPrefWidth(250);
	

		if (esListaCompletadas) {
			columna.setStyle(
			"-fx-background-color: #d5f5e3; -fx-background-radius: 5; -fx-padding: 10; " +
			"-fx-border-color: #27ae60; -fx-border-radius: 5; -fx-border-width: 1.5;");
		} else {
			columna.setStyle(
			"-fx-background-color: #ebecf0; -fx-background-radius: 5; -fx-padding: 10;");
		}
		
		String tituloTexto = esListaCompletadas
							? "✅ " + nombreLista + " (" + tarjetas.size() + ")"
							: nombreLista + " (" + tarjetas.size() + ")";
		
		Label lblTitulo = new Label(tituloTexto);
		lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; " +
							"-fx-padding: 0 0 6 0; -fx-text-fill: #333333;");
		columna.getChildren().add(lblTitulo);
		
		VBox contenedorTarjetas = new VBox(8);
		contenedorTarjetas.setPadding(new Insets(4, 0, 4, 0));
		tarjetas.forEach(t -> contenedorTarjetas.getChildren().add(crearTarjetaVisual(t)));
		columna.getChildren().add(contenedorTarjetas);
		
		Button btnAnadirTarjeta = new Button("+ Añadir tarjeta");
		btnAnadirTarjeta.setMaxWidth(Double.MAX_VALUE);
		btnAnadirTarjeta.setStyle("-fx-background-color: rgba(0,0,0,0.08); -fx-cursor: hand;");
		btnAnadirTarjeta.setDisable(this.estadoBloqueoActual || !this.tienePermisoEscrituraActual);
		btnAnadirTarjeta.setOnAction(e -> abrirDialogoNuevaTarjeta(listId));
		columna.getChildren().add(btnAnadirTarjeta);
		
		columna.setOnDragOver(event -> {
            // Solo permite el "DragOver" si tiene permisos
            if (this.tienePermisoEscrituraActual && event.getGestureSource() != columna && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
		
		columna.setOnDragDropped(event -> {
			Dragboard db = event.getDragboard();
			boolean exito = false;
			if (db.hasString()) {
				String cardIdVolando = db.getString();
				try {
					String emailUsuario = sceneManager.getCurrentUserEmail();
					MoverCardCommand cmd = new MoverCardCommand(cardIdVolando, boardIdActual, listId, emailUsuario);
					cardService.moverTarjeta(cmd);
					exito = true;
					renderizarTodo();
				} catch (IllegalArgumentException | IllegalStateException e) {
					mostrarAlertaRegla(e.getMessage());
				}
			}
			event.setDropCompleted(exito);
			event.consume();
		});
		
		return columna;
	}

    /**
     * @brief Construye programáticamente el componente visual VBox para representar una tarjeta.
     * Estiliza el recuadro según su estado, añade indicadores, dibuja los sub-elementos de checklists 
     * y chips de etiquetas, inyecta el botón rápido de completado e inicializa los disparadores DragDetected.
     * @param tarjeta Estructura DTO de datos planos de la tarjeta.
     * @return El nodo VBox de la tarjeta.
     */
    private VBox crearTarjetaVisual(CardDTO tarjeta) {
        VBox tarjetaVisual = new VBox(4);

        if (tarjeta.isCompletada()) {
            tarjetaVisual.setStyle(
                "-fx-background-color: #eafaf1; -fx-padding: 10; -fx-background-radius: 4; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1); " +
                "-fx-cursor: hand; -fx-border-color: #27ae60; " +
                "-fx-border-radius: 4; -fx-border-width: 1.5;");
        } else {
            tarjetaVisual.setStyle(
                "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 4; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1); " +
                "-fx-cursor: hand;");
        }

        String iconoTipo;
        if (tarjeta.isCompletada()) {
            iconoTipo = "✅ ";
        } else if ("CHECKLIST".equals(tarjeta.getTipo())) {
            iconoTipo = "☑ ";
        } else {
            iconoTipo = "📋 ";
        }

        Label lblTitulo = new Label(iconoTipo + tarjeta.getTitulo());

        if (tarjeta.isCompletada()) {
            lblTitulo.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-text-fill: #888888; -fx-strikethrough: true;");
        } else {
            lblTitulo.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        }

        lblTitulo.setWrapText(true);
        tarjetaVisual.getChildren().add(lblTitulo);

        // Checklist items
        if ("CHECKLIST".equals(tarjeta.getTipo())
                && tarjeta.getChecklistItems() != null
                && !tarjeta.getChecklistItems().isEmpty()) {
            VBox contenedorChecklist = new VBox(3);
            contenedorChecklist.setPadding(new Insets(4, 0, 4, 12));
            for (String paso : tarjeta.getChecklistItems()) {
                CheckBox chkPaso = new CheckBox(paso);
                chkPaso.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");
                chkPaso.setDisable(true);
                chkPaso.setOpacity(0.85);
                contenedorChecklist.getChildren().add(chkPaso);
            }
            tarjetaVisual.getChildren().add(contenedorChecklist);
        }

        // Etiquetas
        if (tarjeta.getEtiquetas() != null && !tarjeta.getEtiquetas().isEmpty()) {
            HBox chips = new HBox(4);
            tarjeta.getEtiquetas().forEach(et -> {
                Label chip = new Label(et.getNombre());
                chip.setStyle(
                    "-fx-background-color: " + et.getColor() + "; " +
                    "-fx-text-fill: white; -fx-padding: 2 6 2 6; " +
                    "-fx-background-radius: 8; -fx-font-size: 10px;");
                chips.getChildren().add(chip);
            });
            tarjetaVisual.getChildren().add(chips);
        }

        if (tarjeta.isCompletada()) {
            Label badge = new Label("✓ Completada");
            badge.setStyle(
                "-fx-text-fill: #27ae60; -fx-font-size: 10px; -fx-font-weight: bold;");
            tarjetaVisual.getChildren().add(badge);
        }

        if (!tarjeta.isCompletada() && listCompletadasId != null && this.tienePermisoEscrituraActual) {
            Button btnCompletar = new Button("✓ Completar");
            btnCompletar.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-font-size: 10px; -fx-padding: 3 8 3 8; " +
                "-fx-background-radius: 4; -fx-cursor: hand;");
            btnCompletar.setOnAction(e -> {
            	String emailUsuario = sceneManager.getCurrentUserEmail();
            	MoverCardCommand cmd = new MoverCardCommand(tarjeta.getId(), boardIdActual, listCompletadasId, emailUsuario);
                cardService.moverTarjeta(cmd);
                renderizarTodo();
                e.consume(); // evita que el click active el drag
            });
            btnCompletar.setOnDragDetected(javafx.event.Event::consume);
            tarjetaVisual.getChildren().add(btnCompletar);
        }

        tarjetaVisual.setOnDragDetected(event -> {
        	if (!this.tienePermisoEscrituraActual || this.estadoBloqueoActual) {
                event.consume();
                return;
            }
            Dragboard db = tarjetaVisual.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(tarjeta.getId());
            db.setContent(content);
            event.consume();
        });

        return tarjetaVisual;
    }

    // ── Acciones FXML ──────────────────────────────────────────────────────────
    
    /**
     * @brief Maneja el evento FXML para retroceder al Dashboard de tableros.
     */
    @FXML
    public void handleVolver() {
        sceneManager.navigateToDashboard();
    }
    
    /**
     * @brief Maneja el evento FXML para alternar el estado de bloqueo del tablero.
     * Comprueba permisos de escritura y orquesta la llamada al caso de uso.
     */
    @FXML
    public void handleAlternarBloqueo() {
        if (!this.tienePermisoEscrituraActual) {
            mostrarAlertaError("Permiso denegado", "Solo los usuarios con permiso de escritura pueden realizar esta acción.");
            return;
        }
        try {
            // AÑADIDO: Sacamos el email del usuario logueado
            String emailUsuario = sceneManager.getCurrentUserEmail(); 
            
            // AÑADIDO: Le pasamos el emailUsuario como 3er parámetro al comando
            CambiarBloqueoBoardCommand cmd =
                new CambiarBloqueoBoardCommand(boardIdActual, !this.estadoBloqueoActual, emailUsuario); 
                
            boardService.cambiarEstadoBloqueo(cmd);
            renderizarTodo();
        } catch (Exception e) {
            mostrarAlertaError("Error al cambiar bloqueo", e.getMessage());
        }
    }

    /**
     * @brief Maneja el evento FXML para desplegar de forma modal la ventana de adición de listas.
     */
    @FXML
    public void handleAnadirListaVentana() {
    	if (!this.tienePermisoEscrituraActual) {
            mostrarAlertaError("Permiso denegado", "Solo los usuarios con permiso de escritura pueden realizar esta acción.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AnadirLista.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            AnadirListaController ctrl = loader.getController();
            ctrl.setBoardId(this.boardIdActual);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(obtenerVentanaPrincipal());
            dialog.setTitle("Añadir Lista");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            renderizarTodo();
        } catch (IOException e) {
            mostrarAlertaError("Error al abrir diálogo", e.getMessage());
        }
    }
    
    /**
     * @brief Maneja el evento FXML para desplegar de forma modal el panel de definición de la lista Done.
     */
    @FXML
    public void handleAjustesTablero() {
    	if (!this.tienePermisoEscrituraActual) {
            mostrarAlertaError("Permiso denegado", "Solo los usuarios con permiso de escritura pueden realizar esta acción.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DefinirCompletadas.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            
            DefinirCompletadasController ctrl = loader.getController();
            ctrl.setBoardId(this.boardIdActual);
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(obtenerVentanaPrincipal());
            dialog.setTitle("Ajustes del Tablero");
            dialog.setScene(new Scene(root));
            
            dialog.showAndWait(); 
            renderizarTodo(); 
            
        } catch (IOException e) {
            mostrarAlertaError("Error al abrir ajustes", e.getMessage());
        }
    }

    /**
     * @brief Maneja el evento FXML para desplegar modalmente las trazas del historial de auditoría.
     */
    @FXML
    public void handleVerHistorial() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Historial.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            HistorialController ctrl = loader.getController();
            ctrl.setBoardId(this.boardIdActual);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(obtenerVentanaPrincipal());
            dialog.setTitle("Historial del Tablero");
            dialog.setScene(new Scene(root));
            dialog.show();
        } catch (IOException e) {
            mostrarAlertaError("Error al abrir historial", e.getMessage());
        }
    }

    /**
     * @brief Resetea los filtros activos limpiando los estados de selección y redibujando la vista.
     */
    @FXML
    public void handleLimpiarFiltros() {
        filtroNombreActual = SIN_FILTRO_NOMBRE;
        filtroColorActual  = SIN_FILTRO_COLOR;
        renderizarTodo();
    }
    
    /**
     * @brief Maneja el evento FXML para desplegar e interactuar con la gestión compartida de accesos.
     * Crea controles dinámicos en memoria y coordina las llamadas de invitación o revocación de permisos.
     */
    @FXML
    public void handleCompartirTablero() {
    	if (!this.tienePermisoEscrituraActual) {
            mostrarAlertaError("Permiso denegado", "Solo los usuarios con permiso de escritura pueden realizar esta acción.");
            return;
        }
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(obtenerVentanaPrincipal());
        dialog.setTitle("Gestionar acceso al tablero");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setPrefWidth(400);

        // 1. DECLARAR LA LISTA PRIMERO para que exista cuando el botón la necesite
        ListView<String> listaPermisos = new ListView<>();
        listaPermisos.setPrefHeight(180);

        // ── Sección: compartir ────────────────────────────────────────────────
        Label lblCompartir = new Label("Invitar a usuario");
        lblCompartir.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField txtEmailInvitado = new TextField();
        txtEmailInvitado.setPromptText("Email del invitado...");

        ComboBox<String> cbRol = new ComboBox<>();
        cbRol.getItems().addAll("WRITE", "READ");
        cbRol.setValue("WRITE");

        Button btnInvitar = new Button("Invitar");
        btnInvitar.setStyle("-fx-background-color: #0079bf; -fx-text-fill: white; -fx-font-weight: bold;");

        Label lblResultado = new Label();

        // 2. AHORA EL BOTÓN PUEDE USAR LA LISTA
        btnInvitar.setOnAction(e -> {
            String emailInvitado = txtEmailInvitado.getText().trim();
            String rol = cbRol.getValue();
            String emailDueno = sceneManager.getCurrentUserEmail();

            if (emailInvitado.isBlank()) {
                lblResultado.setStyle("-fx-text-fill: #e74c3c;");
                lblResultado.setText("Introduce el email del invitado.");
                return;
            }
            try {
                boardService.compartirTablero(
                    new CompartirBoardCommand(boardIdActual, emailDueno, emailInvitado, rol));
                lblResultado.setStyle("-fx-text-fill: #27ae60;");
                lblResultado.setText("✓ Tablero compartido con " + emailInvitado);
                txtEmailInvitado.clear();
                
                // ¡Ya no dará error!
                BoardDTO tableroDTO = obtenerTableroActualizado();
                actualizarListaPermisos(listaPermisos, tableroDTO);
                
            } catch (IllegalStateException ex) {
                lblResultado.setStyle("-fx-text-fill: #e74c3c;");
                lblResultado.setText("Solo el dueño puede compartir el tablero.");
            } catch (IllegalArgumentException ex) {
                lblResultado.setStyle("-fx-text-fill: #e74c3c;");
                lblResultado.setText("Email inválido: " + ex.getMessage());
            }
        });

        HBox filaInvitar = new HBox(8, txtEmailInvitado, cbRol, btnInvitar);
        HBox.setHgrow(txtEmailInvitado, javafx.scene.layout.Priority.ALWAYS);

        // ── Sección: usuarios con acceso ──────────────────────────────────────
        Label lblAccesos = new Label("Usuarios con acceso");
        lblAccesos.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Llenamos la lista inicial
        actualizarListaPermisos(listaPermisos, obtenerTableroActualizado());

        Button btnRevocar = new Button("Revocar acceso seleccionado");
        btnRevocar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnRevocar.setOnAction(e -> {
            String seleccionado = listaPermisos.getSelectionModel().getSelectedItem();
            if (seleccionado == null) return;
            
            String emailAEliminar = seleccionado.split(" — ")[0].trim();
            String emailDueno = sceneManager.getCurrentUserEmail();
            try {
                boardService.revocarAcceso(boardIdActual, emailDueno, emailAEliminar);
                lblResultado.setStyle("-fx-text-fill: #27ae60;");
                lblResultado.setText("✓ Acceso revocado para " + emailAEliminar);
                actualizarListaPermisos(listaPermisos, obtenerTableroActualizado());
            } catch (IllegalStateException ex) {
                lblResultado.setStyle("-fx-text-fill: #e74c3c;");
                lblResultado.setText("No se puede revocar: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(
            lblCompartir, filaInvitar, lblResultado,
            new Separator(),
            lblAccesos, listaPermisos, btnRevocar
        );

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }
    
    /**
     * @brief Maneja el evento FXML para configurar y dar de alta reglas de automatización.
     * Despliega un formulario para asociar un trigger posicional con una acción del dominio.
     */
    @FXML
    public void handleAutomatizaciones() {
        if (!this.tienePermisoEscrituraActual) {
            mostrarAlertaError("Permiso denegado", "Solo los usuarios con permiso de escritura pueden crear reglas.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(obtenerVentanaPrincipal());
        dialog.setTitle("Automatizaciones");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setPrefWidth(450);

        // ── Formulario ────────────────────────────────────────────────
        Label lblCrear = new Label("Crear nueva regla");
        lblCrear.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox filaCondicion = new HBox(8);
        filaCondicion.setStyle("-fx-alignment: CENTER_LEFT;");
        Label lblSi = new Label("Si la tarjeta se mueve a:");
        
        ComboBox<String> cbListas = new ComboBox<>();
        // Rellenar puramente desde el DTO
        BoardDTO tableroDTO = obtenerTableroActualizado();
        if (tableroDTO.getListas() != null) {
            cbListas.getItems().addAll(tableroDTO.getListas().stream()
                    .map(BoardDTO.ListaDTO::getNombre).toList());
        }
        filaCondicion.getChildren().addAll(lblSi, cbListas);

        HBox filaAccion = new HBox(8);
        filaAccion.setStyle("-fx-alignment: CENTER_LEFT;");
        Label lblEntonces = new Label("Entonces:");
        
        ComboBox<String> cbAccion = new ComboBox<>();
        cbAccion.getItems().addAll("MARCAR_COMO_COMPLETADA", "AÑADIR_ETIQUETA_ROJA");
        
        filaAccion.getChildren().addAll(lblEntonces, cbAccion);

        Button btnCrear = new Button("Añadir Regla");
        btnCrear.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold;");

        Label lblResultado = new Label();

        // ── Lista Actual ─────────────────────────────────────────────
        Label lblActuales = new Label("Reglas activas");
        lblActuales.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ListView<String> listaReglas = new ListView<>();
        listaReglas.setPrefHeight(150);
        actualizarListaReglas(listaReglas, tableroDTO);

        // Acción del botón
        btnCrear.setOnAction(e -> {
            String nombreLista = cbListas.getValue();
            String accionStr = cbAccion.getValue();

            if (nombreLista == null || accionStr == null) {
                lblResultado.setStyle("-fx-text-fill: #e74c3c;");
                lblResultado.setText("Selecciona una lista y una acción.");
                return;
            }

            try {
                // Buscamos el ID real consultando el DTO, sin invocar a dominio
                BoardDTO dtoActual = obtenerTableroActualizado();
                String listId = dtoActual.getListas().stream()
                        .filter(l -> l.getNombre().equals(nombreLista))
                        .map(BoardDTO.ListaDTO::getId)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No se encontró la lista"));

                // Importa CrearReglaCommand arriba en tu archivo para no poner la ruta entera aquí
                boardService.anadirReglaAutomatizacion(new CrearReglaCommand(
                        boardIdActual, "TARJETA_MOVIDA_A_LISTA", listId, accionStr));
                
                lblResultado.setStyle("-fx-text-fill: #27ae60;");
                lblResultado.setText("✓ Regla creada");
                actualizarListaReglas(listaReglas, obtenerTableroActualizado());

            } catch (Exception ex) {
                lblResultado.setStyle("-fx-text-fill: #e74c3c;");
                lblResultado.setText("Error: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(
            lblCrear, filaCondicion, filaAccion, btnCrear, lblResultado,
            new Separator(),
            lblActuales, listaReglas
        );

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    /**
     * @brief Formatea y vuelca las reglas de automatización activas en el ListView de control.
     * @param lista Componente ListView de destino.
     * @param tableroDTO DTO de datos del tablero.
     */
    private void actualizarListaReglas(ListView<String> lista, BoardDTO tableroDTO) {
        if (tableroDTO.getReglas() != null) {
            lista.getItems().setAll(
                tableroDTO.getReglas().stream()
                    .map(r -> {
                        // Buscamos el nombre de la lista dentro de las listas del DTO
                        String nombreLista = tableroDTO.getListas().stream()
                                .filter(l -> l.getId().equals(r.getTriggerPayload()))
                                .map(BoardDTO.ListaDTO::getNombre)
                                .findFirst()
                                .orElse("Lista desconocida");
                        
                        return "Si se mueve a '" + nombreLista + "' ➔ " + r.getActionType();
                    })
                    .toList()
            );
        } else {
            lista.getItems().clear();
        }
    }
    
    /**
     * @brief Formatea e inyecta la traza de correos y roles colaboradores asignados en el ListView modal.
     * @param lista Componente ListView de destino.
     * @param tableroDTO DTO de datos del tablero.
     */
    private void actualizarListaPermisos(ListView<String> lista, BoardDTO tableroDTO) {
        if (tableroDTO.getPermisos() != null) {
            lista.getItems().setAll(
                tableroDTO.getPermisos().entrySet().stream()
                    .map(e -> e.getKey() + " — " + e.getValue())
                    .toList()
            );
        } else {
            lista.getItems().clear();
        }
    }
    
    // ── Helpers ───────────────────────────────────────────────────────────────
    
    /**
     * @brief Despliega de manera modal el cuadro secundario FXML para dar de alta nuevas tarjetas.
     * @param listId Identificador String de la columna contenedora donde nacerá la tarjeta.
     */
    private void abrirDialogoNuevaTarjeta(String listId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NuevaTarjeta.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            NuevaTarjetaController ctrl = loader.getController();
            ctrl.setBoardId(this.boardIdActual);
            ctrl.setListId(listId);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(obtenerVentanaPrincipal());
            dialog.setTitle("Nueva Tarjeta");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            renderizarTodo();
        } catch (IOException e) {
            mostrarAlertaError("Error al abrir diálogo", e.getMessage());
        }
    }

    /**
     * @brief Extrae de forma reflexiva la ventana Stage principal del escenario activo.
     * @return Instancia de tipo Window superior.
     */
    private Window obtenerVentanaPrincipal() {
        return hboxColumnas.getScene().getWindow();
    }

    /**
     * @brief Despliega un cuadro de diálogo informativo (Warning Alert) ante bloqueos del dominio o WIP limits.
     * @param mensaje Detalle del texto aclaratorio.
     */
    private void mostrarAlertaRegla(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Movimiento denegado");
        alert.setHeaderText("Regla de negocio incumplida");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * @brief Despliega un popup flotante modal informativo de error crítico (Error Alert) en la vista.
     * @param titulo Encabezado o título identificativo del modal.
     * @param mensaje Detalle técnico del error capturado por excepciones de infraestructura.
     */
    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * @brief Consulta síncronamente el estado rehidratado de los datos del tablero llamando al puerto de entrada.
     * @return Instancia limpia de tipo BoardDTO.
     */
    private BoardDTO obtenerTableroActualizado() {
        return boardService.obtenerTableroPorId(boardIdActual)
                .map(boardMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("El tablero ya no existe"));
    }
    
    /**
     * @brief Maneja el evento FXML para la edición interactiva del título del tablero.
     * * Verifica que el usuario activo posea los privilegios de escritura necesarios. 
     * En caso afirmativo, despliega un cuadro de diálogo nativo de JavaFX (TextInputDialog) 
     * para capturar el nuevo texto. Si el texto es válido y distinto al actual, construye 
     * un objeto de comando inmutable (RenombrarBoardCommand) y delega la ejecución 
     * al puerto de entrada de la capa de aplicación, forzando un repintado de la vista al finalizar.
     */
    @FXML
    public void handleEditarTitulo() {
        if (!this.tienePermisoEscrituraActual) {
            mostrarAlertaError("Permiso denegado", "Solo los usuarios con permiso de escritura pueden modificar el tablero.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(lblTituloTablero.getText());
        dialog.setTitle("Editar Tablero");
        dialog.setHeaderText("Modificar el título del tablero");
        dialog.setContentText("Nuevo título:");

        dialog.showAndWait().ifPresent(nuevoTitulo -> {
            if (!nuevoTitulo.isBlank() && !nuevoTitulo.equals(lblTituloTablero.getText())) {
                try {
                    String emailUsuario = sceneManager.getCurrentUserEmail();
                    
                    es.um.pds.tableros.domain.ports.input.board.commands.RenombrarBoardCommand cmd = 
                        new es.um.pds.tableros.domain.ports.input.board.commands.RenombrarBoardCommand(boardIdActual, nuevoTitulo, emailUsuario);
                    boardService.renombrarTablero(cmd);
                    
                    renderizarTodo(); // Refresca la vista y muestra el nuevo título
                } catch (Exception e) {
                    mostrarAlertaError("Error al modificar", e.getMessage());
                }
            }
        });
    }
}