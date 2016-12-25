package view;

import javafx.geometry.Insets;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URISyntaxException;

/**
 * View handling the GUI. This is the topPane itself.
 */
public class View extends GridPane {

    /**
     * Menu Bar for the program
     */
    private MenuBar menuBar;

    /**
     * The file menu
     */
    private Menu fileMenu;

    /**
     * MenuItem to load the graph.
     */
    MenuItem loadFileMenuItem;

    /**
     * MenuItem to save the graph.
     */
    MenuItem saveFileMenuItem;

    /**
     * The graph menu
     */
    private Menu graphMenu;

    /**
     * MenuItem to clear the graph.
     */
    MenuItem clearGraphMenuItem;

    /**
     * MenuItem to run the embedder on the graph.
     */
    MenuItem runEmbedderMenuItem;

    /**
     * MenuItem to reset the rotation Transformations of the graph.
     */
    MenuItem resetRotationMenuItem;

    /**
     * VBox holding stats of the graph.
     */
    private VBox statLabelsVBox;

    /**
     * Label for number of nodes.
     */
    Label numberOfNodesLabel;

    /**
     * Label for number of edges.
     */
    Label numberOfEdgesLabel;

    /**
     * TGF FileChooser for input and output.
     */
    FileChooser tgfFileChooser;

    /**
     * Pane holding the nodes and edges view of the graph.
     */
    Pane bottomPane;

    /**
     * Pane holding 2D objects stacked above the 3D objects.
     */
    Pane topPane;

    /**
     * Stacking two panes.
     */
    private StackPane stack2D3DPane;

    /**
     * Construct the view.View.
     */
    public View() {
        menuBar = new MenuBar();

        fileMenu = new Menu("File");
        loadFileMenuItem = new MenuItem("Load from file...");
        saveFileMenuItem = new MenuItem("Save to file...");

        graphMenu = new Menu("Graph");
        clearGraphMenuItem = new MenuItem("Clear graph");
        runEmbedderMenuItem = new MenuItem("Run Embedder");
        resetRotationMenuItem = new MenuItem("Reset Rotation");

        statLabelsVBox = new VBox();
        numberOfEdgesLabel = new Label();
        numberOfNodesLabel = new Label();

        bottomPane = new Pane();
        topPane = new Pane();
        // this is in order to make the top pane transparent for mouse events etc. because the top pane should not do
        // anything but show the BoundingBoxes2D -> Therefore no mouse events to be handled, these are passed to the
        // bottomPane of the stackPane
        topPane.setPickOnBounds(false);
        stack2D3DPane = new StackPane();

        setStyle();
        setMenus();
        setUpInputFileChooser();
        setSceneGraphTree();
    }

    /**
     * Set the menu bar's elements and their texts.
     */
    private void setMenus() {
        fileMenu.getItems().addAll(loadFileMenuItem, saveFileMenuItem);
        graphMenu.getItems().addAll(clearGraphMenuItem, runEmbedderMenuItem, resetRotationMenuItem);
        menuBar.getMenus().addAll(fileMenu, graphMenu);
    }

    /**
     * Set up the FileChooser for the input TGF file.
     */
    private void setUpInputFileChooser() {
        tgfFileChooser = new FileChooser();
        tgfFileChooser.setTitle("Choose a TGF formatted file...");
        try {
            tgfFileChooser.initialDirectoryProperty().setValue(
                    new File(View.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile());

        } catch (URISyntaxException e) {
        }
    }

    /**
     * Set up the node graph of the view.
     */
    private void setSceneGraphTree() {
        statLabelsVBox.getChildren().addAll(numberOfEdgesLabel, numberOfNodesLabel);
        stack2D3DPane.getChildren().addAll(bottomPane, topPane);
        this.addColumn(0, menuBar, stack2D3DPane, statLabelsVBox);
    }

    /**
     * Set the view's style with necessary insets.
     */
    private void setStyle() {

        // Show a border fo the node-containing pane
        stack2D3DPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                BorderWidths.DEFAULT)));
        // Some inset to be used
        Insets insets = new Insets(5, 5, 5, 5);
        // set insets for all necessary nodes in the scene graph

        setMargin(stack2D3DPane, insets);
        setMargin(statLabelsVBox, insets);
        setMargin(numberOfEdgesLabel, new Insets(5, 20, 5, 5));
        setMargin(numberOfNodesLabel, new Insets(5, 20, 5, 5));
    }

    /**
     * Set the graph node in the view.
     *
     * @param subScene The graph view.
     */
    void set3DGraphScene(SubScene subScene) {
        bottomPane.getChildren().add(subScene);
    }

    /**
     * Set the nodePane's height and width.
     *
     * @param width  width to be set to.
     * @param height height to be set to.
     */
    void setPaneDimensions(double width, double height) {
        // Set the height and width of the pane which will hold the node and edge representation
        bottomPane.setPrefWidth(width);
        bottomPane.setPrefHeight(height);
    }
}
