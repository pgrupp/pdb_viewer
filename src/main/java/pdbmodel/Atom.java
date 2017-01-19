package pdbmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

/**
 * Node representation.
 *
 * @author Patrick Grupp
 */
public class Atom {

    public enum ChemicalElement {
        CA, CB, N, O, C;

        /**
         * Get the chemically correct ratio between radii of the elements.
         * @return correct ratio between the radii of the elements. Can be multiplied with some final constant.
         */
         public double getRadius(){
             switch (this){
                 case CA:
                 case CB:
                 case C:
                     return 12;
                 case N:
                     return 14;
                 case O:
                     return 16;
                 default:
                     return 5;
             }
         }

        /**
         * Get the correct color for each element.
         * @return Correct color for each element.
         */
        public Color getColor(){
             switch (this){
                 case CA:
                 case CB:
                 case C:
                     return Color.DARKGREY;
                 case N:
                     return Color.CORNFLOWERBLUE;
                 case O:
                     return Color.RED;
                 default:
                     return Color.GREEN;
             }
         }
    }

    /**
     * Text of the node
     **/
    private StringProperty text;
    /**
     * Weight of the node
     **/
    private DoubleProperty radius;
    /**
     * Outgoing edges of the node
     **/
    private ObservableList<Bond> outEdges;
    /**
     * Ingoing edges of the node
     **/
    private ObservableList<Bond> inEdges;

    /**
     * Residue containing this Atom.
     */
    private ObjectProperty<Residue> residue;

    /**
     * The x coordinate as defined by PDB.
     */
    private DoubleProperty xCoordinate;

    /**
     * The y coordinate as defined by PDB.
     */
    private DoubleProperty yCoordinate;

    /**
     * The z coordinate as defined by PDB.
     */
    private DoubleProperty zCoordinate;

    /**
     * The atom's chemical element (differentiating between C alpha and C beta atoms as well, although they
     * are the same chemical element).
     */
    private ObjectProperty<ChemicalElement> chemicalElement;


    /**
     * Constructor
     */
    Atom() {
        // Initiate lists of in and out nodes empty
        outEdges = FXCollections.observableArrayList();
        inEdges = FXCollections.observableArrayList();

        text = new SimpleStringProperty();
        radius = new SimpleDoubleProperty();
        residue = new SimpleObjectProperty<>();
        chemicalElement = new SimpleObjectProperty<>();

        xCoordinate = new SimpleDoubleProperty();
        yCoordinate = new SimpleDoubleProperty();
        zCoordinate = new SimpleDoubleProperty();
    }

    /**
     * Constructor
     *
     * @param x The atom's x coordinate in the PDB pdbFile space.
     * @param y The atom's y coordinate in the PDB pdbFile space.
     * @param z The atom's z coordinate in the PDB pdbFile space.
     * @param chemicalElement   The atom's chemical element, only CA,CB,N,O allowed.
     */
    public Atom(double x, double y, double z, String chemicalElement, String text) {
        this();
        this.text.setValue(text);
        this.xCoordinate.setValue(x);
        this.yCoordinate.setValue(y);
        this.zCoordinate.setValue(z);
        this.chemicalElement.setValue(ChemicalElement.valueOf(chemicalElement));
        this.radius.setValue(this.chemicalElement.getValue().getRadius());
    }


    /**
     * Get the node's text.
     *
     * @return The node's text property.
     */
    public StringProperty textProperty() {
        return this.text;
    }

    /**
     * Get the node's radius property.
     *
     * @return The node's radius property.
     */
    public DoubleProperty radiusProperty() {
        return this.radius;
    }

    /**
     * Get the node's inEdges observable list property.
     *
     * @return ObservableList of the node's ingoing edges.
     */
    public ObservableList<Bond> inEdgesProperty() {
        return this.inEdges;
    }

    /**
     * Get the node's outEdges observable list property.
     *
     * @return ObservableList of the node's outgoing edges.
     */
    public ObservableList<Bond> outEdgesProperty() {
        return this.outEdges;
    }

    /**
     * Get the atom's residue it belongs to.
     *
     * @return corresponding residue.
     */
    public ObjectProperty<Residue> residueProperty() {
        return this.residue;
    }

    /**
     * Get the atom's chemical element property.
     *
     * @return the atom's chemical element property.
     */
    public ObjectProperty<ChemicalElement> chemicalElementProperty() {
        return this.chemicalElement;
    }

    /**
     * Get the atom's x coordinate property.
     *
     * @return the atom's x coordinate property.
     */
    public DoubleProperty xCoordinateProperty() {
        return xCoordinate;
    }

    /**
     * Get the atom's y coordinate property.
     *
     * @return the atom's y coordinate property.
     */
    public DoubleProperty yCoordinateProperty() {
        return yCoordinate;
    }

    /**
     * Get the atom's z coordinate property.
     *
     * @return the atom's z coordinate property.
     */
    public DoubleProperty zCoordinateProperty() {
        return zCoordinate;
    }


    /**
     * Add an edge to this node's outgoing edges. Does not check if an edge to the edge's target is already pointing
     * from this node.
     *
     * @param outEdge The edge to be added to outgoing edges.
     */
    public void addOutEdge(Bond outEdge) {
        outEdges.add(outEdge);
    }

    /**
     * Add an edge to this node's ingoing edges. Does not check if an edge from the edge's source is already pointing to
     * this node.
     *
     * @param inEdge The edge to be added to ingoing edges.
     */
    public void addInEdge(Bond inEdge) {
        inEdges.add(inEdge);
    }

    /**
     * Remove an edge from the node's ingoing edges.
     *
     * @param inEdge Edge to be removed.
     */
    public void removeInEdge(Bond inEdge) {
        inEdges.remove(inEdge);
    }

    /**
     * Remove an edge from the node's outgoing edges.
     *
     * @param outEdge Edge to be removed.
     */
    public void removeOutEdge(Bond outEdge) {
        outEdges.remove(outEdge);
    }

}
