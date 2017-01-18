package pdbmodel;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Testing for pdbmodel.Bond class.
 */
public class BondTest extends TestCase {

	/**
	 * Expect exception when creating a self loop
	 * @throws GraphException
	 */
	@Test(expected= GraphException.class)
	public void testSelfLoopException() throws GraphException {
		Atom n1 = new Atom();
		Bond e = new Bond(n1,n1);
	}

	/**
	 * Expect exception when the source node is equal to null.
	 * @throws GraphException
	 */
	@Test(expected= GraphException.class)
	public void testSourceNullException() throws GraphException {
		Atom n1 = null;
		Atom n2 = new Atom();
		Bond e = new Bond(n1,n2);
	}

	/**
	 * Expect exception when the target node is equal to null.
	 * @throws GraphException
	 */
	@Test(expected= GraphException.class)
	public void testTargetNullException() throws GraphException {
		Atom n1 = new Atom();
		Atom n2 = null;
		Bond e = new Bond(n1,n2);
	}


	/**
	 * Test getSource() method.
	 */
	public void testGetSource(){
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		try {
			Bond e = new Bond(n1, n2);
			assertTrue(e.getSource() == n1);
		}catch (GraphException e){
			fail();
		}
	}

	/**
	 * Test getTarget() method.
	 */
	public void testGetTarget(){
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		try {
			Bond e = new Bond(n1, n2);
			assertTrue(e.getTarget() == n2);
		}catch (GraphException e){
			fail();
		}
	}

	/**
	 * Test getWeight() and setWeight().
	 */
	public void testGetSetWeight(){
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		try {
			Bond e = new Bond(n1, n2, "Testtext");
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
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		try {
			Bond e = new Bond(n1, n2, "Testtext");
			assertTrue(e.getText().equals("Testtext"));
			e.setText("newtext");
			assertTrue(e.getText().equals("newtext"));
		} catch(GraphException e){
			fail();
		}
	}

}
