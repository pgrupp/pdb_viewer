package graph;

import javafx.collections.ListChangeListener;
import junit.framework.TestCase;

/**
 * Test the graph.MyNode class
 */
public class MyNodeTest extends TestCase {

	public MyNodeTest(String name) {
		super(name);
	}

	public void testAddInEdge() {
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		try {
			MyEdge edge = new MyEdge(n1, n2);
			n2.inEdgesProperty().addListener(new ListChangeListener<MyEdge>() {
				@Override
				public void onChanged(Change<? extends MyEdge> c) {
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
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		try {
			MyEdge edge = new MyEdge(n1, n2);
			n1.outEdgesProperty().addListener(new ListChangeListener<MyEdge>() {
				@Override
				public void onChanged(Change<? extends MyEdge> c) {
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
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		try {
			MyEdge edge = new MyEdge(n1, n2);
			n2.addInEdge(edge);
			assertTrue(n2.inEdgesProperty().contains(edge));
			n2.inEdgesProperty().addListener(new ListChangeListener<MyEdge>() {
				@Override
				public void onChanged(Change<? extends MyEdge> c) {
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
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		try {
			MyEdge edge = new MyEdge(n1, n2);
			n1.addOutEdge(edge);
			assertTrue(n1.outEdgesProperty().contains(edge));
			n1.outEdgesProperty().addListener(new ListChangeListener<MyEdge>() {
				@Override
				public void onChanged(Change<? extends MyEdge> c) {
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
