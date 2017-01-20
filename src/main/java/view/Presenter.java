package view;

import javafx.beans.value.ChangeListener;
import javafx.stage.FileChooser;
import pdbmodel.*;
import pdbview3d.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * view.Presenter
 */
public class Presenter {

    /**
     * The primary stage of the view.
     */
    private Stage primaryStage;

    /**
     * The scene to be set on stage.
     */
    private SubScene subScene3d;

    /**
     * View to be set in the scene.
     */
    private View view;

    /**
     * PDB model to be represented by the view.
     */
    private PDBEntry pdbModel;

    /**
     * Saves the source of a newly to be created edge. Is set to null, if any other action than shift+click on another
     * node is performed. Additionally saves the last clicked node. is set to null if no node was clicked.
     */
    private MyNodeView3D lastClickedNode;

    /**
     * Width of the graph model pane, which contains the nodes and edges.
     */
    private final double PANEWIDTH = 650;

    /**
     * Height of the graph model pane, which contains the nodes and edges.
     */
    private final double PANEHEIGHT = 650;

    /**
     * Depth of the graph model pane, which contains the nodes and edges. This is the depth of the perspective camera's
     * near and far clip's distance.
     */
    private final double PANEDEPTH = 5000;

    /**
     * Generator for random node positions.
     */
    private Random randomGenerator;

    /**
     * view.View representation of the graph.
     */
    private MyGraphView3D world;

    /**
     * Property indicating if an animation is running. Does not allow to click a button meanwhile.
     */
    private BooleanProperty animationRunning;

    /**
     * Mouse last pressed position X in scene.
     */
    private double pressedX;

    /**
     * Mouse last pressed position Y in scene.
     */
    private double pressedY;

    /**
     * Rotation of the graph on y axis.
     */
    private final Property<Transform> worldTransformProperty = new SimpleObjectProperty<>(new Rotate());


    /**
     * Construct view.Presenter
     *
     * @param view  The view.View of the MVP implementation.
     * @param graph The model of the MVP implementation.
     */
    public Presenter(View view, PDBEntry graph, Stage primaryStage) {
        lastClickedNode = null;
        randomGenerator = new Random(5);
        // initial last clicked positions for X and Y coordinate
        pressedX = 0.0;
        pressedY = 0.0;

        // The view, model and stage to be handled by this presenter
        this.view = view;
        this.pdbModel = graph;
        this.primaryStage = primaryStage;
        view.setPaneDimensions(PANEWIDTH, PANEHEIGHT);
        primaryStage.setMinWidth(PANEWIDTH);
        primaryStage.setMinHeight(PANEHEIGHT + 100);

        animationRunning = new SimpleBooleanProperty(false);
        // initialize the view of the Graph, which in turn initialized the views of edges and nodes
        world = new MyGraphView3D(this);
        // Set depthBuffer to true, since view is 3D
        this.subScene3d = new SubScene(world, PANEWIDTH, PANEHEIGHT, true, SceneAntialiasing.BALANCED);
        setUpPerspectiveCamera();

        setUpModelListeners();
        setUpTransforms();
        setMenuItemRelations();
        setFileMenuActions();
        setGraphMenuActions();
        initializeStatsBindings();
        setUpMouseEventListeners();
        view.set3DGraphScene(this.subScene3d);
    }

    /**
     * Set up the transformation properties for rotating the graph
     */
    private void setUpTransforms() {
        worldTransformProperty.addListener((e, o, n) -> world.getTransforms().setAll(n));
    }

    /**
     * Set up the perspective camera showing the subScene3d.
     */
    private void setUpPerspectiveCamera() {
        PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
        perspectiveCamera.setNearClip(0.1);
        perspectiveCamera.setFarClip(PANEDEPTH * 2);
        perspectiveCamera.setTranslateZ(-PANEDEPTH / 2);
        this.subScene3d.setCamera(perspectiveCamera);
    }

