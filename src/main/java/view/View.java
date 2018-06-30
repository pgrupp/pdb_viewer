package view;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import pdbview3d.MyStackedBarChart;

/**
 * View handling the GUI. This is the topPane itself.
 */
public class View extends BorderPane {

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
    Menu editMenu;

    /**
     * MenuItem to clear the graph.
     */
    MenuItem clearGraphMenuItem;

    /**
     * MenuItem to run the BLAST service on the currently loaded sequence.
     */
    MenuItem runBlastMenuItem;
    MenuItem cancelBlastMenuItem;

    /**
     * MenuItem to reset the rotation Transformations of the graph.
     */
    MenuItem resetRotationMenuItem;

    Menu viewMenu;

    RadioMenuItem atomViewMenuItem;
    RadioMenuItem cartoonViewMenuItem;

    CheckMenuItem showRibbonMenuItem;
    CheckMenuItem showAtomsMenuItem;
    CheckMenuItem showBondsMenuItem;
    CheckMenuItem showCBetaMenuItem;

    RadioMenuItem coloringByElementMenuItem;
    RadioMenuItem coloringByResidueMenuItem;
    RadioMenuItem coloringBySecondaryMenuItem;
    //RadioMenuItem coloringCustomizedMenuItem;

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
    RadioButton atomViewButton;
    RadioButton cartoonViewButton;
    CheckBox showAtomsToolBarButton;
    CheckBox showBondsToolBarButton;
    CheckBox showCBetaToolBarButton;
    CheckBox showRibbonCheckBox;

    ToolBar lowerToolBar;
    Button runBLASTToolBarButton;
    Slider scaleNodesSlider;
    Slider scaleEdgesSlider;
    Label scaleNodesLabel;
    Label scaleEdgesLabel;


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
     * Tab pane containing the tabs for graph and cartoon view.
     */
    TabPane contentTabPane;

    /**
     * Graph tab.
     */
    Tab graphTab;

    /**
     * Cartoon tab.
     */
    Tab tableTab;

    /**
     * Tab for BLASTing the currently showed sequence.
     */
    Tab blastTab;
    BorderPane blastResult;
    Button runBlastButton;
    Button cancelBlastButton;
    TextArea blastText;


    BorderPane graphTabContent;

    RadioButton coloringByElementRadioButton;
    RadioButton coloringByResidueRadioButton;
    RadioButton coloringBySecondaryRadioButton;
    //RadioButton coloringCustomizedRadioButton;

    /**
     * Stacked Bar chart of content in each of the secondary structures.
     */
    MyStackedBarChart secondaryStructureContentStackedBarChart;
    BorderPane tableBorderPane;


    /**
     * Construct the view.View.
     */
    public View() {
        secondaryStructureContentStackedBarChart = new MyStackedBarChart();

        status = new Label();
        progressBar = new ProgressBar();
        menuBar = new MenuBar();

        initializeMenu();
        initializeButtonBar();

        sequenceScrollPane = new ScrollPane();
        sequenceFlowPane = new FlowPane();
        menusVBox = new VBox();
        statusBar = new HBox();
        numberOfEdgesLabel = new Label();
        numberOfNodesLabel = new Label();

        tableBorderPane = new BorderPane();

        stack2D3DPane = new StackPane();
        bottomPane = new Pane();
        topPane = new Pane();
        // this is in order to make the top pane transparent for mouse events etc. because the top pane should not do
        // anything but show the BoundingBoxes2D -> Therefore no mouse events to be handled, these are passed to the
        // bottomPane of the stackPane
        topPane.setPickOnBounds(false);
        topPane.setMouseTransparent(true);

        contentTabPane = new TabPane();
        graphTab = new Tab("PDB Viewer");
        tableTab = new Tab("Stats");
        blastTab = new Tab("BLAST");

        blastResult = new BorderPane();
        graphTabContent = new BorderPane();
        runBlastButton = new Button("Run BLAST");
        cancelBlastButton = new Button("Cancel BLAST");
        blastText = new TextArea();

        setMenus();
        setUpInputFileChooser();
        setSceneGraphTree();
        setStyle();
        bindButtonsToMenuItems();
    }

