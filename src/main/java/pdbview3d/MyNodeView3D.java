package pdbview3d;

import javafx.beans.property.DoubleProperty;
import pdbmodel.Atom;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * Node representation in 2 dimensional view.
 *
 * @author Patrick Grupp
 */
public class MyNodeView3D extends Group {
    private Sphere sphere;
    private Atom modelNodeReference;

    /**
     * Constructs a View representation of a node.
     *
     * @param node          The model's node this view object represents.
     * @param radiusScaling The scaling factor, with which the radius will be scaled.
     */
    MyNodeView3D(Atom node, DoubleProperty radiusScaling) {
        // Set reference to model instance, in order to identify the node
        this.modelNodeReference = node;

        // Install a tooltip with the nodes text
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(node.textProperty());
        Tooltip.install(this, tooltip);

        // Draw the circular sphere which represents a node
        sphere = new Sphere();
        sphere.radiusProperty().bind(node.radiusProperty().multiply(radiusScaling));
        // Get color from model
        Color col = node.chemicalElementProperty().getValue().getColor();
        PhongMaterial material = new PhongMaterial(col);
        material.setSpecularColor(col.brighter());
        sphere.setMaterial(material);

        // Add the sphere to the scene graph
        this.getChildren().add(sphere);

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
        this.sphere.setMaterial(mat);
    }

}
