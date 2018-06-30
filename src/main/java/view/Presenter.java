package view;

import blast.BlastService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TabPane;
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
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private final View view;

    /**
     * PDB model to be represented by the view.
     */
    private final PDBEntry pdbModel;

    /**
     * The selection model.
     */
    private final MySelectionModel<Residue> selectionModel;

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
    private final MyGraphView3D world;

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
    private final BlastService blastService;

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
        //Disable the cancel button, but not if BLAST service is running or scheduled
        ObservableValue<Boolean> cancelBlastDisableBinding = Bindings.not(blastService.stateProperty().isEqualTo(Worker.State.RUNNING).or(
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

        view.runBLASTToolBarButton.setOnAction(event -> {
            runBlast();
            view.blastText.textProperty().bind(Bindings.concat(blastService.titleProperty(),
                    "\n", blastService.messageProperty()));
        });

        view.cancelBlastMenuItem.setOnAction(event -> cancelBlast(false));

        view.cancelBlastButton.setOnAction(event -> cancelBlast(false));

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
            //blastService.getException().printStackTrace();
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
     * @param silently If the BLAST service is not running, then show a warning, if this is false.
     */
    private void cancelBlast(boolean silently) {
        if (blastService.isRunning()) {
            blastService.cancel();
        } else if(!silently) {
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

        view.atomViewMenuItem.selectedProperty().addListener(event -> {
            if (view.atomViewMenuItem.isSelected()) {
                view.coloringByElementMenuItem.selectedProperty().setValue(true);
            }
        });

        view.cartoonViewMenuItem.selectedProperty().addListener(event -> {
            if (view.cartoonViewMenuItem.isSelected()) {
                world.cartoonView(false);
                view.topPane.setVisible(false);
                view.showCBetaMenuItem.selectedProperty().setValue(false);
                view.showRibbonMenuItem.selectedProperty().setValue(false);
                view.showAtomsMenuItem.selectedProperty().setValue(true);
                view.showBondsMenuItem.selectedProperty().setValue(true);
                view.coloringByElementMenuItem.selectedProperty().setValue(true);
                view.scaleEdgesSlider.setValue(1);
                view.scaleNodesSlider.setValue(3);
                // Set the color gray and radius 1 for all nodes (smoothing for coils :D)
                for (Atom a : pdbModel.nodesProperty()) {
                    a.colorProperty().setValue(Color.LIGHTGRAY);
                    a.radiusProperty().setValue(1);
                }
                // Hide O atoms
                for (Atom a : pdbModel.getAllOAtoms()) {
                    world.getNodeByModel(a).setVisible(false);
                }
                // Hide C=O bonds
                for (Bond bond : pdbModel.getAllCOBonds()) {
                    world.getEdgeByModel(bond).setVisible(false);
                }
                // For all secondary structures hide the coil structure of all residues contained by the sec struc
                for (SecondaryStructure structure : pdbModel.secondaryStructuresProperty()) {
                    for (Residue r : structure.getResiduesContained()) {
                        // Hide all nodes within a residue which is contained by a secondary structure
                        world.getNodeByModel(r.getCAlphaAtom()).setVisible(false);
                        world.getNodeByModel(r.getCBetaAtom()).setVisible(false);
                        world.getNodeByModel(r.getCAtom()).setVisible(false);
                        world.getNodeByModel(r.getNAtom()).setVisible(false);
                        world.getNodeByModel(r.getOAtom()).setVisible(false);
                        // Hide all edges within a residue which is contained by a secondary structure
                        // When alphaHelix hide the edge to calpha. Beta sheets are shown a little differently therefore do not hide the edges there
                        r.getCAlphaAtom().inEdgesProperty().forEach(edge -> world.getEdgeByModel(edge).setVisible(false));
                        r.getCAlphaAtom().outEdgesProperty().forEach(edge -> world.getEdgeByModel(edge).setVisible(false));
                        r.getCAtom().outEdgesProperty().forEach(edge -> world.getEdgeByModel(edge).setVisible(false));
                    }
                    // Betasheets are shown differently than alpha helices therefore we need to show additional bonds:
                    // for the first residue in the structure show the N-> CA bond and for the last residue
                    // show C-N and N-CA bonds.
                    if (structure.getSecondaryStructureType().equals(SecondaryStructure.StructureType.betasheet)) {
                        structure.getResiduesContained().get(0).getCAlphaAtom().inEdgesProperty().forEach(edge -> {
                            world.getEdgeByModel(edge).setVisible(true);
                            world.getEdgeByModel(edge).getSourceNodeView().setVisible(true);
                        });
                        structure.getResiduesContained().get(structure.getResiduesContained().size() - 1).getCAtom().inEdgesProperty().forEach(edge -> {
                            world.getEdgeByModel(edge).setVisible(true);
                            world.getNodeByModel(edge.getTarget()).setVisible(true);
                        });
                    }
                    // Make the last C-N bond in the structure visible since it connnects the sec structure with a coil
                    List<Bond> lastPeptideBondList =
                            structure.getResiduesContained().get(structure.getResiduesContained().size() - 1).getCAtom().outEdgesProperty().stream().filter(
                                    edge -> edge.getSource().chemicalElementProperty().getValue().equals(Atom.ChemicalElement.C) &&
                                            edge.getTarget().chemicalElementProperty().getValue().equals(Atom.ChemicalElement.N)).collect(Collectors.toList());
                    // Else the secondary structure also ends the protein sequence, so threre is no bond to set visible,
                    // since there is no peptide bond with the N of the next residue in sequence
                    if (lastPeptideBondList.size() == 1)
                        world.getEdgeByModel(lastPeptideBondList.get(0)).setVisible(true);

                }
            } else {
                world.cartoonView(true);
                view.topPane.setVisible(true);
                view.showCBetaMenuItem.selectedProperty().setValue(true);
                for (Atom a : pdbModel.nodesProperty()) {
                    a.colorProperty().setValue(a.chemicalElementProperty().getValue().getColor());
                    a.radiusProperty().setValue(a.chemicalElementProperty().getValue().getRadius());
                }
                // Show O atoms
                for (Atom a : pdbModel.getAllOAtoms()) {
                    world.getNodeByModel(a).setVisible(true);
                }
                // Show C=O bonds
                for (Bond bond : pdbModel.getAllCOBonds()) {
                    world.getEdgeByModel(bond).setVisible(true);
                }

                for (SecondaryStructure structure : pdbModel.secondaryStructuresProperty()) {
                    for (Residue r : structure.getResiduesContained()) {
                        // Hide all nodes within a residue which is contained by a secondary structure
                        world.getNodeByModel(r.getCAlphaAtom()).setVisible(true);
                        world.getNodeByModel(r.getCBetaAtom()).setVisible(true);
                        world.getNodeByModel(r.getCAtom()).setVisible(true);
                        world.getNodeByModel(r.getNAtom()).setVisible(true);
                        world.getNodeByModel(r.getOAtom()).setVisible(true);
                        // Hide all edges within a residue which is contained by a secondary structure
                        r.getCAlphaAtom().inEdgesProperty().forEach(edge -> world.getEdgeByModel(edge).setVisible(true));
                        r.getCAlphaAtom().outEdgesProperty().forEach(edge -> world.getEdgeByModel(edge).setVisible(true));
                        r.getCAtom().outEdgesProperty().forEach(edge -> world.getEdgeByModel(edge).setVisible(true));
                    }
                }
                view.scaleNodesSlider.setValue(1);
                view.scaleEdgesSlider.setValue(1);
            }

        });

        view.showRibbonMenuItem.selectedProperty().addListener(event ->
                world.ribbonView(!view.showRibbonMenuItem.isSelected()));

        view.showBondsMenuItem.selectedProperty().addListener(event ->
                world.hideEdges(!view.showBondsMenuItem.isSelected())
        );

        view.showAtomsMenuItem.selectedProperty().addListener(event -> {
            //show or hide atoms
            world.hideNodes(!view.showAtomsMenuItem.isSelected());
            // Do not shown selecion indication bounding boxes, if atoms are not shown. since that does not make much sense
            view.topPane.setVisible(view.showAtomsMenuItem.isSelected());
        });

        view.showCBetaMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Run through all Calpha -> Cbeta bonds and show or hide them
            pdbModel.getAllCAlphaCBetaBonds().forEach(bond ->
                    world.hideEdge(world.getEdgeByModel(bond), !newValue)
            );
            // Run through all Cbeta atoms and show or hide them
            pdbModel.getAllCBetaAtoms().forEach(node ->
                    world.hideNode(world.getNodeByModel(node), !newValue)
            );
        });

        // Color by chemical element and make edges gray
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

        // Color each residue with its own random color
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

        // Color bonds and atoms by secondary structure
        view.coloringBySecondaryMenuItem.selectedProperty().addListener(event -> {
            if (view.coloringBySecondaryMenuItem.isSelected()) {
                float r,g,b;
                Color col;
                for (Residue residue : pdbModel.residuesProperty()) {
                    if (residue.getSecondaryStructure() != null) {
                        if (residue.getSecondaryStructure().getSecondaryStructureType().equals(SecondaryStructure.StructureType.alphahelix)) {
                            col = Color.RED;
                        } else {
                            col = Color.CORNFLOWERBLUE;
                        }
                    } else {
                        r = randomGenerator.nextFloat();
                        g = randomGenerator.nextFloat();
                        b = randomGenerator.nextFloat();
                        col = new Color(r, g, b, 1.);
                    }

                    residue.getCBetaAtom().colorProperty().setValue(col);
                    residue.getCAlphaAtom().colorProperty().setValue(col);
                    residue.getNAtom().colorProperty().setValue(col);
                    residue.getCAtom().colorProperty().setValue(col);
                    residue.getOAtom().colorProperty().setValue(col);
                    for(Bond bond : pdbModel.getBondsOfResidue(residue)){
                        world.getEdgeByModel(bond).colorProperty().setValue(col);
                    }
                }
            }
        });
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

        pdbModel.secondaryStructuresProperty().addListener((ListChangeListener<SecondaryStructure>) c -> {
            while (c.next()) {
                // Add structure
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(secondaryStructure -> world.addSecondaryStructure(secondaryStructure));
                }
                // Remove structure
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(secondaryStructure -> world.removeSecondaryStructure(secondaryStructure));
                }
            }
        });
    }

    /**
     * Set the relationship of menu options to the graph's state and to buttons. If there are no nodes in the graph, no
     * graph can be saved or reset.
     */
    private void setMenuItemRelations() {
        // Set to true if number of nodes is zero, or an animation is running
        ObservableBooleanValue disableButtons =
                Bindings.equal(0, Bindings.size(pdbModel.nodesProperty())).or(animationRunning);

        // Either show atoms or show bonds or both are active -> true. Else false
        ObservableBooleanValue showAtomsOrBonds =
                Bindings.or(view.showAtomsMenuItem.selectedProperty(), view.showBondsMenuItem.selectedProperty()).not();

        ObservableBooleanValue disableAtomViewControls = view.atomViewMenuItem.selectedProperty().not();

        //Disable everything, when no file was loaded yet.
        view.runBlastButton.disableProperty().bind(disableButtons);
        view.toolBar.disableProperty().bind(disableButtons);
        view.lowerToolBar.disableProperty().bind(disableButtons);
        view.fileMenu.disableProperty().bind(animationRunning);
        view.editMenu.disableProperty().bind(disableButtons);
        view.viewMenu.disableProperty().bind(disableButtons);

        // Can only show/hide CBeta if either bonds or atoms or both are shown
        view.showCBetaMenuItem.disableProperty().bind(Bindings.and(showAtomsOrBonds, Bindings.not(disableAtomViewControls)));
        view.showCBetaToolBarButton.disableProperty().bind(view.showCBetaMenuItem.disableProperty());

        // make the sliders for scaling only available in atom/bond view, since they make no sense for cartoon
        view.scaleEdgesSlider.disableProperty().bind(disableAtomViewControls);
        view.scaleNodesSlider.disableProperty().bind(disableAtomViewControls);
        view.scaleEdgesLabel.disableProperty().bind(disableAtomViewControls);
        view.scaleNodesLabel.disableProperty().bind(disableAtomViewControls);

        view.showRibbonMenuItem.disableProperty().bind(disableAtomViewControls);
        view.showBondsMenuItem.disableProperty().bind(disableAtomViewControls);
        view.showAtomsMenuItem.disableProperty().bind(disableAtomViewControls);
        view.showCBetaMenuItem.disableProperty().bind(disableAtomViewControls);

        view.coloringByElementMenuItem.disableProperty().bind(disableAtomViewControls);
        view.coloringBySecondaryMenuItem.disableProperty().bind(disableAtomViewControls);
        view.coloringByResidueMenuItem.disableProperty().bind(disableAtomViewControls);

        // Bind worlds radius scaling properties to the sliders in the view
        world.bondRadiusScalingProperty().bind(view.scaleEdgesSlider.valueProperty());
        world.atomRadiusScalingProperty().bind(view.scaleNodesSlider.valueProperty());

        view.lowerToolBar.managedProperty().bind(view.lowerToolBar.visibleProperty());
        view.lowerToolBar.visibleProperty().bind(Bindings.not(disableAtomViewControls));

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
        view.coloringByElementMenuItem.selectedProperty().setValue(true);
        view.showAtomsMenuItem.selectedProperty().setValue(true);
        view.showBondsMenuItem.selectedProperty().setValue(true);
        view.showCBetaMenuItem.selectedProperty().setValue(true);
        view.atomViewMenuItem.selectedProperty().setValue(true);
        view.showRibbonMenuItem.selectedProperty().setValue(false);
        MyRibbonView3D.reset();
        view.secondaryStructureContentStackedBarChart.reset();
    }

    /**
     * Set actions for the open and save options in the file menu.
     */
    private void setFileMenuActions() {
        view.loadFileMenuItem.setOnAction((event) -> {
            // If BLAST service is running ask user if it should be aborted for loading a file. If has already run reset it.
            if (abortLoadBecauseOfBlastService()) return;
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
            // If BLAST service is running ask user if it should be aborted for loading a file. If has already run reset it.
            if (abortLoadBecauseOfBlastService()) return;
            // Load file from resources
            BufferedReader pdbFile = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/2tga.pdb")));
            loadNewPDBFile(pdbFile);
        }));

        view.open2KL8MenuItem.setOnAction((event -> {
            // If BLAST service is running ask user if it should be aborted for loading a file. If has already run reset it.
            if (abortLoadBecauseOfBlastService()) return;
            // Load file from resources
            BufferedReader pdbFile = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/2kl8.pdb")));
            loadNewPDBFile(pdbFile);
        }));

        view.open1EY4MenuItem.setOnAction((event -> {
            // If BLAST service is running ask user if it should be aborted for loading a file. If has already run reset it.
            if (abortLoadBecauseOfBlastService()) return;
            // Load file from resources
            BufferedReader pdbFile = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/1ey4.pdb")));
            loadNewPDBFile(pdbFile);
        }));
    }

    /**
     * Is used to check the status of the Blast service before a new file is loaded. If is in any finished state
     * (succeeded, cancelled, etc) then it is reset, if it is in the running state (scheduled, running) then the
     * user is queried on how to proceed.
     *
     * @return True if the Blast service should be aborted, false else.
     */
    private boolean abortLoadBecauseOfBlastService() {
        if (!blastService.isRunning()) {
            // If the service is run a second time, reset its state.
            if (blastService.getState().equals(Worker.State.CANCELLED) ||
                    blastService.getState().equals(Worker.State.FAILED) ||
                    blastService.getState().equals(Worker.State.READY) ||
                    blastService.getState().equals(Worker.State.SUCCEEDED)) {
                blastService.reset();
            }
            return false;
        } else {
            // BLAST is still running. Show a confirmation alert if BLAST should really be aborted
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("BLAST service running");
            alert.setContentText("Blast service is still running.\n" +
                    "Do you really want to cancel and load a new file?");
            Optional<ButtonType> result = alert.showAndWait();

            if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                // Abort BLASTing
                blastService.cancel();
                blastService.reset();
                return false;
            } else {
                // Cancel loading new file and continue blasting
                return true;
            }
        }
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
            resetBLASTResult();
            worldTransformProperty.setValue(new Rotate());
            pdbModel.reset();
            // parse the file and set up the model. The view listens to the model and handles everything else automatically
            PDBParser.parse(pdbModel, inputStreamReader);
            // set the new selection model
            Residue[] residues = new Residue[pdbModel.residuesProperty().size()];
            pdbModel.residuesProperty().toArray(residues);
            selectionModel.setItems(residues);
            // Compute charts
            view.secondaryStructureContentStackedBarChart.initialize(
                    pdbModel.getAlphaHelixContent(),
                    pdbModel.getBetaSheetContent(),
                    pdbModel.getCoilContent(),
                    view.contentTabPane.widthProperty(),
                    view.contentTabPane.heightProperty()
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Clear the BLAST result tab, when settings are reset, due to e.g. a new file being loaded.
     */
    private void resetBLASTResult() {
        cancelBlast(true);
        view.blastText.setText("");
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
                        // This will also set up the ribbon for all residues in the view
                        world.addResidue(residue);
                    });
                } else if (c.wasRemoved()) {
                    // Remove residue from the pane showing the sequence and secondary structure.
                    view.sequenceFlowPane.getChildren().remove(c.getFrom(), c.getTo() + c.getRemovedSize());
                    // This will destroy residues from ribbon view
                    c.getRemoved().forEach(residue -> world.removeResidue(residue));
                }
            }
        });

        // deselect everything if the pane and not a residue was clicked.
        view.sequenceScrollPane.setOnMouseClicked(event -> {
            // only if control/cmd is not pressed
            if (!event.isMetaDown())
                selectionModel.clearSelection();
        });

        // deselect everything if the pane and not a residue was clicked.