    /**
     * Set up listeners on the model in order to update the view's representation of it.
     */
    private void setUpModelListeners() {
        pdbModel.edgesProperty().addListener((ListChangeListener<Bond>) c -> {
            while (c.next()) {
                // Handle added edges
                if (c.wasAdded())
                    c.getAddedSubList().forEach((Consumer<Bond>) myEdge -> world.addEdge(myEdge));
                // Handle removed edges
                if (c.wasRemoved())
                    c.getRemoved().forEach((Consumer<Bond>) myEdge -> world.removeEdge(myEdge));
            }
        });

        pdbModel.nodesProperty().addListener((ListChangeListener<Atom>) c -> {
            while (c.next()) {
                // Add nodes
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach((Consumer<Atom>) myNode -> world.addNode(myNode));
                }
                // Remove nodes
                if (c.wasRemoved())
                    c.getRemoved().forEach((Consumer<Atom>) myNode -> world.removeNode(myNode));
            }
            //TODO view.topPane.getChildren().clear();
            //TODO view.topPane.getChildren().add(new BoundingBox2D(view.bottomPane, world.getNodeViews(), worldTransformProperty, subScene3d));
        });
    }

    /**
     * Set the relationship of graph and file menu options to the graph's state. If there are no nodes in the graph, no
     * graph can be saved or reset.
     */
    private void setMenuItemRelations() {
        // Set to true if number of nodes is zero, or an animation is running
        ObservableValue<? extends Boolean> disableButtons =
                Bindings.equal(0, Bindings.size(pdbModel.nodesProperty())).or(animationRunning);

        disableButtons.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                view.disableProperty().setValue(newValue);
            }
        });
        view.loadFileMenuItem.disableProperty().bind(animationRunning);

        view.graphTab.setClosable(false);
        view.tableTab.setClosable(false);
    }

    /**
     * Set actions for clicking on MenuItems in the graphMenu.
     */
    private void setGraphMenuActions() {
        // Clear graph action
        view.clearGraphMenuItem.setOnAction(event -> {
            resetSource();
            animationRunning.setValue(true);
            // reset node connecting cache
            // scale to 0
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1), world);
            scaleTransition.setToX(0);
            scaleTransition.setToY(0);
            scaleTransition.setToZ(0);
            // fade to 0
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), world);
            fadeTransition.setToValue(0);
            // run in parallel
            ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
            parallelTransition.play();
            // when done reset opacity and scale properties and delete graph's contents (nodes and edges)
            parallelTransition.setOnFinished(finishedEvent -> {
                pdbModel.reset();
                worldTransformProperty.setValue(new Rotate());
                world.setOpacity(1);
                world.setScaleX(1);
                world.setScaleY(1);
                world.setScaleZ(1);
                animationRunning.setValue(false);
            });
            event.consume();
        });

        view.resetRotationMenuItem.setOnAction(event -> {
            worldTransformProperty.setValue(new Rotate());
        });
    }

    /**
     * Set actions for the open and save options in the file menu.
     */
    private void setFileMenuActions() {
        view.loadFileMenuItem.setOnAction((e) -> {
            resetSource();
            view.tgfFileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("ExtensionFilter only allows PDB files.",
                            "*.pdb", "*.PDB")
            );
            File graphFile = view.tgfFileChooser.showOpenDialog(primaryStage);
            loadNewPDBFile(graphFile);
        });

        view.open2TGAMenuItem.setOnAction((event -> {
            File pdbFile = new File(getClass().getClassLoader().getResource("2tga.pdb").getFile());
            loadNewPDBFile(pdbFile);
        }));

        view.open2KL8MenuItem.setOnAction((event -> {
            File pdbFile = new File(getClass().getClassLoader().getResource("2kl8.pdb").getFile());
            loadNewPDBFile(pdbFile);
        }));

        view.open1EY4MenuItem.setOnAction((event -> {
            File pdbFile = new File(getClass().getClassLoader().getResource("1ey4.pdb").getFile());
            loadNewPDBFile(pdbFile);
        }));
    }

    /**
     * Load a new PDB model from the provided file. This replaces already loaded data, but does not destroy
     * listeners on view or presenter, but on single nodes and edges, since previously loaded data are destroyed.
     * @param graphFile The PDB file to be loaded.
     */
    private void loadNewPDBFile(File graphFile){
        // Throw error
        if (graphFile == null) {
            System.err.println("No file chosen. Model not touched");
            return;
        }

        try {
            worldTransformProperty.setValue(new Rotate());
            pdbModel.reset();
            PDBParser.parse(pdbModel, graphFile);
        } catch (Exception ex) {
            System.err.println(ex.getMessage() + "\nExiting due to input file error.");
            Platform.exit();
        }
    }


    /**
     * Class to connect/bind the stats of the model with their value and a proper label.
     */
    public class StatViewerBinding extends StringBinding {
        IntegerBinding p;
        String bindingLabel;

        StatViewerBinding(String bindingLabel, IntegerBinding property) {
            super.bind(property);
            this.p = property;
            this.bindingLabel = bindingLabel;
        }

        @Override
        protected String computeValue() {
            // Return nice String format
            return bindingLabel + p.getValue().toString();
        }
    }

    /**
     * Bind the statistics label to the number of nodes and edges.
     */
    private void initializeStatsBindings() {
        // Create a String binding of the label to the size of the nodes list
        StatViewerBinding stringBindingNodes =
                new StatViewerBinding("Number of nodes: ", Bindings.size(pdbModel.nodesProperty()));
        view.numberOfNodesLabel.textProperty().bind(stringBindingNodes);

        // Create a String binding of the label to the size of the edges list
        StatViewerBinding stringBindingEdges =
                new StatViewerBinding("Number of edges: ", Bindings.size(pdbModel.edgesProperty()));
        view.numberOfEdgesLabel.textProperty().bind(stringBindingEdges);
    }


    /**
     * Set up the view's nodes and edges to be click- and moveable.
     *
     * @param node The node to be registered.
     */
    public void setUpNodeView(MyNodeView3D node) {

        node.setOnMouseClicked(event -> {
            // TODO selection model
            event.consume();
        });
    }

    /**
     * Set up mouse events on 3D graph group.
     */
    private void setUpMouseEventListeners() {
        // This is dragging the graph and rotating it around itself.
        view.bottomPane.setOnMouseDragged(event -> {

            double deltaX = event.getSceneX() - pressedX;
            double deltaY = event.getSceneY() - pressedY;

            // Get the perpendicular axis for the dragged point
            Point3D direction = new Point3D(deltaX, deltaY, 0);
            Point3D axis = direction.crossProduct(0, 0, 1);
            double angle = 0.4 * (Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));

            //compute the main focus of the world and use it as pivot
            Point3D focus = computePivot();

            Rotate rotation = new Rotate(angle, focus.getX(), focus.getY(), focus.getZ(), axis);

            // Apply the rotation as an additional transform, keeping earlier modifications
            worldTransformProperty.setValue(rotation.createConcatenation(worldTransformProperty.getValue()));

            // Set the variables new
            pressedX = event.getSceneX();
            pressedY = event.getSceneY();

            event.consume();
            // Reset source node, if the adding of an edge was previously initiated

            resetSource();
        });

        // Save the coordinates, in order to support the dragging of the graph (rotation)
        view.bottomPane.setOnMousePressed(event -> {
            pressedX = event.getSceneX();
            pressedY = event.getSceneY();
        });


        view.bottomPane.setOnMouseClicked(event -> {
            resetSource();
        });

        // Implement zooming, when scolling with the mouse wheel or on a trackpad
        view.bottomPane.setOnScroll(event -> {
            double delta = 0.01 * event.getDeltaY() + 1;
            Point3D focus = computePivot();
            Scale scale = new Scale(delta, delta, delta, focus.getX(), focus.getY(), focus.getZ());
            worldTransformProperty.setValue(scale.createConcatenation(worldTransformProperty.getValue()));
        });
    }

    /**
     * Compute the focus of the world in order to have a pivot for the rotation axis of the world.
     *
     * @return Focus of the world.
     */
    private Point3D computePivot() {
        Bounds b = world.getBoundsInLocal();
        double x = b.getMaxX() - (b.getWidth() / 2);
        double y = b.getMaxY() - (b.getHeight() / 2);
        double z = b.getMaxZ() - (b.getDepth() / 2);
        return new Point3D(x, y, z);
    }

    /**
     * Resets the source node variable which is used to save a  node clicked on in order to create edges. Called when an
     * action is performed, which is not shift+click on a node.
     */
    private void resetSource() {
        lastClickedNode = null;
    }
}
