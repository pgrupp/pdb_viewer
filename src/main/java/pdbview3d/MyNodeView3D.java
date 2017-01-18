package pdbview3d;

import pdbmodel.Atom;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;

import java.util.Random;

/**
 * Node representation in 2 dimensional view.
 *
 * @author Patrick Grupp
 */
public class MyNodeView3D extends Group {
    private Shape3D shape;
    private Atom modelNodeReference;

    /**
     * Constructs a View representation of a node.
     *
     * @param node The model's node this view object represents.
     */
    MyNodeView3D(Atom node) {
        // Set reference to model instance, in order to identify the node
        this.modelNodeReference = node;

        // Install a tooltip with the nodes text
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(node.textProperty());
        Tooltip.install(this, tooltip);

        // Draw the circular shape which represents a node
        shape = new Sphere(node.radiusProperty().getValue());
        // Get color from model
        Color col = node.chemicalElementProperty().getValue().getColor();
        PhongMaterial material = new PhongMaterial(col);
        material.setSpecularColor(col.brighter());
        shape.setMaterial(material);

        // Add the shape to the scene graph
        this.getChildren().add(shape);

        // Set the position of the node in the two dimensional space. Placing is handled by the view.Presenter, therefore not
        // computed here
        this.translateXProperty().bind(node.xCoordinateProperty());
        this.translateYProperty().bind(node.yCoordinateProperty());
        this.translateZProperty().bind(node.zCoordinateProperty());
    }

    /**
     * Get the referenced model node this view object represents.
     *
     * @return Model's node instance, this view node represents.
     */
    public Atom getModelNodeReference() {
        return modelNodeReference;
    }

    /**
     * Set another color for the node.
     *
     * @param col The color to be set.
     */
    public void setColor(Color col) {
        PhongMaterial mat = new PhongMaterial(col);
        mat.setSpecularColor(col.brighter());
        this.shape.setMaterial(mat);
    }

}
