package main;

import graph.MyGraph;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.Presenter;
import view.View;

/**
 * Main class.
 *
 * @author Patrick Grupp
 */
public class SimpleGraphView3DProgram extends Application {

	@Override
	public void start(Stage primaryStage) {
		// Create the GUI view and a model graph.
		View view = new View();
		MyGraph graph = new MyGraph();
		
		// The presenter handles connecting/updating view and model
		Presenter presenter = new Presenter(view, graph, primaryStage);

		// Set the scene and show it
		Scene scene = presenter.getScene();
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
