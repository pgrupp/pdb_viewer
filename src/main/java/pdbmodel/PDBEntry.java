package pdbmodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
     * Constructor
     */
    public PDBEntry() {
        nodes = FXCollections.observableArrayList();
        edges = FXCollections.observableArrayList();
        secondaryStructures = FXCollections.observableArrayList();
        residues = FXCollections.observableArrayList();
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
     * @return All secondary structures noted in the given PDB file.
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
     * Print graph representation in TGF format.
     *
     * @param out {@code PrintStream} to print the output to. Can be {@code System.out} or any other {@code
     *            PrintStream}.
     */
    public void write(PrintStream out) {
        int id = 0;
        Map<Atom, Integer> nodesOutMap = new HashMap<>();
        for (Atom n : nodes) {
            nodesOutMap.put(n, id);
            String nodeName = n.textProperty().getValueSafe(); // if StringProperty is null an empty String is retrieved
            out.println(id + "\t" + nodeName);
            id++;
        }
        out.println("#");
        for (Bond e : edges) {
            String edgeName = e.textProperty().getValueSafe();
            int sourceID = nodesOutMap.get(e.getSource());
            int targetID = nodesOutMap.get(e.getTarget());
            out.println(sourceID + "\t" + targetID + "\t" + edgeName);
        }
    }


    /**
     * Read graph from text file in trivial graph format (TGF).
     *
     * @param graphFile File in TGF format from which the graph should be read.
     * @throws IOException Thrown if the file in {@code filePath} does not exist or any other IO Exception occurs.
     */
    public void read(File graphFile) throws Exception {
        reset();
        if (!graphFile.exists()) {
            throw new FileNotFoundException("The file " + graphFile.getPath() + " does not exist.");
        }
        BufferedReader reader = new BufferedReader(new FileReader(graphFile));
        String curr;
        boolean readingNodes = true; // Still in the nodes section of the file? or in the edges section?
        Map<Integer, Atom> mappingIdsToNodes = new TreeMap<>();
        while ((curr = reader.readLine()) != null) {
            readingNodes = processLine(curr, readingNodes, mappingIdsToNodes);
        }
        if (nodes.size() == 0) {
            throw new Exception("No nodes were read from file. Exiting.");
        }
    }

    /**
     * Resets the graph to initial state, deleting all nodes and edges (implicitly):
     */
    public void reset() {
        edges.clear();
        nodes.clear();
        secondaryStructures.clear();
        residues.clear();
    }

    /**
     * Process the line given from a trivial graph format (TGF) file and add nodes and edges to the graph. Handle
     * misformed lines by skipping the.
     *
     * @param line         The line of a TGF file to be loaded into the model.
     * @param readingNodes Boolean determining, whether currently reading the first part of the file, so reading the
     *                     nodes, or reading the second part of the file, reading the edges.
     * @return The new status of {@param readingNode}, determines if still reading the nodes, or triggers the second
     * part reading edges.
     */
    private boolean processLine(String line, boolean readingNodes, Map<Integer, Atom> mappingIdsToNodes) {
        // Switch from reading nodes to reading edges -> Change state
        if (line.startsWith("#"))
            return false;

        if (readingNodes) {
            //Reading nodes
            String[] input = line.split("\t");
            try {
                int nodeID = Integer.parseInt(input[0]);
                if (!mappingIdsToNodes.containsKey(nodeID)) {
                    String nodeName = "";
                    for (int i = 1; i < input.length; i++) {
                        nodeName = nodeName.concat(input[i]);
                    }
                    Atom node = new Atom(nodeName);
                    mappingIdsToNodes.put(nodeID, node);
                    // Add node to model
                    addNode(node);
                } else {
                    System.err.println(
                            "Could not read the following line, due to duplicate ID " + nodeID + ":\n" + line);
                }
            } catch (NumberFormatException e) {
                System.err.println(
                        "Could not handle the following line in input file, skipping it:\n" + line + "\nReason: " +
                                e.getMessage());
            }
            //this return does not change the state
            return true;

        } else {
            //Reading edges
            String[] input = line.split("\t");
            try {
                if (input.length < 2)
                    throw new GraphException("Invalid line in input file edge not specifying source and target nodes.");
                int sourceID = Integer.parseInt(input[0]);
                int targetID = Integer.parseInt(input[1]);
                // Throw different errors, if input is not valid (eg. self loop or non-existent nodes
                if (!mappingIdsToNodes.containsKey(sourceID))
                    throw new GraphException("Source node with ID " + sourceID + " does not exist. " +
                            "Skipping edge with input line:\n" + line);
                if (!mappingIdsToNodes.containsKey(targetID))
                    throw new GraphException("Target node with ID " + sourceID + " does not exist. " +
                            "Skipping edge with input line:\n" + line);
                if (sourceID == targetID)
                    throw new GraphException("Graph does not allow self loops for node with ID " + sourceID +
                            ". Skipping input file line.");

                String edgeName = "";
                for (int i = 2; i < input.length; i++) {
                    edgeName = edgeName.concat(input[i]);
                }
                Atom source = mappingIdsToNodes.get(sourceID);
                Atom target = mappingIdsToNodes.get(targetID);
                Bond edge = new Bond(source, target, edgeName);
                connectNodes(edge);

            } catch (NumberFormatException e) {
                System.err.println(
                        "Could not handle the following line in input file, skipping it:\n" + line + "\nReason: " +
                                e.getMessage());
            } catch (GraphException e) {
                System.err.println(e.getMessage());
            }
            // this return does not change the stat
            return false;
        }
    }

}