    /**
     * Bind buttons disabled, selected, visible and managed properties to the equivalent menu items in order to
     * achieve same behaviour.
     */
    private void bindButtonsToMenuItems() {
        //SELECT
        //bind the view menuitems and buttons
        atomViewMenuItem.selectedProperty().bindBidirectional(atomViewButton.selectedProperty());
        cartoonViewMenuItem.selectedProperty().bindBidirectional(cartoonViewButton.selectedProperty());

        // bind the show(atoms,bonds,cbeta,ribbon) menuitems and buttons
        showAtomsToolBarButton.selectedProperty().bindBidirectional(showAtomsMenuItem.selectedProperty());
        showBondsToolBarButton.selectedProperty().bindBidirectional(showBondsMenuItem.selectedProperty());
        showCBetaToolBarButton.selectedProperty().bindBidirectional(showCBetaMenuItem.selectedProperty());
        showRibbonCheckBox.selectedProperty().bindBidirectional(showRibbonMenuItem.selectedProperty());

        // Bind the menuItems and buttonbar radio buttons for coloring
        coloringByElementRadioButton.selectedProperty().bindBidirectional(coloringByElementMenuItem.selectedProperty());
        coloringByResidueRadioButton.selectedProperty().bindBidirectional(coloringByResidueMenuItem.selectedProperty());
        coloringBySecondaryRadioButton.selectedProperty().bindBidirectional(coloringBySecondaryMenuItem.selectedProperty());

        // DISABLE
        // bind the show(atoms,bonds,cbeta,ribbon) menuitems and buttons
        showAtomsToolBarButton.disableProperty().bind(showAtomsMenuItem.disableProperty());
        showBondsToolBarButton.disableProperty().bind(showBondsMenuItem.disableProperty());
        showCBetaToolBarButton.disableProperty().bind(showCBetaMenuItem.disableProperty());
        showRibbonCheckBox.disableProperty().bind(showRibbonMenuItem.disableProperty());

        // Bind the menuItems and buttonbar radio buttons for coloring
        coloringByElementRadioButton.disableProperty().bind(coloringByElementMenuItem.disableProperty());
        coloringByResidueRadioButton.disableProperty().bind(coloringByResidueMenuItem.disableProperty());
        coloringBySecondaryRadioButton.disableProperty().bind(coloringBySecondaryMenuItem.disableProperty());

        // MANAGED
        // bind the show(atoms,bonds,cbeta,ribbon) menuitems and buttons
        showAtomsToolBarButton.managedProperty().bind(showAtomsToolBarButton.visibleProperty());
        showBondsToolBarButton.managedProperty().bind(showBondsToolBarButton.visibleProperty());
        showCBetaToolBarButton.managedProperty().bind(showCBetaToolBarButton.visibleProperty());
        showRibbonCheckBox.managedProperty().bind(showRibbonCheckBox.visibleProperty());

        // Bind the menuItems and buttonbar radio buttons for coloring
        coloringByElementRadioButton.managedProperty().bind(coloringByElementRadioButton.visibleProperty());
        coloringByResidueRadioButton.managedProperty().bind(coloringByResidueRadioButton.visibleProperty());
        coloringBySecondaryRadioButton.managedProperty().bind(coloringBySecondaryRadioButton.visibleProperty());

        scaleEdgesSlider.managedProperty().bind(scaleEdgesSlider.visibleProperty());
        scaleNodesSlider.managedProperty().bind(scaleNodesSlider.visibleProperty());
        scaleEdgesLabel.managedProperty().bind(scaleEdgesLabel.visibleProperty());
        scaleNodesLabel.managedProperty().bind(scaleNodesLabel.visibleProperty());

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
        runBlastMenuItem = new MenuItem("Run BLAST");
        cancelBlastMenuItem = new MenuItem("Cancel BLAST");
        resetRotationMenuItem = new MenuItem("Reset Rotation");

        viewMenu = new Menu("View");
        // View type sub menu
        ToggleGroup viewSelectionRadioGroup = new ToggleGroup();
        atomViewMenuItem = new RadioMenuItem("Show atom view");
        cartoonViewMenuItem = new RadioMenuItem("Show cartoon view");
        atomViewMenuItem.setToggleGroup(viewSelectionRadioGroup);
        cartoonViewMenuItem.setToggleGroup(viewSelectionRadioGroup);

        // Coloring sub menu
        ToggleGroup coloringGroup = new ToggleGroup();
        coloringByElementMenuItem = new RadioMenuItem("Coloring by chemical element");
        coloringByResidueMenuItem = new RadioMenuItem("Coloring by residue");
        coloringBySecondaryMenuItem = new RadioMenuItem("Coloring by secondary structure");
        //coloringCustomizedMenuItem = new RadioMenuItem("Customized");
        coloringByElementMenuItem.setToggleGroup(coloringGroup);
        coloringByResidueMenuItem.setToggleGroup(coloringGroup);
        coloringBySecondaryMenuItem.setToggleGroup(coloringGroup);
        //coloringCustomizedMenuItem.setToggleGroup(coloringGroup);

        showAtomsMenuItem = new CheckMenuItem("Show atoms");
        showBondsMenuItem = new CheckMenuItem("Show bonds");
        showCBetaMenuItem = new CheckMenuItem("Show C-Betas");
        showRibbonMenuItem = new CheckMenuItem("Show ribbon view");
    }

