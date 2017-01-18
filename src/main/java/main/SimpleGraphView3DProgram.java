package main;

import pdbmodel.PDBEntry;
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
		PDBEntry graph = new PDBEntry();
		
		// Set the scene and show it
		Scene scene = new Scene(view);
		primaryStage.setScene(scene);

		// The presenter handles connecting/updating view and model
		new Presenter(view, graph, primaryStage);

		primaryStage.setTitle("PDB Viewer");
		//primaryStage.getIcons().add(new Image(SimpleGraphView3DProgram.class.getResourceAsStream("/images/pdb_viewer_icon.ico")));
        //com.apple.eawt.Application.getApplication().setDockIconImage(new ImageIcon("src/main/resources/images/pdb_viewer_icon.ico").getImage());
		primaryStage.show();
	}

}
