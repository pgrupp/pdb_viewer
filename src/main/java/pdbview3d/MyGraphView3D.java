package pdbview3d;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import pdbmodel.Atom;
import pdbmodel.Bond;
import javafx.scene.Group;
import javafx.scene.Node;
import view.Presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Graph view representation in 2 dimensional space.
 *
 * @author Patrick Grupp
 */
public class MyGraphView3D extends Group {
    /**
     * List of view.View's node representation. Can ONLY contain objects of type {@link MyNodeView3D}.
     */
    private Group nodeViewGroup;

    /**
     * List of view.View's edge representation. Can ONLY contain objects of type {@link MyEdgeView3D}.
     */
    private Group edgeViewGroup;

    /**
     * Maps model to view of nodes.
     */
    private Map<Atom, MyNodeView3D> modelToNode;

    /**
     * Maps model to view of edges.
     */
    private Map<Bond, MyEdgeView3D> modelToEdge;

    /**
     * The presenter to be called for queries.
     */
    private Presenter presenter;

    /**
     * Property determining the radius of the bonds.
     */
    private DoubleProperty bondRadiusScaling;

    /**
     * Property determining the scaling factor for the radius of the atoms.
     */
    private DoubleProperty atomRadiusScaling;

    /**
     * Constructor for the graph representation in the view. The model needs to make sure, that all nodes represented
     * by its edges are already persisted in the model. otherwise this will produce errors.
     *
     * @param presenter The view presenter
     */
    public MyGraphView3D(Presenter presenter) {
        this.presenter = presenter;
        modelToNode = new HashMap<>();
        modelToEdge = new HashMap<>();
        nodeViewGroup = new Group();
        edgeViewGroup = new Group();
        this.bondRadiusScaling = new SimpleDoubleProperty(1);
        this.atomRadiusScaling = new SimpleDoubleProperty(1);

        this.getChildren().add(edgeViewGroup);
        this.getChildren().add(nodeViewGroup);
    }

    /**
     * Add a note to the view.
     *
     * @param atom The model node to be added.
     */
    public void addNode(Atom atom) {
        // Create new view node
        MyNodeView3D node = new MyNodeView3D(atom, this.atomRadiusScaling);
        // Set up the view logic in the presenter for this node.
        presenter.setUpNodeView(node);
        // Add the node to the scene graph
        nodeViewGroup.getChildren().add(node);
        // Add to mapping for later use
        modelToNode.put(atom, node);
    }

    /**
     * Remove a node from the view. NOTE: Assumes the edges have already been deleted by the model.
     *
     * @param atom The model node to be removed.
     */
    public void removeNode(Atom atom) {
        // Filter for view's node to be removed through all view nodes.
        if (modelToNode.containsKey(atom)) {

            MyNodeView3D current = modelToNode.get(atom);
            nodeViewGroup.getChildren().remove(current);
            modelToNode.remove(atom);
        } else
            System.err.println("Error in node removal, list size is not equal to 1.");

    }

    /**
     * Adds a new edge to the view. NOTE: Both nodes it conects need to exist already in the view model.
     *
     * @param bond The model's edge to be represented.
     */
    public void addEdge(Bond bond) {
        Atom sourceNode = bond.getSource();
        Atom targetNode = bond.getTarget();

        //Find the view representation of source and target
        MyNodeView3D source = modelToNode.get(sourceNode);
        MyNodeView3D target = modelToNode.get(targetNode);


        // source and target nodes found? then add the edge. else print an error
        if (source != null && target != null) {
            // Create new view edge
            MyEdgeView3D tmp = new MyEdgeView3D(bond, source, target, this.bondRadiusScaling);
            // Add edge to the scene graph
            edgeViewGroup.getChildren().add(tmp);
            modelToEdge.put(bond, tmp);
        } else {
            System.err.println("Source or target node not found, could not create view edge.");
        }
    }

    /**
     * Remove an edge from the view.
     *
     * @param bond The model's edge to be removed.
     */
    public void removeEdge(Bond bond) {
        // Filter all view edges for the one to be removed
        MyEdgeView3D toBeRemoved = modelToEdge.get(bond);
        // Remove the found one -> should only be one
        edgeViewGroup.getChildren().remove(toBeRemoved);
        modelToEdge.remove(bond);
    }

    /**
     * Get all node views.
     *
     * @return All view.View instances representing a node.
     */
    public List<Node> getNodeViews() {
        ArrayList<Node> ret = new ArrayList<>();
        ret.addAll(nodeViewGroup.getChildren());
        return ret;
    }

    /**
     * Get all edge views.
     *
     * @return All view instance representing an edge.
     */
    public List<Node> getEdgeViews() {
        ArrayList<Node> ret = new ArrayList<>();
        ret.addAll(edgeViewGroup.getChildren());
        return ret;
    }

    /**
     * Get the view node by model node.
     *
     * @param atom The model instance.
     * @return The corresponding view node instance.
     */
    public MyNodeView3D getNodeByModel(Atom atom) {
        return modelToNode.get(atom);
    }


    /**
     * Get the view edge by model edge.
     *
     * @param bond The model instance for which the view edge should be returned.
     * @return The view instance corresponding to the given model.
     */
    public MyEdgeView3D getEdgeByModel(Bond bond) {
        return modelToEdge.get(bond);
    }

    /**
     * Hides the edges.
     *
     * @param hide Specifies if to hide, or to show the edges.
     */
    public void hideEdges(boolean hide) {
        edgeViewGroup.setVisible(!hide);
    }

    /**
     * Hides the nodes.
     *
     * @param hide Specifies if to hide, or to show the edges.
     */
    public void hideNodes(boolean hide) {
        nodeViewGroup.setVisible(!hide);
    }

    /**
     * Hide given node.
     *
     * @param node Node to be visible or hidden.
     * @param hide Hide the node if true, else show the node.
     */
    public void hideNode(MyNodeView3D node, boolean hide) {
        node.setVisible(!hide);
    }

    /**
     * Hide given edge.
     *
     * @param edge Edge to be visible or hidden.
     * @param hide Hide the given edge if true, else show the edge.
     */
    public void hideEdge(MyEdgeView3D edge, boolean hide) {
        edge.setVisible(!hide);
    }

    /**
     * Property to scale the radius of a bond.
     * @return Property to scale the radius of a bond.
     */
    public DoubleProperty bondRadiusScalingProperty(){
        return bondRadiusScaling;
    }

    /**
     * Property to scale the radius of an atom.
     * @return Property to scale the radius of an atom.
     */
    public DoubleProperty atomRadiusScalingProperty(){
        return atomRadiusScaling;
    }

}