    /**
     * Initialize the contents of the Button bar.
     */
    private void initializeButtonBar() {
        toolBar = new ToolBar();

        ToggleGroup viewToggleGroup = new ToggleGroup();
        atomViewButton = new RadioButton("Atom View");
        cartoonViewButton = new RadioButton("Cartoon View");
        atomViewButton.setToggleGroup(viewToggleGroup);
        cartoonViewButton.setToggleGroup(viewToggleGroup);

        showRibbonCheckBox = new CheckBox("Ribbon View");

        showAtomsToolBarButton = new CheckBox("Show atoms");
        showBondsToolBarButton = new CheckBox("Show bonds");
        showCBetaToolBarButton = new CheckBox("Show C-Betas");


        // Initialize and set the toggle group for the buttons in the toolbar for selecting coloring
        ToggleGroup coloringToggleGroup = new ToggleGroup();
        coloringByElementRadioButton = new RadioButton("By Element");
        coloringByResidueRadioButton = new RadioButton("By Residue");
        coloringBySecondaryRadioButton = new RadioButton("By secondary structure");
        //coloringCustomizedRadioButton = new RadioButton("Customized");
        coloringByElementRadioButton.setToggleGroup(coloringToggleGroup);
        coloringByResidueRadioButton.setToggleGroup(coloringToggleGroup);
        coloringBySecondaryRadioButton.setToggleGroup(coloringToggleGroup);
        //coloringCustomizedRadioButton.setToggleGroup(coloringToggleGroup);
        runBLASTToolBarButton = new Button("Run BLAST");

        toolBar.getItems().addAll(
                atomViewButton, cartoonViewButton,
                new Separator(Orientation.VERTICAL),
                runBLASTToolBarButton,
                new Separator(Orientation.VERTICAL),
                showRibbonCheckBox, showAtomsToolBarButton, showBondsToolBarButton, showCBetaToolBarButton
        );

        lowerToolBar = new ToolBar();

        scaleNodesSlider = new Slider(0.3, 3., 1.);
        scaleEdgesSlider = new Slider(0.3, 3., 1.);
        scaleNodesLabel = new Label("Scale nodes: ");
        scaleEdgesLabel = new Label("Scale edges");
        scaleEdgesLabel.setLabelFor(scaleEdgesSlider);
        scaleNodesLabel.setLabelFor(scaleNodesLabel);

        lowerToolBar.getItems().addAll(
                scaleNodesLabel, scaleNodesSlider, scaleEdgesLabel, scaleEdgesSlider,
                new Separator(Orientation.VERTICAL),
                new Label("Coloring"), coloringByElementRadioButton, coloringByResidueRadioButton, coloringBySecondaryRadioButton
        );

    }

    /**
     * Set the menu bar's elements and their texts.
     */
    private void setMenus() {
        fileMenu.getItems().addAll(loadFileMenuItem, open1EY4MenuItem, open2KL8MenuItem, open2TGAMenuItem);
        editMenu.getItems().addAll(
                clearGraphMenuItem,
                new Menu("BLAST", null, runBlastMenuItem, cancelBlastMenuItem),
                resetRotationMenuItem
        );
        viewMenu.getItems().addAll(atomViewMenuItem, cartoonViewMenuItem, new SeparatorMenuItem(),
                new Menu("Show elements", null, showRibbonMenuItem, showAtomsMenuItem, showBondsMenuItem, showCBetaMenuItem),
                new Menu("Coloring", null, coloringByElementMenuItem, coloringByResidueMenuItem, coloringBySecondaryMenuItem)//, coloringCustomizedMenuItem)
        );

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);
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
        // Set the children of the status bar at the bottom
        statusBar.getChildren().addAll(numberOfEdgesLabel, new Separator(Orientation.VERTICAL), numberOfNodesLabel,
                new Separator(Orientation.VERTICAL), status, new Separator(Orientation.VERTICAL), progressBar);
        // Overlay 3D and 2D views of nodes.
        stack2D3DPane.getChildren().addAll(bottomPane, topPane);

