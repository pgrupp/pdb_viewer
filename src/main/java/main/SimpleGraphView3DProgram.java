package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pdbmodel.PDBEntry;
import view.Presenter;
import view.View;

import javax.swing.ImageIcon;
import java.awt.*;
import java.net.URL;

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
        // Set Application icon
            javafx.scene.image.Image icon = new javafx.scene.image.Image(SimpleGraphView3DProgram.class.getResourceAsStream("/pdb_viewer.png"));
            primaryStage.getIcons().add(icon);
        try {
            URL iconURL = SimpleGraphView3DProgram.class.getResource("/pdb_viewer.png");
            Image ico = new ImageIcon(iconURL).getImage();
            com.apple.eawt.Application.getApplication().setDockIconImage(ico);
        } catch (Exception e) {
            // Mac stuff won't work on Windows or Linux, so just ignore it.
        }
        primaryStage.show();
    }

}
