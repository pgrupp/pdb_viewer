package view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URISyntaxException;

/**
 * View handling the GUI. This is the topPane itself.
 */
public class View extends BorderPane {

    /**
     * Observable set by Presenter, determining, if buttons should be clickable right now.
     */
    BooleanProperty disableButtons;

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
     * MenuItem to open the 1EY4 PDB file
     */
    MenuItem open1EY4MenuItem;

    /**
     * MenuItem to open the 2KL8 PDB file
     */
    MenuItem open2KL8MenuItem;

    /**
     * MenuItem to open the 2TGA PDB file
     */
    MenuItem open2TGAMenuItem;

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
    StackPane stack2D3DPane;

    /**
     * Graph tab.
     */
    Tab graphTab;

    /**
     * Cartoon tab.
     */
    Tab tableTab;

    /**
     * Tab pane containing the tabs for graph and cartoon view.
     */
    TabPane graphTabPane;


    /**
     * Construct the view.View.
     */
    public View() {

        disableButtons = new SimpleBooleanProperty(true);
        menuBar = new MenuBar();

        initializeMenu();

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

        graphTab = new Tab("PDB Viewer");
        tableTab = new Tab("Table");
        graphTabPane = new TabPane();

        setStyle();
        setMenus();
        setUpInputFileChooser();
        setSceneGraphTree();
        bindButtonsToDisableProperty();
    }

    /**
     * Initialize the menu of the view.
     */
    private void initializeMenu() {
        fileMenu = new Menu("File");
        loadFileMenuItem = new MenuItem("Load from file...");
        open1EY4MenuItem = new MenuItem("Open 1EY4 PDB file");
        open2KL8MenuItem = new MenuItem("Open 2KL8 PDB file");
        open2TGAMenuItem = new MenuItem("Open 2TGA PDB file");

        graphMenu = new Menu("Graph");
        clearGraphMenuItem = new MenuItem("Clear pdbmodel");
        runEmbedderMenuItem = new MenuItem("Run Embedder");
        resetRotationMenuItem = new MenuItem("Reset Rotation");
    }

    /**
     * Bind all buttons to the disable property in order to disable them, if Presenter tells them to.
     */
    private void bindButtonsToDisableProperty() {
        clearGraphMenuItem.disableProperty().bind(disableButtons);
        runEmbedderMenuItem.disableProperty().bind(disableButtons);
        resetRotationMenuItem.disableProperty().bind(disableButtons);
    }

    /**
     * Set the menu bar's elements and their texts.
     */
    private void setMenus() {
        fileMenu.getItems().addAll(loadFileMenuItem, open1EY4MenuItem, open2KL8MenuItem, open2TGAMenuItem);
        graphMenu.getItems().addAll(clearGraphMenuItem, runEmbedderMenuItem, resetRotationMenuItem);
        menuBar.getMenus().addAll(fileMenu, graphMenu);
    }

    /**
     * Set up the FileChooser for the input TGF file.
     */
    private void setUpInputFileChooser() {
        tgfFileChooser = new FileChooser();
        tgfFileChooser.setTitle("Choose a PDB formatted file...");
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
        final String os = System.getProperty ("os.name");
        if (os != null && os.startsWith ("Mac"))
            menuBar.useSystemMenuBarProperty().set(true);
        this.setTop(menuBar);
        this.setCenter(graphTabPane);
        this.setBottom(statLabelsVBox);
        //this.addColumn(0, menuBar, graphTabPane, statLabelsVBox);
        graphTabPane.getTabs().addAll(graphTab, tableTab);
        graphTab.setContent(stack2D3DPane);
    }

    /**
     * Set the view's style with necessary insets, widths and heights.
     */
    private void setStyle() {

        // Show a border fo the node-containing pane
        //stack2D3DPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
        //        BorderWidths.DEFAULT)));

        // Always have the bottom and top pane the same dimension the stack pane containing them.
        bottomPane.minWidthProperty().bind(stack2D3DPane.minWidthProperty());
        bottomPane.minHeightProperty().bind(stack2D3DPane.minHeightProperty());
        bottomPane.prefWidthProperty().bind(stack2D3DPane.prefWidthProperty());
        bottomPane.prefHeightProperty().bind(stack2D3DPane.prefHeightProperty());

        topPane.minWidthProperty().bind(stack2D3DPane.minWidthProperty());
        topPane.minHeightProperty().bind(stack2D3DPane.minHeightProperty());
        topPane.prefWidthProperty().bind(stack2D3DPane.prefWidthProperty());
        topPane.prefHeightProperty().bind(stack2D3DPane.prefHeightProperty());

        // Some inset to be used
        Insets insets = new Insets(5, 5, 5, 5);
        // set insets for all necessary nodes in the scene graph

        //setMargin(stack2D3DPane, insets);
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
        subScene.widthProperty().bind(stack2D3DPane.widthProperty());
        subScene.heightProperty().bind(stack2D3DPane.heightProperty());
    }

    /**
     * Set the nodePane's height and width.
     *
     * @param width  width to be set to.
     * @param height height to be set to.
     */
    void setPaneDimensions(double width, double height) {
        // Set the height and width of the pane which will hold the node and edge representation
        stack2D3DPane.setMinWidth(width);
        stack2D3DPane.setMinHeight(height);
        graphTabPane.setMinHeight(height);
        graphTabPane.setMinWidth(width);
    }
}
