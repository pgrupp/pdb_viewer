package pdbmodel;

import javafx.beans.property.*;

/**
 * Edge representation.
 *
 * @author Patrick Grupp
 */
public class Bond {

	/**
	 * The source node of the edge
	 **/
	private ObjectProperty<Atom> source;
	/**
	 * The target node of the edge
	 **/
	private ObjectProperty<Atom> target;

	/**
	 * The edge's text
	 **/
	private StringProperty text;
	/**
	 * The edge's weight
	 **/
	private DoubleProperty weight;

	/**
	 * Instantiate an edge with given start and stop nodes.
	 *
	 * @param from From this node the edge will be drawn.
	 * @param to   To this node the edge will be drawn.
	 */
	public Bond(Atom from, Atom to) throws GraphException {
		source = new SimpleObjectProperty<>(from);
		target = new SimpleObjectProperty<>(to);

		text = new SimpleStringProperty();
		weight = new SimpleDoubleProperty();
		validate();
	}

	/**
	 * Create edge with node's name
	 * @param from Source node.
	 * @param to Target node.
	 * @param text Edge's text.
	 * @throws GraphException
	 */
	public Bond(Atom from, Atom to, String text) throws GraphException {
		this(from, to);
		this.text.setValue(text);
	}

	/**
	 * Validate the nodes attributes after change.
	 * @throws GraphException
	 */
	private void validate() throws GraphException {
		if(source == null)
		   throw new GraphException("Source node is null");
		if(target == null)
			throw new GraphException("Target node is null");
		if(source == target)
			throw new GraphException("Cannot connect a node with itself.");
	}

	/**
	 * Get the edge's source node.
	 *
	 * @return The edge's source node.
	 */
	public Atom getSource() {
		return this.source.getValue();
	}

	/**
	 * Get the edge's target node.
	 *
	 * @return The edge's target node.
	 */
	public Atom getTarget() {
		return this.target.getValue();
	}

	/**
	 * Set the edge's text.
	 *
	 * @param text The edge's new text.
	 */
	public void setText(String text) throws GraphException {
		this.text.setValue(text);
		validate();
	}


	/**
	 * Set the node's weight.
	 *
	 * @param weight the new weight of the node.
	 */
	public void setWeight(double weight) throws GraphException {
		this.weight.set(weight);
		validate();
	}

	/**
	 * Get the edge's text.
	 * @return Text
	 */
	public String getText() {
		return text.getValueSafe();
	}

	public StringProperty textProperty() {
		return text;
	}

	public double getWeight() {
		return weight.get();
	}

	public DoubleProperty weightProperty() {
		return weight;
	}

	public ObjectProperty<Atom> sourceProperty(){
		return source;
	}

	public ObjectProperty<Atom> targetProperty(){
		return target;
	}

}
