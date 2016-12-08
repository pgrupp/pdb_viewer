package graphview;

import graph.MyEdge;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Line;

/**
 * view.View of an edge in 2 dimensional space. NOTE: Always add the two nodes to the model, before adding the connecting edge.
 * @author Patrick Grupp
 */
public class MyEdgeView2D extends Group {
	
	private Line line;
	private MyEdge modelEdgeReference;
	private MyNodeView2D source;
	private MyNodeView2D target;
	
	/**
	 * Constructor
	 * @param reference Reference to the model edge.
	 * @param source The Source node
	 * @param target The Target node
	 */
	MyEdgeView2D(MyEdge reference, MyNodeView2D source, MyNodeView2D target){
		
		this.modelEdgeReference = reference;
		this.source = source;
		this.target = target;
		
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(modelEdgeReference.textProperty());
		Tooltip.install(this, tooltip);
		
		line = new Line(0,0,0,0);
		// Bind line start point to source node's start coordinates
		line.startXProperty().bind(source.translateXProperty());
		line.startYProperty().bind(source.translateYProperty());
		
		// Bind line to end/target nodes coordinates
		line.endXProperty().bind(target.translateXProperty());
		line.endYProperty().bind(target.translateYProperty());
		
		// This dummyline makes clicking the edge easier, without having a way too thick line in the graph. It is the
		// same as the actual line, but thicker and not visible (opacity 0).
		Line dummyLine = new Line(0,0,0,0);
		dummyLine.startXProperty().bind(source.translateXProperty());
		dummyLine.startYProperty().bind(source.translateYProperty());
		dummyLine.endXProperty().bind(target.translateXProperty());
		dummyLine.endYProperty().bind(target.translateYProperty());
		dummyLine.setStrokeWidth(10);
		dummyLine.setOpacity(0);
		
		// Add lines to scene graph/ this group
		this.getChildren().add(line);
		this.getChildren().add(dummyLine);
		
	}
	
	/**
	 * Get the reference to the model's edge.
	 * @return Model edge represented by this view representation.
	 */
	public MyEdge getModelEdgeReference(){
		return modelEdgeReference;
	}
	
	/**
	 * Get the view's source node.
	 * @return view.View source node.
	 */
	public MyNodeView2D getSourceNodeView(){
		return this.source;
	}
	
	/**
	 * Get the view's target node.
	 * @return view.View target ndoe.
	 */
	public MyNodeView2D getTargetNodeView(){
		return this.target;
	}
	
	/**
	 * Get the edge's line shape.
	 * @return The line of the edge.
	 */
	public Line getLine(){
		return this.line;
	}
}
