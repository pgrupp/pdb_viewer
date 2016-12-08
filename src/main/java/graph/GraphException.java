package graph;

/**
 * Exception thrown if e.g. a edge connecting the specified two nodes already exists.
 */
public class GraphException extends Exception {
	public GraphException(String message){
		super(message);
	}
}