        // TODO Set the menu bar to be used in OS provided menu
//        final String os = System.getProperty("os.name");
//        if (os != null && os.startsWith("Mac"))
//            menuBar.useSystemMenuBarProperty().set(true);

        // Make the pane show the sequence
        sequenceScrollPane.setContent(sequenceFlowPane);
        // Provide multiple toolbars vertically at the top of the containing border pane
        menusVBox.getChildren().addAll(menuBar, toolBar, lowerToolBar);
        VBox contentVBOX = new VBox(5);
        contentVBOX.getChildren().addAll(sequenceScrollPane, contentTabPane);
        VBox bottomVBox = new VBox(new Separator(Orientation.HORIZONTAL), statusBar);
        bottomVBox.setSpacing(5);
        VBox.setMargin(bottomVBox, new Insets(0, 5, 5, 5));

        //BLAST tab
        blastResult.setTop(new HBox(runBlastButton, cancelBlastButton));
        blastResult.setCenter(blastText);
        blastTab.setContent(blastResult);

        this.setTop(menusVBox);
        this.setCenter(contentVBOX);
        this.setBottom(bottomVBox);
        // this.addColumn(0, menuBar, toolBar,sequenceScrollPane, contentTabPane, new Separator(Orientation.HORIZONTAL), statusBar);
        contentTabPane.getTabs().addAll(graphTab, tableTab, blastTab);

        tableBorderPane.setCenter(secondaryStructureContentStackedBarChart);
        tableTab.setContent(tableBorderPane);

        graphTabContent.setCenter(stack2D3DPane);
        graphTab.setContent(graphTabContent);
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


        topPane.minWidthProperty().bind(stack2D3DPane.minWidthProperty());
        topPane.minHeightProperty().bind(stack2D3DPane.minHeightProperty());

        //stack2D3DPane.setMaxHeight(USE_COMPUTED_SIZE);
        VBox.setVgrow(contentTabPane, Priority.ALWAYS);

        blastText.setEditable(false);
        VBox.setMargin(blastResult.getChildren().get(0), new Insets(5, 5, 5, 5));
        HBox.setMargin(runBlastButton, new Insets(5));
        HBox.setMargin(cancelBlastButton, new Insets(5));

        blastText.minHeightProperty().bind(stack2D3DPane.minHeightProperty());
        blastText.minWidthProperty().bind(stack2D3DPane.minWidthProperty());
        blastText.setFont(Font.font("Monospaced", 14));

        sequenceScrollPane.setMinWidth(bottomPane.getMinWidth());
        sequenceScrollPane.setFitToWidth(true);
        sequenceScrollPane.setFitToHeight(false);
        sequenceScrollPane.setMinHeight(50);
        sequenceScrollPane.setPrefHeight(100);
        Tooltip.install(sequenceScrollPane, new Tooltip("Hold ctrl/cmd in order to mark multiple residues."));

        sequenceFlowPane.setOrientation(Orientation.HORIZONTAL);
        sequenceFlowPane.setVgap(10);
        sequenceFlowPane.setHgap(1);
        sequenceFlowPane.prefWrapLengthProperty().bind(sequenceFlowPane.widthProperty().subtract(100));
        sequenceFlowPane.setMinHeight(50);

        // Some inset to be used
        Insets insets = new Insets(5, 5, 5, 5);
        // set insets for all necessary nodes in the scene graph
        statusBar.setSpacing(10);
        //setMargin(stack2D3DPane, insets);
        setMargin(statusBar, insets);

        graphTabContent.minHeightProperty().bind(contentTabPane.minHeightProperty());
        graphTabContent.minWidthProperty().bind(contentTabPane.minWidthProperty());



        //graphTabContent.getRight().setVisible(false);
        //graphTabContent.getRight().setManaged(false);

        // Set style and restrictions for scaling sliders
        scaleNodesSlider.setMajorTickUnit(1.0);
        scaleNodesSlider.setMinorTickCount(3);
        scaleNodesSlider.setShowTickLabels(true);
        scaleNodesSlider.setShowTickMarks(true);
        scaleNodesSlider.setSnapToTicks(true);

        scaleEdgesSlider.setMajorTickUnit(1.0);
        scaleEdgesSlider.setMinorTickCount(3);
        scaleEdgesSlider.setShowTickLabels(true);
        scaleEdgesSlider.setShowTickMarks(true);
        scaleEdgesSlider.setSnapToTicks(true);

        progressBar.setVisible(false);
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
        contentTabPane.setMinHeight(height);
        contentTabPane.setMinWidth(width);
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
