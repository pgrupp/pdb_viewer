package graphview;

import graph.MyEdge;
import graph.MyGraph;
import graph.MyNode;
import javafx.scene.Group;
import javafx.scene.Node;
import view.Presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Graph view representation in 2 dimensional space.
 *
 * @author Patrick Grupp
 */
public class MyGraphView2D extends Group {
	/**
	 * List of view.View's node representation. Can ONLY contain objects of type {@link MyNodeView2D}.
	 */
	private Group nodeViewGroup;
	/**
	 * List of view.View's edge representation. Can ONLY contain objects of type {@link MyEdgeView2D}.
	 */
	private Group edgeViewGroup;
	
	/**
	 * The presenter to be called for queries.
	 */
	private Presenter presenter;
	
	/**
	 * Constructor for the graph representation in the view. The model needs to make sure, that all nodes represented
	 * by its edges are already persisted in the model. otherwise this will produce errors.
	 *
	 * @param graph     The graph model
	 * @param presenter The view presenter
	 */
	public MyGraphView2D(MyGraph graph, Presenter presenter) {
		this.presenter = presenter;
		nodeViewGroup = new Group();
		edgeViewGroup = new Group();
		
		this.getChildren().add(edgeViewGroup);
		this.getChildren().add(nodeViewGroup);
	}
	
	/**
	 * Add a note to the view.
	 *
	 * @param myNode The model node to be added.
	 */
	public void addNode(MyNode myNode) {
		// Create new view node
		MyNodeView2D node = new MyNodeView2D(myNode, presenter.getXPosition(), presenter.getYPosition());
		// Set up the view logic in the presenter for this node.
		presenter.setUpNodeView(node);
		// Add the node to the scene graph
		nodeViewGroup.getChildren().add(node);
		
	}
	
	/**
	 * Remove a node from the view. NOTE: Assumes the edges have already been deleted by the model.
	 *
	 * @param myNode The model node to be removed.
	 */
	public void removeNode(MyNode myNode) {
		// Filter for view's node to be removed through all view nodes.
		List<Node> node = nodeViewGroup.getChildren().stream().filter(p -> {
			MyNodeView2D tmp = (MyNodeView2D) p;
			return tmp.getModelNodeReference().equals(myNode);
		}).collect(Collectors.toList());
		
		// Should only be one node, else there is an error.
		if (node.size() == 1) {
			MyNodeView2D n = (MyNodeView2D) node.get(0);
			nodeViewGroup.getChildren().remove(n);
		}
		else
			System.err.println("Error in node removal, list size is not equal to 1.");
	}
	
	/**
	 * Adds a new edge to the view. NOTE: Both nodes it conects need to exist already in the view model.
	 *
	 * @param myEdge The model's edge to be represented.
	 */
	public void addEdge(MyEdge myEdge) {
		MyNode sourceNode = myEdge.getSource();
		MyNode targetNode = myEdge.getTarget();
		
		//Find the view representation of source and target
		List<Node> source = nodeViewGroup.getChildren().stream().filter(p -> {
			MyNodeView2D curr = (MyNodeView2D) p;
			return curr.getModelNodeReference().equals(sourceNode);
		}).collect(Collectors.toList());
		
		List<Node> target = nodeViewGroup.getChildren().stream().filter(p -> {
			MyNodeView2D curr = (MyNodeView2D) p;
			return curr.getModelNodeReference().equals(targetNode);
		}).collect(Collectors.toList());
		
		
		// source and target nodes found? then add the edge. else print an error
		if (source.size() == 1 && target.size() == 1) {
			// Create new view edge
			MyEdgeView2D tmp = new MyEdgeView2D(myEdge, (MyNodeView2D) source.get(0), (MyNodeView2D) target.get(0));
			// Set up the view logic for this edge in the view.Presenter
			presenter.setUpEdgeView(tmp);
			// Add edge to the scene graph
			edgeViewGroup.getChildren().add(tmp);
		}
		else {
			System.err.println("Source or target node not found, could not create view edge.");
		}
	}
	
	/**
	 * Remove an edge from the view.
	 *
	 * @param myEdge The model's edge to be removed.
	 */
	public void removeEdge(MyEdge myEdge) {
		// Filter all view edges for the one to be removed
		List<Node> temp = edgeViewGroup.getChildren().stream().filter(p -> {
			MyEdgeView2D edge = (MyEdgeView2D) p;
			return edge.getModelEdgeReference().equals(myEdge);
		}).collect(Collectors.toList());
		// Remove the found one -> should only be one
		for (Node e : temp) {
			edgeViewGroup.getChildren().remove(e);
		}
	}
	
	
	/**
	 * Get all node views.
	 * @return All view.View instances representing a node.
	 */
	public List<Node> getNodeViews(){
		ArrayList<Node> ret = new ArrayList<>();
		ret.addAll(nodeViewGroup.getChildren());
		return ret;
	}
	
	/**
	 * Get all edge views.
	 * @return All view instance representing an edge.
	 */
	public List<Node> getEdgeViews(){
		ArrayList<Node> ret = new ArrayList<>();
		ret.addAll(edgeViewGroup.getChildren());
		return ret;
	}
}
