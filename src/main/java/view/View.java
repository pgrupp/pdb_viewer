package view;

import graphview.MyGraphView2D;
import javafx.geometry.Insets;
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
 * view.View handling the GUI.
 */
public class View extends GridPane {
	
	/**
	 * Menu Bar for the program
	 */
	MenuBar menuBar;
	
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
	Pane nodesPane;
	
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
		
		statLabelsVBox = new VBox();
		numberOfEdgesLabel = new Label();
		numberOfNodesLabel = new Label();
		
		nodesPane = new Pane();
		
		setStyle();
		setMenus();
		setUpInputFileChooser();
		setSceneGraphTree();
	}
	
	/**
	 * Set the menu bar's elements and their texts.
	 */
	private void setMenus(){
		fileMenu.getItems().addAll(loadFileMenuItem, saveFileMenuItem);
		graphMenu.getItems().addAll(clearGraphMenuItem, runEmbedderMenuItem);
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
		this.addColumn(0, menuBar, nodesPane, statLabelsVBox);
	}
	
	/**
	 * Set the view's style with necessary insets.
	 */
	private void setStyle() {
		
		// Show a border fo the node-containing pane
		nodesPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
															   BorderWidths.DEFAULT)));
		// Some inset to be used
		Insets insets = new Insets(5, 5, 5, 5);
		// set insets for all necessary nodes in the scene graph
		
		setMargin(nodesPane, insets);
		setMargin(statLabelsVBox, insets);
		setMargin(numberOfEdgesLabel, new Insets(5, 20, 5, 5));
		setMargin(numberOfNodesLabel, new Insets(5, 20, 5, 5));
	}
	
	/**
	 * Set the graph node in the view.
	 *
	 * @param graph The graph view.
	 */
	void setGraphView(MyGraphView2D graph) {
		nodesPane.getChildren().add(graph);
	}
	
	/**
	 * Set the nodePane's height and width.
	 *
	 * @param width  width to be set to.
	 * @param height height to be set to.
	 */
	void setPaneDimensions(double width, double height) {
		// Set the height and width of the pane which will hold the node and edge representation
		nodesPane.setPrefWidth(width);
		nodesPane.setPrefHeight(height);
	}
}
