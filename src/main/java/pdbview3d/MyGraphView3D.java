package pdbview3d;

import pdbmodel.Atom;
import pdbmodel.Bond;
import pdbmodel.PDBEntry;
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
     * The presenter to be called for queries.
     */
    private Presenter presenter;

    /**
     * Constructor for the graph representation in the view. The model needs to make sure, that all nodes represented
     * by its edges are already persisted in the model. otherwise this will produce errors.
     *
     * @param presenter The view presenter
     */
    public MyGraphView3D(Presenter presenter) {
        this.presenter = presenter;
        nodeViewGroup = new Group();
        edgeViewGroup = new Group();

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
        MyNodeView3D node = new MyNodeView3D(atom);
        // Set up the view logic in the presenter for this node.
        presenter.setUpNodeView(node);
        // Add the node to the scene graph
        nodeViewGroup.getChildren().add(node);
    }

    /**
     * Remove a node from the view. NOTE: Assumes the edges have already been deleted by the model.
     *
     * @param atom The model node to be removed.
     */
    public void removeNode(Atom atom) {
        // Filter for view's node to be removed through all view nodes.
        List<Node> node = nodeViewGroup.getChildren().stream().filter(p -> {
            MyNodeView3D tmp = (MyNodeView3D) p;
            return tmp.getModelNodeReference().equals(atom);
        }).collect(Collectors.toList());

        // Should only be one node, else there is an error.
        if (node.size() == 1) {
            MyNodeView3D n = (MyNodeView3D) node.get(0);
            nodeViewGroup.getChildren().remove(n);
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
            MyEdgeView3D tmp = new MyEdgeView3D(bond, (MyNodeView3D) source.get(0), (MyNodeView3D) target.get(0));
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
}
