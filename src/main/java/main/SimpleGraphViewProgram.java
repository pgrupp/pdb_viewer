package main;

import graph.MyGraph;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class.
 *
 * @author Patrick Grupp
 */
public class SimpleGraphViewProgram extends Application {

	@Override
	public void start(Stage primaryStage) {
		// Create the GUI view and a model graph.
		View view = new View();
		MyGraph graph = new MyGraph();
		
		// The presenter handles connecting/updating view and model
		new Presenter(view, graph, primaryStage);

		// Set the scene and show it
		Scene scene = new Scene(view);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
