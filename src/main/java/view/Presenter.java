package view;

import graph.GraphException;
import graph.MyEdge;
import graph.MyGraph;
import graph.MyNode;
import graphview.MyEdgeView2D;
import graphview.MyGraphView2D;
import graphview.MyNodeView2D;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
	 * view.View to be set in the scene.
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
	 * Generator for random node positions.
	 */
	private Random randomGenerator;
	
	/**
	 * X coordinate where the mouse was last pressed.
	 */
	private double pressedX;
	/**
	 * Y coordinate where the mouse was last pressed.
	 */
	private double pressedY;
	
	/**
	 * determines whether nodes should be placed randomly, or on the last clicked/pressed position on the
	 * pane/graphModel.
	 */
	private boolean randomNodePositions;
	
	/**
	 * Saves the source of a newly to be created edge. Is set to null, if any other action than shift+click on another
	 * node is performed.
	 */
	private MyNodeView2D edgeSource;
	
	/**
	 * view.View representation of the graph.
	 */
	private MyGraphView2D graphView;
	
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
		edgeSource = null;
		randomNodePositions = false;
		randomGenerator = new Random(5);
		// initial last clicked positions for X and Y coordinate
		pressedX = 0.0;
		pressedY = 0.0;
		// The view, model and stage to be handled by this presenter
		this.view = view;
		this.graphModel = graph;
		this.primaryStage = primaryStage;
		
		animationRunning = new SimpleBooleanProperty(false);
		
		// initialize the view of the Graph, which in turn initialized the views of edges and nodes
		graphView = new MyGraphView2D(graph, this);
		setUpModelListeners();
		view.setPaneDimensions(PANEWIDTH, PANEHEIGHT);
		
		setUpDoubleClickPane(view.nodesPane);
		
		setMenuItemRelations();
		setFileMenuActions();
		setGraphMenuActions();
		initializeStatsBindings();
		view.setGraphView(graphView);
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
			resetSource();
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
			resetSource();
			double[][] initialPositions = new double[graphModel.getNumberOfNodes()][2];
			int[][] edges = new int[graphModel.getNumberOfEdges()][2];
			int id = 0;
			// Maps holding generated IDs for view.View nodes
			Map<Integer, MyNodeView2D> idToNode = new HashMap<>();
			Map<MyNodeView2D, Integer> nodeToId = new HashMap<>();
			// Filling the maps with content
			for (Node n : graphView.getNodeViews()) {
				// filling the coordinates array with data
				MyNodeView2D currentNode = (MyNodeView2D) n;
				idToNode.put(id, currentNode);
				nodeToId.put(currentNode, id);
				initialPositions[id][0] = currentNode.getTranslateX();
				initialPositions[id][1] = currentNode.getTranslateY();
				id++;
			}
			int edgeCounter = 0;
			for (Node n : graphView.getEdgeViews()) {
				// filling the edges array with edge's information
				MyEdgeView2D currentEdge = (MyEdgeView2D) n;
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
			List<KeyValue> valLists = new ArrayList<KeyValue>();
			for (int i = 0; i < graphModel.getNumberOfNodes(); i++) {
				MyNodeView2D current = idToNode.get(i);
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
			resetSource();
			File graphFile = view.tgfFileChooser.showOpenDialog(primaryStage);
			// Throw error
			if (graphFile == null) {
				System.out.println("No file chosen. Model not touched");
				return;
			}
			
			try {
				randomNodePositions = true;
				graphModel.read(graphFile);
				randomNodePositions = false;
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
	 * Set up the node pane, holding all nodes and edges in the view to handle double click events.
	 *
	 * @param pane The node pane in the scene graphModel.
	 */
	private void setUpDoubleClickPane(Pane pane) {
		setUpAllElements(pane);
		
		pane.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				// add a new node to the model, if the pane is double clicked
				graphModel.addNewNode();
			}
			event.consume();
			resetSource();
		});
	}
	
	/**
	 * Set up the view's nodes and edges to be click- and moveable.
	 *
	 * @param node The node to be registered.
	 */
	public void setUpNodeView(MyNodeView2D node) {
		
		setUpAllElements(node);
		
		node.setOnMouseDragged(event -> {
			
			double deltaX = event.getSceneX() - pressedX;
			double deltaY = event.getSceneY() - pressedY;
			// Set new position of node.
			if (node.getTranslateX() + deltaX > getCurrentPaneWidth())
				// Don't allow to go out of the pane on the right
				node.setTranslateX(getCurrentPaneWidth());
			else if (node.getTranslateX() + deltaX < 0)
				// Forbid to gou out of the pane on the left
				node.setTranslateX(0);
			else
				// Dragged inside the pane, set new position
				node.setTranslateX(node.getTranslateX() + deltaX);
			
			if (node.getTranslateY() + deltaY > getCurrentPaneHeight())
				// Forbid to go out of the pane at the bottom
				node.setTranslateY(getCurrentPaneHeight());
			else if (node.getTranslateY() + deltaY < 0)
				// Forbid to go out of the pane at the top
				node.setTranslateY(0);
			else
				// Set the new position inside the pane
				node.setTranslateY(node.getTranslateY() + deltaY);
			// Set the variables new.
			pressedX = event.getSceneX();
			pressedY = event.getSceneY();
			
			event.consume();
			// Reset source node, if the adding of an edge was previously initiated
			resetSource();
		});
		
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
		
		node.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY) && !event.isShiftDown()) {
				// Delete node on double click
				graphModel.removeNode(node.getModelNodeReference());
			}
			else if (event.getButton().equals(MouseButton.PRIMARY) && event.isShiftDown()) {
				if (edgeSource == null) {
					// Save source node of potential new edge
					edgeSource = (MyNodeView2D) event.getSource();
				}
				else {
					try {
						// Try to create new edge, since a source has already been saved.
						MyNodeView2D target = (MyNodeView2D) event.getSource();
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
		
		// EventHandler for a resize making the window smaller. Shifts all nodes up, if resize hits them
		view.nodesPane.heightProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.doubleValue() < node.getTranslateY())
				node.setTranslateY(newValue.doubleValue());
			
		});
		
		// EventHandler for a resize making the window smaller. Shifts all nodes to the left, if resize hits them
		view.nodesPane.widthProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.doubleValue() < node.getTranslateX())
				node.setTranslateX(newValue.doubleValue());
			
		});
	}
	
	
	/**
	 * Set up the mouse event handler for edges in the view.
	 *
	 * @param edge The edge to be set up.
	 */
	public void setUpEdgeView(MyEdgeView2D edge) {
		setUpAllElements(edge);
		
		edge.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY)) {
				//Delete edge on double click
				graphModel.deleteEdge(edge.getModelEdgeReference());
			}
			if(event.getClickCount() == 1 && event.getButton().equals(MouseButton.PRIMARY)){
				animationRunning.setValue(true);
				Circle circ = new Circle(0,0,4, Color.YELLOW);
				circ.strokeProperty().setValue(Color.BLACK);
				// add circle to scene graph
				view.nodesPane.getChildren().add(circ);
				
				// Set initial position of the circle
				circ.translateXProperty().setValue(edge.getSourceNodeView().getTranslateX());
				circ.translateYProperty().setValue(edge.getSourceNodeView().getTranslateY());
				
				PathTransition path = new PathTransition(Duration.millis(400), edge.getLine(), circ);
				// Remove circle from scene graph when done
				path.setOnFinished(e -> {
					view.nodesPane.getChildren().remove(circ);
					animationRunning.setValue(false);
				});
				path.setCycleCount(20);
				path.play();
				
			}
			resetSource();
			event.consume();
		});
		
		
		
	}
	
	/**
	 * Set up the event handler for all nodes in the scene graphModel, when the mouse is pressed.
	 *
	 * @param node Node of the scene graphModel to be set up.
	 */
	private void setUpAllElements(Node node) {
		// When clicked, set the current coordinates.
		node.setOnMousePressed(event -> {
			//For all elements set the coordinates, where the mouse button was pressed (for dragging nodes)
			pressedX = event.getSceneX();
			pressedY = event.getSceneY();
			
			System.out.println("Mouse pressed on " + node + " at (" + pressedX + ", " + pressedY + ").");
			
			event.consume();
		});
	}
	
	/**
	 * Determines if nodes should be placed randomly and returns the appropriate X coordinate for a new node.
	 *
	 * @return X coordinate to be set for a new node.
	 */
	public double getXPosition() {
		if (randomNodePositions) {
			return randomGenerator.nextDouble() * getCurrentPaneWidth();
		}
		else
			// -5 in order to place the circle center at the clicked position
			return pressedX - 5;
		
	}
	
	/**
	 * Determines if nodes should be placed randomly and returns the appropriate Y coordinate for a new node.
	 *
	 * @return Y coordinate to be set for a new node.
	 */
	public double getYPosition() {
		if (randomNodePositions) {
			return randomGenerator.nextDouble() * getCurrentPaneHeight();
		}
		else
			// -5 in order to place the circle center at the clicked position
			// additionally normalize the menubar's height
			return pressedY - 5 - view.menuBar.getHeight();
	}
	
	/**
	 * Resets the source node variable which is used to save a  node clicked on in order to create edges. Called when an
	 * action is performed, which is not shift+click on a node.
	 */
	private void resetSource() {
		edgeSource = null;
	}
	
	/**
	 * Get the current width of the node-containing pane in the view.
	 *
	 * @return Width of the nodes pane.
	 */
	private double getCurrentPaneWidth() {
		return view.nodesPane.getWidth();
	}
	
	/**
	 * Get the current height of the node-containing pane in the view.
	 *
	 * @return Height of the nodes pane.
	 */
	private double getCurrentPaneHeight() {
		return view.nodesPane.getHeight();
	}
}
