package graphview3d;

import graph.MyNode;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import java.util.Random;

/**
 * Node representation in 2 dimensional view.
 *
 * @author Patrick Grupp
 */
public class MyNodeView3D extends Group {
	private Shape shape;
	private MyNode modelNodeReference;
	
	/**
	 * Constructs a view.View representation of a node.
	 *
	 * @param node        The model's node this view object represents.
	 * @param xCoordinate The x coordinate where this node should be placed
	 * @param yCoordinate The y coordinate where this node should be placed.
	 */
	MyNodeView3D(MyNode node, double xCoordinate, double yCoordinate) {
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
		shape = new Circle(0, 0, 10, col);
		shape.setStroke(Color.BLACK);
		
		
		// Add the shape to the scene graph
		this.getChildren().add(shape);
		
		// Set the position of the node in the two dimensional space. Placing is handled by the view.Presenter, therefore not
		// computed here
		this.setTranslateX(xCoordinate);
		this.setTranslateY(yCoordinate);
	}
	
	/**
	 * Get the referenced model node this view object represents.
	 *
	 * @return Model's node instance, this view node represents.
	 */
	public MyNode getModelNodeReference() {
		return modelNodeReference;
	}
	
}
