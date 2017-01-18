package pdbview3d;

import pdbmodel.MyEdge;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * view.View of an edge in 2 dimensional space. NOTE: Always add the two nodes to the model, before adding the connecting edge.
 * @author Patrick Grupp
 */
public class MyEdgeView3D extends Group {
	
	private MyLine3D line;
	private MyEdge modelEdgeReference;
	private MyNodeView3D source;
	private MyNodeView3D target;
	
	/**
	 * Constructor
	 * @param reference Reference to the model edge.
	 * @param source The Source node
	 * @param target The Target node
	 */
	MyEdgeView3D(MyEdge reference, MyNodeView3D source, MyNodeView3D target){
		
		this.modelEdgeReference = reference;
		this.source = source;
		this.target = target;
		
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(modelEdgeReference.textProperty());
		Tooltip.install(this, tooltip);
		// Generate a random color for this edge
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		Color col = new Color(r, g, b, 1.0);
		
		// Bind line start point to source node's start coordinates
		// Bind line to end/target nodes coordinates
		line = new MyLine3D(source.translateXProperty(), source.translateYProperty(), source.translateZProperty(),
								   target.translateXProperty(), target.translateYProperty(), target.translateZProperty(),
								   col);
		
		// Add line to scene graph/ this group
		this.getChildren().add(line);
	}
	
	/**
	 * Get the reference to the model's edge.
	 * @return Model edge represented by this view representation.
	 */
	MyEdge getModelEdgeReference(){
		return modelEdgeReference;
	}
	
	/**
	 * Get the view's source node.
	 * @return view.View source node.
	 */
	public MyNodeView3D getSourceNodeView(){
		return this.source;
	}
	
	/**
	 * Get the view's target node.
	 * @return view.View target ndoe.
	 */
	public MyNodeView3D getTargetNodeView(){
		return this.target;
	}
	
	/**
	 * Get the edge's line shape.
	 * @return The line of the edge.
	 */
	public MyLine3D getLine(){
		return this.line;
	}
}
