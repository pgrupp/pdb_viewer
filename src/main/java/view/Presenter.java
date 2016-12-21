package view;

import graph.GraphException;
import graph.MyEdge;
import graph.MyGraph;
import graph.MyNode;
import graphview3d.MyEdgeView3D;
import graphview3d.MyGraphView3D;
import graphview3d.MyLine3D;
import graphview3d.MyNodeView3D;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;

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
	private SubScene scene;
	/**
	 * View to be set in the scene.
	 */
	private View view;
	/**
	 * Model of the graphModel to be represented by the view.
	 */
	private MyGraph graphModel;
	
	/**
	 * Saves the source of a newly to be created edge. Is set to null, if any other action than shift+click on another
	 * node is performed.
	 */
	private MyNodeView3D edgeSource;
	
	/**
	 * Width of the graphModel pane, which contains the nodes and edges.
	 */
	private final double PANEWIDTH = 650;
	/**
	 * Height of the graphModel pane, which contains the nodes and edges.
	 */
	private final double PANEHEIGHT = 650;
	
	/**
	 * Depth of the graphModel pane, which contains the nodes and edges. This is the depth of the perspective camera's
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
	public Presenter(View view, MyGraph graph, Stage primaryStage) {
		edgeSource = null;
		randomGenerator = new Random(5);
		// initial last clicked positions for X and Y coordinate
		pressedX = 0.0;
		pressedY = 0.0;
		
		// The view, model and stage to be handled by this presenter
		this.view = view;
		this.graphModel = graph;
		this.primaryStage = primaryStage;
		view.setPaneDimensions(PANEWIDTH, PANEHEIGHT);
		
		animationRunning = new SimpleBooleanProperty(false);
		
		// initialize the view of the Graph, which in turn initialized the views of edges and nodes
		world = new MyGraphView3D(graph, this);
		// Set depthBuffer to true, since view is 3D
		this.scene = new SubScene(world, PANEWIDTH, PANEHEIGHT, true, SceneAntialiasing.BALANCED);
		setUpPerspectiveCamera();
		
		setUpModelListeners();
		setUpTransforms();
		setMenuItemRelations();
		setFileMenuActions();
		setGraphMenuActions();
		initializeStatsBindings();
		setUpMouseEventListeners();
		view.set3DGraphScene(this.scene);
//		DoubleProperty zero = new SimpleDoubleProperty(0);
//		DoubleProperty one = new SimpleDoubleProperty(100);
//		DoubleProperty negone = new SimpleDoubleProperty(-100);
//		sceneGroup.getChildren().add(new MyLine3D(negone, zero, zero, one, zero, zero, Color.BLACK));
//		sceneGroup.getChildren().add(new MyLine3D(zero, negone, zero, zero, one, zero, Color.BLUE));
//		sceneGroup.getChildren().add(new MyLine3D(zero, zero, negone, zero, zero, one, Color.RED));
	}
	
	/**
	 * Set up the transformation properties for rotating the graph
	 */
	private void setUpTransforms() {
		worldTransformProperty.addListener((e, o, n) -> {
			world.getTransforms().setAll(n);
		});
	}
	
	/**
	 * Set up the perspective camera showing the scene.
	 */
	private void setUpPerspectiveCamera() {
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
		perspectiveCamera.setFarClip(PANEDEPTH);
		perspectiveCamera.setTranslateZ(-PANEDEPTH / 2);
		this.scene.setCamera(perspectiveCamera);
	}
	
	/**
	 * Set up listeners on the model in order to update the view's representation of it.
	 */
	private void setUpModelListeners() {
		graphModel.edgesProperty().addListener((ListChangeListener<MyEdge>) c -> {
			while (c.next()) {
				// Handle added edges
				if (c.wasAdded())
					c.getAddedSubList().forEach((Consumer<MyEdge>) myEdge -> world.addEdge(myEdge));
				// Handle removed edges
				if (c.wasRemoved())
					c.getRemoved().forEach((Consumer<MyEdge>) myEdge -> world.removeEdge(myEdge));
			}
		});
		
		graphModel.nodesProperty().addListener((ListChangeListener<MyNode>) c -> {
			while (c.next()) {
				// Add nodes
				if (c.wasAdded())
					c.getAddedSubList().forEach((Consumer<MyNode>) myNode -> world.addNode(myNode));
				// Remove nodes
				if (c.wasRemoved())
					c.getRemoved().forEach((Consumer<MyNode>) myNode -> world.removeNode(myNode));
			}
		});
	}
	
	/**
	 * Set the relationship of graph and file menu options to the graph's state. If there are no nodes in the graph, no
	 * graph can be saved or reset.
	 */
	private void setMenuItemRelations() {
		// Set to true if number of nodes is zero, or an animation is running
		ObservableValue<? extends Boolean> disableButtons =
				Bindings.equal(0, Bindings.size(graphModel.nodesProperty())).or(animationRunning);
		
		view.clearGraphMenuItem.disableProperty().bind(disableButtons);
		view.runEmbedderMenuItem.disableProperty().bind(disableButtons);
		view.saveFileMenuItem.disableProperty().bind(disableButtons);
		view.resetRotationMenuItem.disableProperty().bind(disableButtons);
		view.loadFileMenuItem.disableProperty().bind(animationRunning);
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
				graphModel.reset();
				world.setOpacity(1);
				world.setScaleX(1);
				world.setScaleY(1);
				world.setScaleZ(1);
				animationRunning.setValue(false);
			});
			event.consume();
		});
		
		// Run embedder
		view.runEmbedderMenuItem.setOnAction(event -> {
			resetSource();
			// disable all buttons while animation runs
			animationRunning.setValue(true);
			double[][] initialPositions = new double[graphModel.getNumberOfNodes()][2];
			int[][] edges = new int[graphModel.getNumberOfEdges()][2];
			int id = 0;
			// Maps holding generated IDs for view.View nodes
			Map<Integer, MyNodeView3D> idToNode = new HashMap<>();
			Map<MyNodeView3D, Integer> nodeToId = new HashMap<>();
			// Filling the maps with content
			for (Node n : world.getNodeViews()) {
				// filling the coordinates array with data
				MyNodeView3D currentNode = (MyNodeView3D) n;
				idToNode.put(id, currentNode);
				nodeToId.put(currentNode, id);
				initialPositions[id][0] = currentNode.getTranslateX();
				initialPositions[id][1] = currentNode.getTranslateY();
				id++;
			}
			int edgeCounter = 0;
			for (Node n : world.getEdgeViews()) {
				// filling the edges array with edge's information
				MyEdgeView3D currentEdge = (MyEdgeView3D) n;
				int sourceID = nodeToId.get(currentEdge.getSourceNodeView());
				int targetID = nodeToId.get(currentEdge.getTargetNodeView());
				edges[edgeCounter][0] = sourceID;
				edges[edgeCounter][1] = targetID;
				edgeCounter++;
			}
			// resulting coordinates
			double[][] endPositions =
					SpringEmbedder.computeSpringEmbedding(100, graphModel.getNumberOfNodes(), edges, initialPositions);
			// boundaries which should be used to fit the nodes in the pane
			int xMin = (int) -PANEWIDTH / 2;
			int xMax = (int) PANEWIDTH / 2;
			int yMin = (int) -PANEHEIGHT / 2;
			int yMax = (int) PANEHEIGHT / 2;
			// normalize the new coordinates using the given boundaries of the pane
			SpringEmbedder.centerCoordinates(endPositions, xMin, xMax, yMin, yMax);
			
			Timeline timeline = new Timeline();
			List<KeyValue> valLists = new ArrayList<>();
			for (int i = 0; i < graphModel.getNumberOfNodes(); i++) {
				MyNodeView3D current = idToNode.get(i);
				double yTarget = randomGenerator.nextBoolean() ? randomGenerator.nextDouble() * 100 :
										 -randomGenerator.nextDouble() * 100;
				KeyValue keyValueX = new KeyValue(current.translateXProperty(), endPositions[i][0]);
				KeyValue keyValueY = new KeyValue(current.translateYProperty(), endPositions[i][1]);
				KeyValue keyValueZ = new KeyValue(current.translateZProperty(), yTarget);
				valLists.add(keyValueX);
				valLists.add(keyValueY);
				valLists.add(keyValueZ);
			}
			KeyFrame endKeyFrame = new KeyFrame(Duration.seconds(3), "", null, valLists);
			timeline.getKeyFrames().add(endKeyFrame);
			timeline.play();
			// make buttons clickable again
			timeline.setOnFinished(e -> animationRunning.setValue(false));
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
			File graphFile = view.tgfFileChooser.showOpenDialog(primaryStage);
			// Throw error
			if (graphFile == null) {
				System.out.println("No file chosen. Model not touched");
				return;
			}
			
			try {
				graphModel.read(graphFile);
			} catch (Exception ex) {
				System.err.println(ex.getMessage() + "\nExiting due to input file error.");
				Platform.exit();
			}
		});
		
		view.saveFileMenuItem.setOnAction((e) -> {
			resetSource();
			File outFile = view.tgfFileChooser.showSaveDialog(primaryStage);
			if (outFile == null) {
				System.out.println("No file chosen. Cancelling save.");
				return;
			}
			try {
				// Get output in the PrintStream and buffer it
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				PrintStream outStream = new PrintStream(buffer);
				graphModel.write(outStream);
				// write everything to file
				PrintWriter pw = new PrintWriter(outFile);
				pw.print(new String(buffer.toByteArray(), StandardCharsets.UTF_8));
				// flush and close file
				pw.flush();
				pw.close();
			} catch (FileNotFoundException ex) {
				System.err.println("Could not write to file " + outFile.getPath());
			}
		});
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
				new StatViewerBinding("Number of nodes: ", Bindings.size(graphModel.nodesProperty()));
		view.numberOfNodesLabel.textProperty().bind(stringBindingNodes);
		
		// Create a String binding of the label to the size of the edges list
		StatViewerBinding stringBindingEdges =
				new StatViewerBinding("Number of edges: ", Bindings.size(graphModel.edgesProperty()));
		view.numberOfEdgesLabel.textProperty().bind(stringBindingEdges);
	}
	
	
	/**
	 * Set up the view's nodes and edges to be click- and moveable.
	 *
	 * @param node The node to be registered.
	 */
	public void setUpNodeView(MyNodeView3D node) {
		
		// Handle mouse event, with mouse entering the shape/ a node
		node.setOnMouseEntered(event -> {
			ScaleTransition resizeTransition = new ScaleTransition(Duration.millis(200), node);
			resizeTransition.setToX(3);
			resizeTransition.setToY(3);
			resizeTransition.setToZ(3);
			resizeTransition.play();
			event.consume();
		});
		
		// Handle mouse event, with mouse exiting the shape/ a node
		node.setOnMouseExited(event -> {
			ScaleTransition resizeTransition = new ScaleTransition(Duration.millis(200), node);
			resizeTransition.setToX(1);
			resizeTransition.setToY(1);
			resizeTransition.setToZ(1);
			resizeTransition.play();
			event.consume();
		});
		
		node.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY) && !event.isShiftDown()) {
				// Delete node on double click
				graphModel.removeNode(node.getModelNodeReference());
			}
			else if (event.getButton().equals(MouseButton.PRIMARY) && event.isShiftDown()) {
				if (edgeSource == null) {
					// Save source node of potential new edge
					edgeSource = (MyNodeView3D) event.getSource();
				}
				else {
					try {
						// Try to create new edge, since a source has already been saved.
						MyNodeView3D target = (MyNodeView3D) event.getSource();
						// if source and target is not the same connect the nodes in the model as well
						if (!edgeSource.getModelNodeReference().equals(target.getModelNodeReference()))
							graphModel.connectNodes(edgeSource.getModelNodeReference(), target.getModelNodeReference());
						
						resetSource();
					} catch (GraphException e) {
						System.err.println("Could not connect nodes: " + e.getMessage());
						resetSource();
						event.consume();
					}
				}
			}
			else {
				resetSource();
			}
			event.consume();
		});
	}
	
	/**
	 * Set up mouse events on 3D graph group.
	 */
	private void setUpMouseEventListeners() {
		view.bottomPane.setOnMouseDragged(event -> {
			
			double deltaX = event.getSceneX() - pressedX;
			double deltaY = event.getSceneY() - pressedY;
			
			// Get the perpendicular axis for the dragged point
			Point3D direction = new Point3D(deltaX, deltaY, 0);
			Point3D axis = direction.crossProduct(0, 0, 1);
			double angle = 0.4 * (Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
			
			Rotate rotation = new Rotate(angle, axis);
			
			// Apply the rotation as an additional transform, keeping earlier modifications
			worldTransformProperty.setValue(rotation.createConcatenation(worldTransformProperty.getValue()));
			
			// Set the variables new
			pressedX = event.getSceneX();
			pressedY = event.getSceneY();
			
			event.consume();
			// Reset source node, if the adding of an edge was previously initiated
			
			resetSource();
		});
		
		view.bottomPane.setOnMousePressed(event -> {
			pressedX = event.getSceneX();
			pressedY = event.getSceneY();
		});
	}
	
	/**
	 * Resets the source node variable which is used to save a  node clicked on in order to create edges. Called when an
	 * action is performed, which is not shift+click on a node.
	 */
	private void resetSource() {
		edgeSource = null;
	}
	
	/**
	 * Determines if nodes should be placed randomly and returns the appropriate X coordinate for a new node.
	 *
	 * @return X coordinate to be set for a new node.
	 */
	public double getXPosition() {
		double number = randomGenerator.nextDouble() * PANEWIDTH;
		return randomGenerator.nextBoolean() ? number : -number;
	}
	
	/**
	 * Determines if nodes should be placed randomly and returns the appropriate Y coordinate for a new node.
	 *
	 * @return Y coordinate to be set for a new node.
	 */
	public double getYPosition() {
		double number = randomGenerator.nextDouble() * PANEHEIGHT;
		return randomGenerator.nextBoolean() ? number : -number;
		
	}
	
	/**
	 * Determines if nodes should be placed randomly and returns the appropriate Z coordinate for a new node.
	 *
	 * @return Z coordinate to be set for a new node.
	 */
	public double getZPosition() {
		return randomGenerator.nextDouble() * PANEDEPTH / 2;
	}
}
