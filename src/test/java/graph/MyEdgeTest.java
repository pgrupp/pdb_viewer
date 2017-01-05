package graph;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Testing for graph.MyEdge class.
 */
public class MyEdgeTest extends TestCase {

	/**
	 * Expect exception when creating a self loop
	 * @throws GraphException
	 */
	@Test(expected= GraphException.class)
	public void testSelfLoopException() throws GraphException {
		MyNode n1 = new MyNode();
		MyEdge e = new MyEdge(n1,n1);
	}

	/**
	 * Expect exception when the source node is equal to null.
	 * @throws GraphException
	 */
	@Test(expected= GraphException.class)
	public void testSourceNullException() throws GraphException {
		MyNode n1 = null;
		MyNode n2 = new MyNode();
		MyEdge e = new MyEdge(n1,n2);
	}

	/**
	 * Expect exception when the target node is equal to null.
	 * @throws GraphException
	 */
	@Test(expected= GraphException.class)
	public void testTargetNullException() throws GraphException {
		MyNode n1 = new MyNode();
		MyNode n2 = null;
		MyEdge e = new MyEdge(n1,n2);
	}


	/**
	 * Test getSource() method.
	 */
	public void testGetSource(){
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		try {
			MyEdge e = new MyEdge(n1, n2);
			assertTrue(e.getSource() == n1);
		}catch (GraphException e){
			fail();
		}
	}

	/**
	 * Test getTarget() method.
	 */
	public void testGetTarget(){
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		try {
			MyEdge e = new MyEdge(n1, n2);
			assertTrue(e.getTarget() == n2);
		}catch (GraphException e){
			fail();
		}
	}

	/**
	 * Test getWeight() and setWeight().
	 */
	public void testGetSetWeight(){
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		try {
			MyEdge e = new MyEdge(n1, n2, "Testtext");
			assertTrue(e.getWeight() == 0.0);
			e.setWeight(1.75);
			assertTrue(e.getWeight() == 1.75);
		} catch(GraphException e){
			fail();
		}
	}

	/**
	 * Test getText() and setText().
	 */
	public void testGetSetText(){
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		try {
			MyEdge e = new MyEdge(n1, n2, "Testtext");
			assertTrue(e.getText().equals("Testtext"));
			e.setText("newtext");
			assertTrue(e.getText().equals("newtext"));
		} catch(GraphException e){
			fail();
		}
	}

}
