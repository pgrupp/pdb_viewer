package view;

import graph.MyEdge;
import graph.MyGraph;
import graph.MyNode;
import graphview3d.MyEdgeView3D;
import graphview3d.MyGraphView3D;
import graphview3d.MyNodeView3D;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
	private Scene scene;
	/**
	 * View to be set in the scene.
	 */
	private View view;
	/**
	 * Model of the graphModel to be represented by the view.
	 */
	private MyGraph graphModel;
	
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
	private final double PANEDEPTH = 10000;
	
	/**
	 * Generator for random node positions.
	 */
	private Random randomGenerator;
	
	/**
	 * view.View representation of the graph.
	 */
	private MyGraphView3D graphView;
	
	/**
	 * Property indicating if an animation is running. Does not allow to click a button meanwhile.
	 */
	private BooleanProperty animationRunning;
	
	
	/**
	 * Construct view.Presenter
	 *
	 * @param view  The view.View of the MVP implementation.
	 * @param graph The model of the MVP implementation.
	 */
	public Presenter(View view, MyGraph graph, Stage primaryStage) {
		randomGenerator = new Random(5);
		
		// The view, model and stage to be handled by this presenter
		this.view = view;
		this.graphModel = graph;
		this.primaryStage = primaryStage;
		// Set depthBuffer to true, since view is 3D
		this.scene = new Scene(view,PANEWIDTH + 50, PANEHEIGHT + 150,true);
		
		animationRunning = new SimpleBooleanProperty(false);
		
		// initialize the view of the Graph, which in turn initialized the views of edges and nodes
		graphView = new MyGraphView3D(graph, this);
		
		setUpPerspectiveCamera();
		
		setUpModelListeners();
		view.setPaneDimensions(PANEWIDTH, PANEHEIGHT);
		
		setMenuItemRelations();
		setFileMenuActions();
		setGraphMenuActions();
		initializeStatsBindings();
		view.setGraphView(graphView);
	}
	
	/**
	 * Set up the perspective camera showing the scene.
	 */
	private void setUpPerspectiveCamera(){
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
		perspectiveCamera.setFarClip(PANEDEPTH+0.1);
		perspectiveCamera.setTranslateZ(-500);
		this.scene.setCamera(perspectiveCamera);
	}
	
	/**
	 * Get the scene.
	 * @return the scene.
	 */
	public Scene getScene(){
		return this.scene;
	}
	
	
	/**
	 * Set up listeners on the model in order to update the view's representation of it.
	 */
	private void setUpModelListeners() {
		graphModel.edgesProperty().addListener(new ListChangeListener<MyEdge>() {
			@Override
			public void onChanged(Change<? extends MyEdge> c) {
				while (c.next()) {
					// Handle added edges
					if (c.wasAdded()) {
						c.getAddedSubList().forEach((Consumer<MyEdge>) myEdge -> graphView.addEdge(myEdge));
					}
					// Handle removed edges
					if (c.wasRemoved()) {
						c.getRemoved().forEach((Consumer<MyEdge>) myEdge -> graphView.removeEdge(myEdge));
					}
				}
			}
		});
		
		graphModel.nodesProperty().addListener(new ListChangeListener<MyNode>() {
			@Override
			public void onChanged(Change<? extends MyNode> c) {
				while (c.next()) {
					// Add nodes
					if (c.wasAdded()) {
						c.getAddedSubList().forEach((Consumer<MyNode>) myNode -> graphView.addNode(myNode));
						
					}
					// Remove nodes
					if (c.wasRemoved()) {
						c.getRemoved().forEach((Consumer<MyNode>) myNode -> graphView.removeNode(myNode));
					}
				}
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
		view.loadFileMenuItem.disableProperty().bind(animationRunning);
	}
	
	/**
	 * Set actions for clicking on MenuItems in the graphMenu.
	 */
	private void setGraphMenuActions() {
		// Clear graph action
		view.clearGraphMenuItem.setOnAction(event -> {
			animationRunning.setValue(true);
			// reset node connecting cache
			// scale to 0
			ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1), graphView);
			scaleTransition.setToX(0);
			scaleTransition.setToY(0);
			// fade to 0
			FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), graphView);
			fadeTransition.setToValue(0);
			// run in parallel
			ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
			parallelTransition.play();
			// when done reset opacity and scale properties and delete graph's contents (nodes and edges)
			parallelTransition.setOnFinished(finishedEvent -> {
				graphModel.reset();
				graphView.setOpacity(1);
				graphView.setScaleX(1);
				graphView.setScaleY(1);
				animationRunning.setValue(false);
			});
			event.consume();
		});
		
		// Run embedder
		view.runEmbedderMenuItem.setOnAction(event -> {
			// disable all buttons whil animation runs
			animationRunning.setValue(true);
			double[][] initialPositions = new double[graphModel.getNumberOfNodes()][2];
			int[][] edges = new int[graphModel.getNumberOfEdges()][2];
			int id = 0;
			// Maps holding generated IDs for view.View nodes
			Map<Integer, MyNodeView3D> idToNode = new HashMap<>();
			Map<MyNodeView3D, Integer> nodeToId = new HashMap<>();
			// Filling the maps with content
			for (Node n : graphView.getNodeViews()) {
				// filling the coordinates array with data
				MyNodeView3D currentNode = (MyNodeView3D) n;
				idToNode.put(id, currentNode);
				nodeToId.put(currentNode, id);
				initialPositions[id][0] = currentNode.getTranslateX();
				initialPositions[id][1] = currentNode.getTranslateY();
				id++;
			}
			int edgeCounter = 0;
			for (Node n : graphView.getEdgeViews()) {
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
			int xMax = (int) view.nodesPane.getWidth();
			int yMax = (int) view.nodesPane.getHeight();
			// normalize the new coordinates using the given boundaries of the pane
			SpringEmbedder.centerCoordinates(endPositions, 0, xMax, 0, yMax);
			
			Timeline timeline = new Timeline();
			List<KeyValue> valLists = new ArrayList<>();
			for (int i = 0; i < graphModel.getNumberOfNodes(); i++) {
				MyNodeView3D current = idToNode.get(i);
				KeyValue keyValueX = new KeyValue(current.translateXProperty(), endPositions[i][0]);
				KeyValue keyValueY = new KeyValue(current.translateYProperty(), endPositions[i][1]);
				valLists.add(keyValueX);
				valLists.add(keyValueY);
			}
			KeyFrame endKeyFrame = new KeyFrame(Duration.seconds(3), "", null, valLists);
			timeline.getKeyFrames().add(endKeyFrame);
			timeline.play();
			// make buttons clickable again
			timeline.setOnFinished(e -> animationRunning.setValue(false));
			event.consume();
		});
		
		
	}
	
	/**
	 * Set actions for the open and save options in the file menu.
	 */
	private void setFileMenuActions() {
		view.loadFileMenuItem.setOnAction((e) -> {
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
			resizeTransition.play();
			event.consume();
		});
		
		// Handle mouse event, with mouse exiting the shape/ a node
		node.setOnMouseExited(event -> {
			ScaleTransition resizeTransition = new ScaleTransition(Duration.millis(200), node);
			resizeTransition.setToX(1);
			resizeTransition.setToY(1);
			resizeTransition.play();
			event.consume();
		});
		
	}
	
	/**
	 * Determines if nodes should be placed randomly and returns the appropriate X coordinate for a new node.
	 *
	 * @return X coordinate to be set for a new node.
	 */
	public double getXPosition() {
		return randomGenerator.nextDouble() * PANEWIDTH;
	}
	
	/**
	 * Determines if nodes should be placed randomly and returns the appropriate Y coordinate for a new node.
	 *
	 * @return Y coordinate to be set for a new node.
	 */
	public double getYPosition() {
		return randomGenerator.nextDouble() * PANEHEIGHT;
		
	}
	
	/**
	 * Determines if nodes should be placed randomly and returns the appropriate Z coordinate for a new node.
	 *
	 * @return Z coordinate to be set for a new node.
	 */
	public double getZPosition() {
		return randomGenerator.nextDouble() * PANEDEPTH + 0.1;
		
	}
}
