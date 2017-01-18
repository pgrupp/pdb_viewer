package pdbview3d;

import pdbmodel.MyNode;
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
	private MyNode modelNodeReference;
	
	/**
	 * Constructs a View representation of a node.
	 *
	 * @param node        The model's node this view object represents.
	 * @param xCoordinate The x coordinate where this node should be placed
	 * @param yCoordinate The y coordinate where this node should be placed.
	 * @param zCoordinate The z coordinate where this node should be placed.
	 */
	MyNodeView3D(MyNode node, double xCoordinate, double yCoordinate, double zCoordinate) {
		// Set reference to model instance, in order to identify the node
		this.modelNodeReference = node;
		
		// Get random RGB color
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		Color col = new Color(r, g, b, 1.0);
		
		// Install a tooltip with the nodes text
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(node.textProperty());
		Tooltip.install(this, tooltip);
		
		// Draw the circular shape which represents a node
		shape = new Sphere(19);
		PhongMaterial material = new PhongMaterial(col);
		material.setSpecularColor(col.brighter());
		shape.setMaterial(material);
		
		
		// Add the shape to the scene graph
		this.getChildren().add(shape);
		
		// Set the position of the node in the two dimensional space. Placing is handled by the view.Presenter, therefore not
		// computed here
		this.setTranslateX(xCoordinate);
		this.setTranslateY(yCoordinate);
		this.setTranslateZ(zCoordinate);
	}
	
	/**
	 * Get the referenced model node this view object represents.
	 *
	 * @return Model's node instance, this view node represents.
	 */
	public MyNode getModelNodeReference() {
		return modelNodeReference;
	}
	
	/**
	 * Set another color for the node.
	 * @param col THe color to be set.
	 */
	public void setColor(Color col){
		PhongMaterial mat = new PhongMaterial(col);
		mat.setSpecularColor(col.brighter());
		this.shape.setMaterial(mat);
	}
	
}
