package pdbmodel;

import javafx.collections.ListChangeListener;
import junit.framework.TestCase;

/**
 * Test the pdbmodel.Atom class
 */
public class AtomTest extends TestCase {

	public AtomTest(String name) {
		super(name);
	}

	public void testAddInEdge() {
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		try {
			Bond edge = new Bond(n1, n2);
			n2.inEdgesProperty().addListener(new ListChangeListener<Bond>() {
				@Override
				public void onChanged(Change<? extends Bond> c) {
					while (c.next()) {
						assertTrue(c.getAddedSize() == 1);
						assertTrue(c.getAddedSubList().contains(edge));
						assertTrue(c.getAddedSubList().get(0).equals(edge));
					}
				}
			});
			n2.addInEdge(edge);
			assertTrue(n2.inEdgesProperty().contains(edge));
		} catch (GraphException e) {
			fail();
		}
	}

	public void testAddOutEdge() {
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		try {
			Bond edge = new Bond(n1, n2);
			n1.outEdgesProperty().addListener(new ListChangeListener<Bond>() {
				@Override
				public void onChanged(Change<? extends Bond> c) {
					while (c.next()) {
						assertTrue(c.getAddedSize() == 1);
						assertTrue(c.getAddedSubList().contains(edge));
						assertTrue(c.getAddedSubList().get(0).equals(edge));
					}
				}
			});
			n1.addOutEdge(edge);
			assertTrue(n1.outEdgesProperty().contains(edge));
		} catch (GraphException e) {
			fail();
		}
	}

	public void testRemoveInEdge() {
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		try {
			Bond edge = new Bond(n1, n2);
			n2.addInEdge(edge);
			assertTrue(n2.inEdgesProperty().contains(edge));
			n2.inEdgesProperty().addListener(new ListChangeListener<Bond>() {
				@Override
				public void onChanged(Change<? extends Bond> c) {
					while (c.next()) {
						assertTrue(c.getRemovedSize() == 1);
						assertTrue(c.getRemoved().contains(edge));
						assertTrue(c.getRemoved().get(0).equals(edge));
					}
				}
			});
			n2.removeInEdge(edge);
			assertFalse(n2.inEdgesProperty().contains(edge));
		} catch (GraphException e) {
			fail();
		}
	}

	public void testRemoveOutEdge() {
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		try {
			Bond edge = new Bond(n1, n2);
			n1.addOutEdge(edge);
			assertTrue(n1.outEdgesProperty().contains(edge));
			n1.outEdgesProperty().addListener(new ListChangeListener<Bond>() {
				@Override
				public void onChanged(Change<? extends Bond> c) {
					while (c.next()) {
						assertTrue(c.getRemovedSize() == 1);
						assertTrue(c.getRemoved().contains(edge));
						assertTrue(c.getRemoved().get(0).equals(edge));
					}
				}
			});
			n1.removeOutEdge(edge);
			assertFalse(n1.outEdgesProperty().contains(edge));
		} catch (GraphException e) {
			fail();
		}
	}

}
