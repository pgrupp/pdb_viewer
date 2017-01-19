package pdbmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An entry of the Protein Data Bank (PDB) containing all Residues, which in turn contain all atoms associated with
 * them, and all secondary structures.
 *
 * @author Patrick Grupp
 */
public class PDBEntry {

    /**
     * Observable list of all nodes
     **/
    private ObservableList<Atom> nodes;

    /**
     * Observable list of all edges
     **/
    private ObservableList<Bond> edges;

    /**
     * The pdb entry's secondary structures as an observable list.
     */
    private ObservableList<SecondaryStructure> secondaryStructures;

    /**
     * The pdb entry's residues as an observable list.
     */
    private ObservableList<Residue> residues;

    /**
     * Title of the PDB file shortly describing the protein shown.
     */
    private StringProperty title;

    /**
     * The four letter code of the shown pdb structure.
     */
    private StringProperty pdbCode;

    /**
     * Constructor
     */
    public PDBEntry() {
        nodes = FXCollections.observableArrayList();
        edges = FXCollections.observableArrayList();
        secondaryStructures = FXCollections.observableArrayList();
        residues = FXCollections.observableArrayList();
        title = new SimpleStringProperty();
        pdbCode = new SimpleStringProperty();
    }

    /**
     * Get a {@link ObservableList} of all {@link Atom}s in the Graph.
     *
     * @return List of nodes in the graph.
     */
    public ObservableList<Atom> nodesProperty() {
        return nodes;
    }

    /**
     * Get a {@link ObservableList} of all {@link Bond}s in the Graph.
     *
     * @return List of edges in the graph.
     */
    public ObservableList<Bond> edgesProperty() {
        return edges;
    }

    /**
     * Get a {@link ObservableList} of all {@link SecondaryStructure}s in the PDB Entry.
     *
     * @return All secondary structures noted in the given PDB pdbFile.
     */
    public ObservableList<SecondaryStructure> secondaryStructuresProperty() {
        return secondaryStructures;
    }

    /**
     * Get a {@link ObservableList} of all {@link Residue}s in the PDB Entry.
     *
     * @return All residues containing their respective Atoms.
     */
    public ObservableList<Residue> residuesProperty() {
        return residues;
    }

    /**
     * Get the short description of the entry (protein).
     * @return Property holding the Title of the PDB entry, a short protein description.
     */
    public StringProperty titleProperty(){
        return this.title;
    }

    /**
     * Get the pdb ID property.
     * @return Property holding the PDB ID.
     */
    public StringProperty pdbCodeProperty(){
        return this.pdbCode;
    }

    /**
     * Add residue to the list of residues of the model.
     * @param res
     */
    public void addResidue(Residue res){
        residues.add(res);
    }

    /**
     * Add a node to the graph.
     *
     * @param n Node to be added to the graph.
     */
    public void addNode(Atom n) {
        nodes.add(n);
    }

    /**
     * Get node in nodes list with index idx.
     *
     * @param idx Index of node in list.
     * @return Node requested.
     */
    public Atom getNode(int idx) {
        return nodes.get(idx);
    }

    /**
     * Remove a node from the graph an remove edges connecting it.
     *
     * @param n Node to be deleted.
     */
    public void removeNode(Atom n) {
        List<Bond> edgesToBeRemoved =
                edges.stream().filter(p -> p.getTarget() == n || p.getSource() == n).collect(Collectors.toList());
        for (Bond e : edgesToBeRemoved) {
            deleteEdge(e);
        }
        nodes.remove(n);
    }

    /**
     * Connect the given nodes with a new edge.
     *
     * @param source The source node.
     * @param target The target node
     */
    public void connectNodes(Atom source, Atom target) throws GraphException {
        Bond connection = new Bond(source, target);
        connectNodes(connection);
    }

    /**
     * Connect nodes n1 and n2 with edge e.
     *
     * @param e edge for connecting the two nodes.
     * @throws pdbmodel.GraphException if edge already exists
     */
    public void connectNodes(Bond e) throws GraphException {
        if (!graphContainsEdge(e)) {
            // Add new nodes if necessary
            if (!nodes.contains(e.getSource()))
                addNode(e.getSource());
            if (!nodes.contains(e.getTarget()))
                addNode(e.getTarget());

            edges.add(e);
            e.getSource().addOutEdge(e);
            e.getTarget().addInEdge(e);
        } else
            throw new GraphException("Edge already exists");
    }

    /**
     * Disconnect two nodes by removing the edge connecting the source with the target.
     *
     * @param source First node.
     * @param target Second node.
     */
    void disconnectNodes(Atom source, Atom target) {
        // Get edges with source and target as source and target respectively or vice versa.
        List<Bond> connectingEdges =
                edges.stream().filter(p -> (p.getSource() == source && p.getTarget() == target)).collect(
                        Collectors.toList());
        for (Bond e : connectingEdges) {
            deleteEdge(e);
        }
    }

    /**
     * Delete edge from graph.
     *
     * @param e edge to be removed from graph
     */
    public void deleteEdge(Bond e) {
        e.getSource().removeOutEdge(e);
        e.getTarget().removeInEdge(e);
        edges.remove(e);
    }

    /**
     * Get the number of edges in the graph
     *
     * @return number of edges in the graph
     */
    public int getNumberOfEdges() {
        return edges.size();
    }

    /**
     * get the number of nodes in the graph
     *
     * @return number of nodes in the graph
     */
    public int getNumberOfNodes() {
        return nodes.size();
    }

    /**
     * Get the number of secondary structures.
     * @return the number of secondary structures.
     */
    public int getNumberOfSecondaryStructures(){
        return secondaryStructures.size();
    }

    /**
     * Get the number of residues (!= atoms).
     * @return the number of residues.
     */
    public int getNumberOfResidues() {
        return residues.size();
    }

    /**
     * Does the graph already contain this edge.
     *
     * @param e The edge to be checked, if it is contained in the graph.
     * @return true if edge e is contained in the graph, else false.
     */
    private boolean graphContainsEdge(Bond e) {
        // Create list matching the predicate of having the same source and target. Should be 0. If not there is
        // already a edge connecting the two nodes in the same direction
        List<Bond> edgeWithSameSourceAndTarget =
                edges.stream().filter(p -> p.getSource() == e.getSource() && p.getTarget() == e.getTarget()).collect(
                        Collectors.toList());
        return edgeWithSameSourceAndTarget.size() != 0;
    }

    /**
     * Resets the graph to initial state, deleting all nodes and edges (implicitly):
     */
    public void reset() {
        edges.clear();
        nodes.clear();
        secondaryStructures.clear();
        residues.clear();
        titleProperty().setValue("");
        pdbCodeProperty().setValue("");
    }

}
