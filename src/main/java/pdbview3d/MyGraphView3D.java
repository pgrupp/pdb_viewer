package pdbview3d;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import pdbmodel.Atom;
import pdbmodel.Bond;
import pdbmodel.PDBEntry;
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
    private Map<Atom, MyNodeView3D> modelToView;

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
        modelToView = new HashMap<>();
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
        modelToView.put(atom, node);
    }

    /**
     * Remove a node from the view. NOTE: Assumes the edges have already been deleted by the model.
     *
     * @param atom The model node to be removed.
     */
    public void removeNode(Atom atom) {
        // Filter for view's node to be removed through all view nodes.
        if (modelToView.containsKey(atom)) {

            MyNodeView3D current = modelToView.get(atom);
            nodeViewGroup.getChildren().remove(current);
            modelToView.remove(atom);
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
        List<Node> source = nodeViewGroup.getChildren().stream().filter(p -> {
            MyNodeView3D curr = (MyNodeView3D) p;
            return curr.getModelNodeReference().equals(sourceNode);
        }).collect(Collectors.toList());

        List<Node> target = nodeViewGroup.getChildren().stream().filter(p -> {
            MyNodeView3D curr = (MyNodeView3D) p;
            return curr.getModelNodeReference().equals(targetNode);
        }).collect(Collectors.toList());


        // source and target nodes found? then add the edge. else print an error
        if (source.size() == 1 && target.size() == 1) {
            // Create new view edge
            MyEdgeView3D tmp = new MyEdgeView3D(bond, (MyNodeView3D) source.get(0), (MyNodeView3D) target.get(0), this.bondRadiusScaling);
            // Add edge to the scene graph
            edgeViewGroup.getChildren().add(tmp);
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
        List<Node> temp = edgeViewGroup.getChildren().stream().filter(p -> {
            MyEdgeView3D edge = (MyEdgeView3D) p;
            return edge.getModelEdgeReference().equals(bond);
        }).collect(Collectors.toList());
        // Remove the found one -> should only be one
        for (Node e : temp) {
            edgeViewGroup.getChildren().remove(e);
        }
    }

    // TODO public void colorBySecondaryStructure
    // TODO public void colorByResidue
    // TODO public void colorDefault

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
     * @param atom The model instance.
     * @return The corresponding view node instance.
     */
    public MyNodeView3D getNodeByModel(Atom atom){
        return modelToView.get(atom);
    }

    /**
     * Hides the edges.
     * @param hide Specifies if to hide, or to show the edges.
     */
    public void hideEdges(boolean hide){
        if(hide)
        this.getChildren().remove(edgeViewGroup);
        else
            if(!this.getChildren().contains(edgeViewGroup))
                this.getChildren().add(edgeViewGroup);
    }

    /**
     * Hides the nodes.
     * @param hide Specifies if to hide, or to show the edges.
     */
    public void hideNodes(boolean hide){
        if(hide)
            this.getChildren().remove(nodeViewGroup);
        else
        if(!this.getChildren().contains(nodeViewGroup))
            this.getChildren().add(nodeViewGroup);
    }

    /**
     * Set the color of an atom.
     * @param atom Atom for which the color should be set.
     * @param color The color to set it to.
     */
    public void setColor(Atom atom, Color color){
        modelToView.get(atom).setColor(color);
    }

}