//        view.bottomPane.setOnMouseClicked(event -> {
//            // only if control/cmd is not pressed
//            if(!event.isMetaDown())
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
                    try {
                        c.getRemoved().forEach(index ->
                                ((VBox) view.sequenceFlowPane.getChildren().get(index)).setBackground(Background.EMPTY));
                    } catch(IndexOutOfBoundsException e){
                        // This can happen when there are residues marked and the c beta atoms are not shown. Only when
                        // new file is loaded and the list is cleared, this can happen, due to setting a listener on
                        // the showCBeta property
                        // See issue #1. This does not affect the applications functionality, after the list was cleared.
                    }
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
                        // issue #2 fixed not showing bounding box if c beta are hidden
                        view.showCBetaToolBarButton.selectedProperty().addListener(new WeakInvalidationListener(observable -> {
                            bbcb.visibleProperty().setValue(view.showCBetaToolBarButton.isSelected());
                        }));
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
    }

    /**
     * Choose whether to select or unselect a clicked residue, when control/cmd is down.
     * When control/cmd isn't down, the selection is cleared and the element is selected.
     *
     * @param r     The selected residue.
     * @param event The mouse event which triggered the action.
     */
    private void selectInSelectionModel(Residue r, MouseEvent event) {
        if (selectionModel.isSelected(r)) {
            // The clicked residue is already selected
            if (event.isMetaDown()) {
                // if control/cmd is down we want to deselect the clicked item, but not all. So we only unselect the clicked one
                selectionModel.clearSelection(r);
            } else {
                // if control/cmd is not down and the clicked item was already selected, we want to unselect it, if it is the only selected item.
                if (selectionModel.getSelectedItems().size() == 1) {
                    selectionModel.clearSelection();
                } else {
                    // if the clicked item is not the only selected item, we clear the selection and only select the clicked item
                    selectionModel.clearAndSelect(r);
                }
            }
        } else {
            // The clicked residue is not yet selected.
            if (event.isMetaDown()) {
                // If control/cmd is pressed, we allow to select multiple
                selectionModel.select(r);
            } else {
                // if control/cmd is not pressed we clear the selection and select the clicked item
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
