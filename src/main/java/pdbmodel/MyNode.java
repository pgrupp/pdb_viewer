package pdbmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Node representation.
 *
 * @author Patrick Grupp
 */
public class MyNode {

	/**
	 * Text of the node
	 **/
	private StringProperty text;
	/**
	 * Weight of the node
	 **/
	private DoubleProperty weight;
	/**
	 * Outgoing edges of the node
	 **/
	private ObservableList<MyEdge> outEdges;
	/**
	 * Ingoing edges of the node
	 **/
	private ObservableList<MyEdge> inEdges;
	/**
	 * Data object of the node
	 **/
	private ObjectProperty<Object> userData;

	/**
	 * Constructor
	 */
	public MyNode() {
		// Initiate lists of in and out nodes empty
		outEdges = FXCollections.observableArrayList();
		inEdges = FXCollections.observableArrayList();

		text = new SimpleStringProperty();
		weight = new SimpleDoubleProperty();
		userData = new SimpleObjectProperty<>();
	}

	/**
	 * Constructor
	 *
	 * @param text Set the node's name/text
	 */
	public MyNode(String text) {
		this();
		this.text.setValue(text);
	}

	/**
	 * Constructor
	 *
	 * @param weight   The node's weight
	 * @param userData The node's userData object
	 * @param text Set the node's name/text
	 */
	public MyNode(String text, double weight, Object userData) {
		this();

		this.text.setValue(text);
		this.weight.setValue(weight);
		this.userData.setValue(userData);
	}

	/**
	 * Get the user data object.
	 *
	 * @return The node's user data object of type {@link Object}.
	 */
	public ObjectProperty userDataProperty() {
		return this.userData;
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
	 * Get the node's weight property.
	 *
	 * @return The node's weight property.
	 */
	public DoubleProperty weightProperty() {
		return this.weight;
	}

	/**
	 * Get the node's inEdges observable list property.
	 *
	 * @return ObservableList of the node's ingoing edges.
	 */
	public ObservableList<MyEdge> inEdgesProperty() {
		return this.inEdges;
	}

	/**
	 * Get the node's outEdges observable list property.
	 *
	 * @return ObservableList of the node's outgoing edges.
	 */
	public ObservableList<MyEdge> outEdgesProperty() {
		return this.outEdges;
	}


	/**
	 * Add an edge to this node's outgoing edges. Does not check if an edge to the edge's target is already pointing
	 * from this node.
	 *
	 * @param outEdge The edge to be added to outgoing edges.
	 */
	public void addOutEdge(MyEdge outEdge) {
		outEdges.add(outEdge);
	}

	/**
	 * Add an edge to this node's ingoing edges. Does not check if an edge from the edge's source is already pointing to
	 * this node.
	 *
	 * @param inEdge The edge to be added to ingoing edges.
	 */
	public void addInEdge(MyEdge inEdge) {
		inEdges.add(inEdge);
	}

	/**
	 * Remove an edge from the node's ingoing edges.
	 *
	 * @param inEdge Edge to be removed.
	 */
	public void removeInEdge(MyEdge inEdge) {
		inEdges.remove(inEdge);
	}

	/**
	 * Remove an edge from the node's outgoing edges.
	 *
	 * @param outEdge Edge to be removed.
	 */
	public void removeOutEdge(MyEdge outEdge) {
		outEdges.remove(outEdge);
	}

}
