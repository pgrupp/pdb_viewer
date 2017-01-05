package graph;

import javafx.beans.property.*;

/**
 * Edge representation.
 *
 * @author Patrick Grupp
 */
public class MyEdge {

	/**
	 * The source node of the edge
	 **/
	private ObjectProperty<MyNode> source;
	/**
	 * The target node of the edge
	 **/
	private ObjectProperty<MyNode> target;

	/**
	 * The edge's text
	 **/
	private StringProperty text;
	/**
	 * The edge's weight
	 **/
	private DoubleProperty weight;
	/**
	 * The edge's data
	 **/
	private ObjectProperty<Object> userData;


	/**
	 * Instantiate an edge with given start and stop nodes.
	 *
	 * @param from From this node the edge will be drawn.
	 * @param to   To this node the edge will be drawn.
	 */
	public MyEdge(MyNode from, MyNode to) throws GraphException {
		source = new SimpleObjectProperty<>(from);
		target = new SimpleObjectProperty<>(to);

		text = new SimpleStringProperty();
		weight = new SimpleDoubleProperty();
		userData = new SimpleObjectProperty<>();
		validate();
	}

	/**
	 * Create edge with node's name
	 * @param from Source node.
	 * @param to Target node.
	 * @param text Edge's text.
	 * @throws GraphException
	 */
	public MyEdge(MyNode from, MyNode to, String text) throws GraphException {
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
	public MyNode getSource() {
		return this.source.getValue();
	}

	/**
	 * Get the edge's target node.
	 *
	 * @return The edge's target node.
	 */
	public MyNode getTarget() {
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
	 * Set user data.
	 *
	 * @param userData the data to be set to
	 */
	public void setUserData(Object userData) throws GraphException {
		this.userData.setValue(userData);
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

	public Object getUserData() {
		return userData.get();
	}

	public ObjectProperty<Object> userDataProperty() {
		return userData;
	}

	public ObjectProperty<MyNode> sourceProperty(){
		return source;
	}

	public ObjectProperty<MyNode> targetProperty(){
		return target;
	}

}
