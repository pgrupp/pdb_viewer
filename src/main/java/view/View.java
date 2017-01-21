package view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

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
    Menu fileMenu;

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
     * The view menu
     */
    private Menu editMenu;

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

    private Menu viewMenu;


    /**
     * Displays any status in the status bar.
     */
    Label status;

    /**
     * Progress Bar displaying any made progress if neccessary.
     */
    ProgressBar progressBar;

    /**
     * VBox holding stats of the graph.
     */
    private HBox statusBar;

    /**
     * VBox containing the menus and controls.
     */
    private VBox menusVBox;

    /**
     * A toolbar with mostly used buttons.
     */
    ToolBar toolBar;

    /**
     * ScrollPane containing the sequenceFlowPane, adding a scrollbar if the sequence is large
     */
    ScrollPane sequenceScrollPane;

    /**
     * Pane showing the sequence and secondary structure.
     */
    FlowPane sequenceFlowPane;

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

        status = new Label();
        progressBar = new ProgressBar();
        disableButtons = new SimpleBooleanProperty(true);
        menuBar = new MenuBar();

        initializeMenu();

        sequenceScrollPane = new ScrollPane();
        sequenceFlowPane = new FlowPane();
        toolBar = new ToolBar();
        menusVBox = new VBox();
        statusBar = new HBox();
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

        editMenu = new Menu("Edit");
        clearGraphMenuItem = new MenuItem("Clear PDB view");
        runEmbedderMenuItem = new MenuItem("Run Embedder");
        resetRotationMenuItem = new MenuItem("Reset Rotation");

        viewMenu = new Menu("View");
    }

    /**
     * Bind all buttons to the disable property in order to disable them, if Presenter tells them to.
     */
    private void bindButtonsToDisableProperty() {
        editMenu.disableProperty().bind(disableButtons);

        progressBar.visibleProperty().setValue(false);
    }

    /**
     * Set the menu bar's elements and their texts.
     */
    private void setMenus() {
        fileMenu.getItems().addAll(loadFileMenuItem, open1EY4MenuItem, open2KL8MenuItem, open2TGAMenuItem);
        editMenu.getItems().addAll(clearGraphMenuItem, runEmbedderMenuItem, resetRotationMenuItem);
        menuBar.getMenus().addAll(fileMenu, editMenu);
    }

    /**
     * Set up the FileChooser for the input TGF file.
     */
    private void setUpInputFileChooser() {
        tgfFileChooser = new FileChooser();
        tgfFileChooser.setTitle("Choose a PDB formatted file...");
    }

    /**
     * Set up the node graph of the view.
     */
    private void setSceneGraphTree() {
        statusBar.getChildren().addAll(numberOfEdgesLabel, new Separator(Orientation.VERTICAL), numberOfNodesLabel,
                new Separator(Orientation.VERTICAL), status, new Separator(Orientation.VERTICAL), progressBar);
        // Overlay 3D and 2D views of nodes.
        stack2D3DPane.getChildren().addAll(bottomPane, topPane);
        // Set the menu bar to be used in OS provided menu
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac"))
            menuBar.useSystemMenuBarProperty().set(false); // TODO set to true (maybe)
        // Make the pane showing the sequence
        sequenceScrollPane.setContent(sequenceFlowPane);
        // Provide multiple toolbars vertically at the top of the containing border pane
        menusVBox.getChildren().addAll(menuBar, toolBar);
        this.setTop(menusVBox);
        VBox contentVBOX = new VBox(5);
        contentVBOX.getChildren().addAll(sequenceScrollPane, graphTabPane);
        this.setCenter(contentVBOX);
        VBox bottomVBox = new VBox(new Separator(Orientation.HORIZONTAL), statusBar);
        bottomVBox.setSpacing(5);
        bottomVBox.setMargin(statusBar, new Insets(0, 5, 5, 5));
        this.setBottom(bottomVBox);
        //this.addColumn(0, menuBar, graphTabPane, statusBar);
        graphTabPane.getTabs().addAll(graphTab, tableTab);
        graphTab.setContent(stack2D3DPane);
    }

    /**
     * Set the view's style with necessary insets, widths and heights.
     */
    private void setStyle() {
        status.setMinWidth(100);
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

//        sequenceFlowPane.maxWidthProperty().bind(sequenceScrollPane.maxWidthProperty());
//        sequenceFlowPane.minWidthProperty().bind(sequenceScrollPane.minWidthProperty());
//        sequenceFlowPane.prefWidthProperty().bind(sequenceScrollPane.prefWidthProperty());

        sequenceScrollPane.setMinWidth(bottomPane.getMinWidth());
        sequenceScrollPane.setFitToWidth(true);
        sequenceScrollPane.setFitToHeight(false);
        sequenceScrollPane.setMinHeight(50);
        sequenceScrollPane.setPrefHeight(100);
        Tooltip.install(sequenceScrollPane, new Tooltip("Hold shift in order to mark multiple residues."));

        sequenceFlowPane.setOrientation(Orientation.HORIZONTAL);
        sequenceFlowPane.setVgap(5);
        sequenceFlowPane.setHgap(1);
        sequenceFlowPane.prefWrapLengthProperty().bind(sequenceFlowPane.widthProperty().subtract(100));
        sequenceFlowPane.setMinHeight(50);

        // Some inset to be used
        Insets insets = new Insets(5, 5, 5, 5);
        // set insets for all necessary nodes in the scene graph
        statusBar.setSpacing(10);
        //setMargin(stack2D3DPane, insets);
        setMargin(statusBar, insets);

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

    VBox addResidueToSequence(String oneLetterAminoAcidName, String oneLetterSecondaryStructureType) {
        Label nameLabel = new Label(oneLetterAminoAcidName);
        nameLabel.setFont(Font.font("Monospaced", 14));
        nameLabel.setBackground(Background.EMPTY);
        Label aaLabel = new Label(oneLetterSecondaryStructureType);
        aaLabel.setFont(Font.font("Monospaced", 14));
        aaLabel.setBackground(Background.EMPTY);
        VBox vbox = new VBox(nameLabel, aaLabel);
        this.sequenceFlowPane.getChildren().add(vbox);
        vbox.setMinWidth(10);

        return vbox;
    }
}
