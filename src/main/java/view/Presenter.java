package view;

import blast.BlastService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import pdbmodel.*;
import pdbview3d.*;

import java.io.*;
import java.util.Random;
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
     * The selection model.
     */
    private MySelectionModel<Residue> selectionModel;

    /**
     * Width of the graph model pane, which contains the nodes and edges.
     */
    private final double PANEWIDTH = 600;

    /**
     * Height of the graph model pane, which contains the nodes and edges.
     */
    private final double PANEHEIGHT = 600;

    /**
     * Depth of the graph model pane, which contains the nodes and edges. This is the depth of the perspective camera's
     * near and far clip's distance.
     */
    private final double PANEDEPTH = 5000;

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
     * The Blast service, handling querying the current sequence to BLAST.
     */
    private BlastService blastService;

    /**
     * Rotation of the graph on y axis.
     */
    private final Property<Transform> worldTransformProperty = new SimpleObjectProperty<>(new Rotate());

    private Random randomGenerator;


    /**
     * Construct view.Presenter
     *
     * @param view  The view.View of the MVP implementation.
     * @param graph The model of the MVP implementation.
     */
    public Presenter(View view, PDBEntry graph, Stage primaryStage) {
        this.selectionModel = new MySelectionModel<>();
        this.blastService = new BlastService();
        // initial last clicked positions for X and Y coordinate
        pressedX = 0.0;
        pressedY = 0.0;

        // The view, model and stage to be handled by this presenter
        this.view = view;
        this.pdbModel = graph;
        this.primaryStage = primaryStage;
        view.setPaneDimensions(PANEWIDTH, PANEHEIGHT);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(800);

        randomGenerator = new Random(15);

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
        setEditMenuActions();
        setViewMenuActions();
        initializeStatsBindings();
        setUpMouseEventListeners();
        setUpSequencePaneAndSelectionModel();
        view.set3DGraphScene(this.subScene3d);
        setUpTabPane();
        setUpBlastService();
    }

    private void setUpBlastService() {
        //Disable the cancel button, but not if BLAST service is running
        Binding cancelBlastDisableBinding = Bindings.not(blastService.stateProperty().isEqualTo(Worker.State.RUNNING).or(
                blastService.stateProperty().isEqualTo(Worker.State.SCHEDULED)));
        view.cancelBlastButton.disableProperty().bind(cancelBlastDisableBinding);
        view.cancelBlastMenuItem.disableProperty().bind(cancelBlastDisableBinding);

        // Action for Run BLAST Button in the BLAST tab
        view.runBlastButton.setOnAction((ActionEvent event) -> {
            runBlast();
            view.blastText.textProperty().bind(Bindings.concat(blastService.titleProperty(),
                    "\n", blastService.messageProperty()));
        });

        // Action for the Run Blast menu item
        view.runBlastMenuItem.setOnAction(event -> {
            runBlast();
            view.blastText.textProperty().bind(Bindings.concat(blastService.titleProperty(),
                    "\n", blastService.messageProperty()));
        });

        view.cancelBlastMenuItem.setOnAction(event -> cancelBlast());

        view.cancelBlastButton.setOnAction(event -> cancelBlast());

        // When BLAST was cancelled
        blastService.setOnCancelled(event -> {
            view.progressBar.progressProperty().unbind();
            view.progressBar.setVisible(false);
            view.status.textProperty().unbind();
            view.blastText.textProperty().unbind();
            view.status.setText("BLASTing was cancelled.");
        });

        // When BLAST failed
        blastService.setOnFailed(event -> {
            // remove binding
            view.status.textProperty().unbind();

            view.progressBar.progressProperty().unbind();
            view.progressBar.setVisible(false);

            view.blastText.textProperty().unbind();
            blastService.getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "BLAST service failed: " + blastService.getException().getMessage(), ButtonType.OK);
            alert.show();
        });

        // When BLAST service changes to Running bind the progressbar to the progress and make it visible.
        blastService.setOnRunning(event -> {
            // Bind the status to the blast title
            view.status.textProperty().bind(blastService.titleProperty());
            // Bind the progress bar to the service's progress
            view.progressBar.progressProperty().bind(blastService.progressProperty());
            view.progressBar.setVisible(true);
            view.blastText.textProperty().bind(blastService.messageProperty());
        });

        // When BLAST succeeded
        blastService.setOnSucceeded(event -> {
            view.status.textProperty().unbind();
            view.blastText.textProperty().unbind();
            view.progressBar.progressProperty().unbind();
            view.progressBar.setVisible(false);
            // Show an alert, if the BLAST tab is currently not being viewed
            if (!view.contentTabPane.getSelectionModel().getSelectedItem().equals(view.blastTab)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "BLAST service finished searching for the given sequence. View the alignments in the 'BLAST' tab", ButtonType.OK);
                alert.show();
            }
            view.status.setText("BLAST service succeded.");
            // Set the result temporarily permanent (until next BLAST is run)
            view.blastText.setText(blastService.getValue());
        });
    }

    /**
     * Allows to cancel the BLAST service, if it is running. Otherwise it shows a message that the service is not
     * running. But one should never be able to call this, when the BLAST service is not running.
     */
    private void cancelBlast() {
        if (blastService.isRunning()) {
            blastService.cancel();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Cannot cancel the BLAST service, since it is not running.", ButtonType.OK);
            alert.show();
        }
    }

    /**
     * Triger BLASTing the given seqeunce.
     */
    private void runBlast() {
        if (pdbModel.getNumberOfResidues() > 0) {
            String toBlastSequence = pdbModel.getSequence();
            blastService.setSequence(toBlastSequence);
            if (!blastService.isRunning()) {
                // If the service is run a second time, reset its state.
                if (blastService.getState().equals(Worker.State.CANCELLED) ||
                        blastService.getState().equals(Worker.State.FAILED) ||
                        blastService.getState().equals(Worker.State.READY) ||
                        blastService.getState().equals(Worker.State.SUCCEEDED)) {
                    blastService.reset();
                }
                blastService.start();
            } else {
                ChoiceDialog<String> choiceDialogBlastRunning = new ChoiceDialog<>("Continue", "Restart");
                choiceDialogBlastRunning.setContentText("The BLAST service is still running.\nDo you want to abort " +
                        "it and start another query?\nThis is not recommended.");
                choiceDialogBlastRunning.showAndWait();
                String choice = choiceDialogBlastRunning.getSelectedItem();
                if (choice.equals("Restart")) {
                    view.status.textProperty().unbind();
                    view.status.setText("Restarted BLAST service. Now running.");
                    blastService.restart();
                }
            }
        } else {
            System.err.println("Cannot run BLAST, when no model is loaded. Aborting");
            view.status.textProperty().setValue("BLASTing not possible, load a PDB file first.");
        }
    }

    /**
     * Set up actions to change the view from or to atom/bond, ribbon and cartoon view. Default to atom/bond view.
     */
    private void setViewMenuActions() {
        // TODO complete those

        view.atomViewMenuItem.selectedProperty().addListener(event -> {
            if (view.atomViewMenuItem.isSelected()) {
                // This adds the nodes and edges for the atom/bond view to the scenegraph
                disableAtomBondView(false);
                world.ribbonView(true);

            }


        });

        view.ribbonViewMenuItem.selectedProperty().addListener(event -> {
            if (view.ribbonViewMenuItem.isSelected()) {
                // This removes the nodes and edges for the atom/bond view from the scenegraph
                disableAtomBondView(true);
                world.ribbonView(false);
            }

        });

        view.cartoonViewMenuItem.selectedProperty().addListener(event -> {
            if (view.cartoonViewMenuItem.isSelected()) {
                // This removes the nodes and edges for the atom/bond view from the scenegraph
                disableAtomBondView(true);
                world.ribbonView(true);
            }

        });

        view.showBondsMenuItem.selectedProperty().addListener(event ->
                world.hideEdges(!view.showBondsMenuItem.isSelected())
        );

        view.showAtomsMenuItem.selectedProperty().addListener(event -> {
            world.hideNodes(!view.showAtomsMenuItem.isSelected());
            view.topPane.setVisible(view.showAtomsMenuItem.isSelected());
        });

        view.showCBetaMenuItem.selectedProperty().addListener(event -> {
            pdbModel.getAllCAlphaCBetaBonds().forEach(bond ->
                    world.hideEdge(world.getEdgeByModel(bond), !view.showCBetaMenuItem.isSelected())
            );
            pdbModel.getAllCBetaAtoms().forEach(node ->
                    world.hideNode(world.getNodeByModel(node), !view.showCBetaMenuItem.isSelected())
            );
        });

        view.coloringByElementRadioButton.selectedProperty().addListener(event -> {
            if (view.coloringByElementRadioButton.isSelected()) {
                for (Atom a : pdbModel.nodesProperty()) {
                    a.colorProperty().setValue(a.chemicalElementProperty().getValue().getColor());
                }
                for (Node edge : world.getEdgeViews()) {
                    ((MyEdgeView3D) edge).colorProperty().setValue(Color.LIGHTGRAY);
                }
            }
        });

        view.coloringByResidueMenuItem.selectedProperty().addListener(event -> {
            if (view.coloringByResidueMenuItem.isSelected()) {
                for (Residue residue : pdbModel.residuesProperty()) {
                    float r = randomGenerator.nextFloat();
                    float g = randomGenerator.nextFloat();
                    float b = randomGenerator.nextFloat();
                    Color col = new Color(r, g, b, 1.);
                    residue.getCBetaAtom().colorProperty().setValue(col);
                    residue.getCAlphaAtom().colorProperty().setValue(col);
                    residue.getNAtom().colorProperty().setValue(col);
                    residue.getCAtom().colorProperty().setValue(col);
                    residue.getOAtom().colorProperty().setValue(col);
                    pdbModel.getBondsOfResidue(residue).forEach(bond -> world.getEdgeByModel(bond).colorProperty().setValue(col));
                }
            }
        });

        view.coloringBySecondaryMenuItem.selectedProperty().addListener(event -> {
            if (view.coloringBySecondaryMenuItem.isSelected()) {
                SecondaryStructure current = null;
                float r = randomGenerator.nextFloat();
                float g = randomGenerator.nextFloat();
                float b = randomGenerator.nextFloat();
                for (Residue residue : pdbModel.residuesProperty()) {
                    if (residue.getSecondaryStructure() == null || current == null) {
                        r = randomGenerator.nextFloat();
                        g = randomGenerator.nextFloat();
                        b = randomGenerator.nextFloat();
                    } else if(!residue.getSecondaryStructure().equals(current)){
                        r = randomGenerator.nextFloat();
                        g = randomGenerator.nextFloat();
                        b = randomGenerator.nextFloat();
                    }
                    Color col = new Color(r, g, b, 1.);

                    residue.getCBetaAtom().colorProperty().setValue(col);
                    residue.getCAlphaAtom().colorProperty().setValue(col);
                    residue.getNAtom().colorProperty().setValue(col);
                    residue.getCAtom().colorProperty().setValue(col);
                    residue.getOAtom().colorProperty().setValue(col);
                    pdbModel.getBondsOfResidue(residue).forEach(bond -> world.getEdgeByModel(bond).colorProperty().setValue(col));
                    current = residue.getSecondaryStructure();
                }
            }
        });

//        view.coloringCustomizedMenuItem.selectedProperty().addListener(event -> {
//            if (view.coloringCustomizedMenuItem.isSelected()) {
//
//            }
//        });
    }

    /**
     * Use this on changing to and from atom/bon view.
     *
     * @param disable Set to false when atom/bonds should be shown, else to true.
     */
    private void disableAtomBondView(boolean disable) {
        view.showAtomsMenuItem.setSelected(!disable);
        view.showBondsMenuItem.setSelected(!disable);
        view.showAtomsMenuItem.setDisable(disable);
        view.showBondsMenuItem.setDisable(disable);
        view.showAtomsToolBarButton.setVisible(!disable);
        view.showBondsToolBarButton.setVisible(!disable);
    }

    /**
     * Set up the transformation property for rotating the graph
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
        });
    }

    /**
     * Set the relationship of menu options to the graph's state and to buttons. If there are no nodes in the graph, no
     * graph can be saved or reset.
     */
    private void setMenuItemRelations() {
        // Set to true if number of nodes is zero, or an animation is running
        ObservableValue<? extends Boolean> disableButtons =
                Bindings.equal(0, Bindings.size(pdbModel.nodesProperty())).or(animationRunning);

        view.disableButtons.bind(disableButtons);
        view.runBlastButton.disableProperty().bind(disableButtons);
        view.toolBar.disableProperty().bind(disableButtons);
        view.lowerToolBar.disableProperty().bind(disableButtons);
        view.fileMenu.disableProperty().bind(animationRunning);
        // make the sliders for scaling only available in atom/bond view, since they make no sense for ribbon and cartoon
        view.scaleEdges.visibleProperty().bind(view.atomViewMenuItem.selectedProperty());
        view.scaleNodes.visibleProperty().bind(view.atomViewMenuItem.selectedProperty());
        // Take the sliders out of the layout management of their parent, if they are not visible.
        view.scaleEdges.managedProperty().bind(view.scaleEdges.visibleProperty());
        view.scaleNodes.managedProperty().bind(view.scaleNodes.visibleProperty());

        //bind the view menuitems and buttons
        view.atomViewMenuItem.selectedProperty().bindBidirectional(view.atomViewButton.selectedProperty());
        view.ribbonViewMenuItem.selectedProperty().bindBidirectional(view.ribbonViewButton.selectedProperty());
        view.cartoonViewMenuItem.selectedProperty().bindBidirectional(view.cartoonViewButton.selectedProperty());

        // bind the show(atoms,bonds,cbeta) menuitems and buttons
        view.showAtomsToolBarButton.selectedProperty().bindBidirectional(view.showAtomsMenuItem.selectedProperty());
        view.showBondsToolBarButton.selectedProperty().bindBidirectional(view.showBondsMenuItem.selectedProperty());
        view.showCBetaToolBarButton.selectedProperty().bindBidirectional(view.showCBetaMenuItem.selectedProperty());

        // Bind the menuItems and buttonbar radio buttons for coloring
        view.coloringByElementMenuItem.selectedProperty().bindBidirectional(view.coloringByElementRadioButton.selectedProperty());
        view.coloringByResidueMenuItem.selectedProperty().bindBidirectional(view.coloringByResidueRadioButton.selectedProperty());
        //view.coloringCustomizedMenuItem.selectedProperty().bindBidirectional(view.coloringCustomizedRadioButton.selectedProperty());
        view.coloringBySecondaryMenuItem.selectedProperty().bindBidirectional(view.coloringBySecondaryRadioButton.selectedProperty());

        // Bind worlds radius scaling properties to the sliders in the view
        world.bondRadiusScalingProperty().bind(view.scaleEdges.valueProperty());
        world.atomRadiusScalingProperty().bind(view.scaleNodes.valueProperty());

        //Set initial values
        resetSettings();
    }

    private void setUpTabPane() {
        view.contentTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    }

    /**
     * Set actions for clicking on MenuItems in the edit menu.
     */
    private void setEditMenuActions() {
        // Clear graph action
        view.clearGraphMenuItem.setOnAction(event -> {
            resetSettings();
            animationRunning.setValue(true);
            selectionModel.clearSelection();
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

        view.resetRotationMenuItem.setOnAction(event -> worldTransformProperty.setValue(new Rotate()));

        //Blast service and MenuItems are set up in setUpBlastService()
    }

    /**
     * Reset the view settings when loading a new graph or dismissing the previously loaded one.
     */
    private void resetSettings() {
        // TODO add all settings elements here. This method is called, when the graph is deleted, or a new one is loaded
        view.coloringByElementMenuItem.selectedProperty().setValue(true);
        view.showAtomsMenuItem.selectedProperty().setValue(true);
        view.showBondsMenuItem.selectedProperty().setValue(true);
        view.showCBetaMenuItem.selectedProperty().setValue(true);
        view.atomViewMenuItem.selectedProperty().setValue(true);
        MyRibbonView3D.reset();
    }

    /**
     * Set actions for the open and save options in the file menu.
     */
    private void setFileMenuActions() {
        view.loadFileMenuItem.setOnAction((event) -> {
            view.tgfFileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDB files (.pdb, .PDB)",
                            "*.pdb", "*.PDB")
            );
            File graphFile = view.tgfFileChooser.showOpenDialog(primaryStage);
            try {
                BufferedReader pdbFile = new BufferedReader(new InputStreamReader(new FileInputStream(graphFile)));
                loadNewPDBFile(pdbFile);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } catch (NullPointerException e) {
                System.out.println("No file chosen. Aborted.");
            }
        });

        // Easy loading of all three PDB files
        view.open2TGAMenuItem.setOnAction((event -> {
            BufferedReader pdbFile = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/2tga.pdb")));
            loadNewPDBFile(pdbFile);
        }));

        view.open2KL8MenuItem.setOnAction((event -> {
            BufferedReader pdbFile = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/2kl8.pdb")));
            loadNewPDBFile(pdbFile);
        }));

        view.open1EY4MenuItem.setOnAction((event -> {
            BufferedReader pdbFile = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/1ey4.pdb")));
            loadNewPDBFile(pdbFile);
        }));
    }

    /**
     * Load a new PDB model from the provided file. This replaces already loaded data, but does not destroy
     * listeners on view or presenter, but on single nodes and edges, since previously loaded data are destroyed.
     *
     * @param inputStreamReader The PDB file to be loaded.
     */
    private void loadNewPDBFile(BufferedReader inputStreamReader) {
        // Report error
        if (inputStreamReader == null) {
            System.err.println("No file chosen. Model not touched");
            return;
        }

        try {
            resetSettings();
            worldTransformProperty.setValue(new Rotate());
            pdbModel.reset();
            // parse the file and set up the model. The view listens to the model and handles everything else automatically
            PDBParser.parse(pdbModel, inputStreamReader);
            // set the new selection model
            Residue[] residues = new Residue[pdbModel.residuesProperty().size()];
            pdbModel.residuesProperty().toArray(residues);
            selectionModel.setItems(residues);
        } catch (Exception ex) {
            ex.printStackTrace();
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
                new StatViewerBinding("# atoms: ", Bindings.size(pdbModel.nodesProperty()));
        view.numberOfNodesLabel.textProperty().bind(stringBindingNodes);

        // Create a String binding of the label to the size of the edges list
        StatViewerBinding stringBindingEdges =
                new StatViewerBinding("# bonds: ", Bindings.size(pdbModel.edgesProperty()));
        view.numberOfEdgesLabel.textProperty().bind(stringBindingEdges);
    }


    /**
     * Set up the view's nodes and edges to be click- and moveable.
     *
     * @param node The node to be registered.
     */
    public void setUpNodeView(MyNodeView3D node) {
        node.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                Residue clickedResidue = node.getModelNodeReference().residueProperty().getValue();
                selectInSelectionModel(clickedResidue, event);
            }
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
        });

        // Save the coordinates, in order to support the dragging of the graph (rotation)
        view.bottomPane.setOnMousePressed(event -> {
            pressedX = event.getSceneX();
            pressedY = event.getSceneY();
            event.consume();
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
     * Sets up the sequence pane, holding the whole sequence, which should be clickable and bound to the SelectionModel.
     * Sets up listeners on the selection model, in order to mark selected residues in both the sequence and the
     * atom/bond view graph.
     */
    private void setUpSequencePaneAndSelectionModel() {
        pdbModel.residuesProperty().addListener((ListChangeListener<Residue>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(residue -> {
                        VBox vbox = view.addResidueToSequence(residue.getOneLetterAminoAcidName(), residue.getOneLetterSecondaryStructureType());
                        // Handle clicks on the vbox representing a residue -> selection
                        vbox.setOnMouseClicked(event -> {
                            if (event.getButton().equals(MouseButton.PRIMARY)) {
                                selectInSelectionModel(residue, event);
                            }
                            event.consume();
                        });
                        world.addResidue(residue);
                    });
                } else if (c.wasRemoved()) {
                    view.sequenceFlowPane.getChildren().remove(c.getFrom(), c.getTo() + c.getRemovedSize());
                    c.getRemoved().forEach(residue -> world.removeResidue(residue));
                }
            }
        });

        // deselect everything if the pane and not a residue was clicked.
        view.sequenceScrollPane.setOnMouseClicked(event -> {
            // only if shift is not pressed
            if (!event.isShiftDown())
                selectionModel.clearSelection();
        });

        // deselect everything if the pane and not a residue was clicked.
//        view.bottomPane.setOnMouseClicked(event -> {
//            // only if shift is not pressed
//            if(!event.isShiftDown())
//                selectionModel.clearSelection();
//
//        });

        // Mark selected residues in the sequence pane. This removes and adds markings depending
        // on the selection model's state
        selectionModel.getSelectedIndices().addListener((ListChangeListener<Integer>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(index ->
                            ((VBox) view.sequenceFlowPane.getChildren().get(index)).setBackground(
                                    new Background(new BackgroundFill(Color.CORNFLOWERBLUE, CornerRadii.EMPTY,
                                            new Insets(0))))
                    );
                }
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(index ->
                            ((VBox) view.sequenceFlowPane.getChildren().get(index)).setBackground(Background.EMPTY));
                }
            }
        });

        // Mark selected residues in the graph's atom/bond view. This adds and removes bounding
        // boxes for each residue which is marked
        selectionModel.getSelectedItems().addListener((ListChangeListener<Residue>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(residue -> {
                        // Find the nodes
                        MyNodeView3D calpha = world.getNodeByModel(residue.getCAlphaAtom());
                        MyNodeView3D cbeta = world.getNodeByModel(residue.getCBetaAtom());
                        MyNodeView3D catom = world.getNodeByModel(residue.getCAtom());
                        MyNodeView3D n = world.getNodeByModel(residue.getNAtom());
                        MyNodeView3D o = world.getNodeByModel(residue.getOAtom());

                        // Create a group of bounding boxes for the residue and add it to the topPane
                        Group resGroup = new Group();
                        BoundingBox2D bbca = new BoundingBox2D(view.bottomPane, calpha, worldTransformProperty, subScene3d);
                        BoundingBox2D bbcb = new BoundingBox2D(view.bottomPane, cbeta, worldTransformProperty, subScene3d);
                        BoundingBox2D bbc = new BoundingBox2D(view.bottomPane, catom, worldTransformProperty, subScene3d);
                        BoundingBox2D bbn = new BoundingBox2D(view.bottomPane, n, worldTransformProperty, subScene3d);
                        BoundingBox2D bbo = new BoundingBox2D(view.bottomPane, o, worldTransformProperty, subScene3d);
                        resGroup.getChildren().addAll(bbca, bbcb, bbc, bbn, bbo);

                        view.topPane.getChildren().add(resGroup);
                    });
                }
                if (c.wasRemoved()) {
                    // Remove the correct element (Group of nodes) from the scene graph when unselected.
                    view.topPane.getChildren().remove(c.getFrom(), c.getTo() + c.getRemovedSize());
                }
            }
        });

        // TODO add support for selection model of other view modes
    }

    /**
     * Choose whether to select or unselect a clicked residue, when shift is down.
     * When shift isn't down, the selection is cleared and the element is selected.
     *
     * @param r     The selected residue.
     * @param event The mouse event which triggered the action.
     */
    private void selectInSelectionModel(Residue r, MouseEvent event) {
        if (selectionModel.isSelected(r)) {
            // The clicked residue is already selected
            if (event.isShiftDown()) {
                // if shift is down we want to deselect the clicked item, but not all. So we only unselect the clicked one
                selectionModel.clearSelection(r);
            } else {
                // if shift is not down and the clicked item was already selected, we want to unselect it, if it is the only selected item.
                if (selectionModel.getSelectedItems().size() == 1) {
                    selectionModel.clearSelection();
                } else {
                    // if the clicked item is not the only selected item, we clear the selection and only select the clicked item
                    selectionModel.clearAndSelect(r);
                }
            }
        } else {
            // The clicked residue is not yet selected.
            if (event.isShiftDown()) {
                // If shift is pressed, we allow to select multiple
                selectionModel.select(r);
            } else {
                // if shift is not pressed we clear the selection and select the clicked item
                selectionModel.clearAndSelect(r);
            }
        }
    }

    /**
     * Compute the focus of the world in order to have a pivot for the rotation axis of the world.
     *
     * @return Focus of the world.
     */
    private Point3D computePivot() {
        // Use the local bound for computation of the midpoint
        Bounds b = world.getBoundsInLocal();
        double x = b.getMaxX() - (b.getWidth() / 2);
        double y = b.getMaxY() - (b.getHeight() / 2);
        double z = b.getMaxZ() - (b.getDepth() / 2);
        return new Point3D(x, y, z);
    }
}
