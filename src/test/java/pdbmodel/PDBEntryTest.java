package pdbmodel;

import javafx.collections.ListChangeListener;
import junit.framework.TestCase;

/**
 * Testing for pdbmodel.PDBEntry class.
 */
public class PDBEntryTest extends TestCase {

	public PDBEntryTest(String name) {
		super(name);
	}


	public void testGetNumberOfNodes() {
		PDBEntry g = new PDBEntry();
		assertTrue(g.getNumberOfNodes() == 0);
		g.addNode(new Atom());
		assertTrue(g.getNumberOfNodes() == 1);
		g.addNode(new Atom());
		assertTrue(g.getNumberOfNodes() == 2);
	}

	public void testConnectNodes() {
		PDBEntry g = new PDBEntry();
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		g.addNode(n1);
		g.addNode(n2);
		try {
			Bond e = new Bond(n1, n2);
			g.edgesProperty().addListener(new ListChangeListener<Bond>() {
				@Override
				public void onChanged(Change<? extends Bond> c) {
					while(c.next()) {
						//Test if added edge is the one connecting the two nodes
						assertTrue(c.getAddedSize() == 1);
						assertEquals(c.getAddedSubList().get(0), e);
					}
				}
			});

			g.connectNodes(e);
		} catch (GraphException ex) {
			fail();
		}
		// Reset
		g = new PDBEntry();
		g.addNode(n1);
		g.nodesProperty().addListener(new ListChangeListener<Atom>() {
			@Override
			public void onChanged(Change<? extends Atom> c) {
				// Check if node is added to the pdbmodel, if it wasn't contained, until a edge to/from it was added
				while(c.next()) {
					assertTrue(c.getAddedSize() == 1);
					assertEquals(c.getAddedSubList().get(0), n2);
				}
			}
		});
		try {
			g.connectNodes(n1, n2);
		} catch (GraphException ex) {
			fail();
		}

	}

	public void testDisconnectNodes() {
		PDBEntry g = new PDBEntry();
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		g.addNode(n1);
		g.addNode(n2);

		assertTrue(g.getNumberOfEdges() == 0);
		try {
			Bond e = new Bond(n1, n2);
			g.connectNodes(e);
			assertTrue(g.getNumberOfEdges() == 1);
			g.edgesProperty().addListener(new ListChangeListener<Bond>() {
				@Override
				public void onChanged(Change<? extends Bond> c) {
					while(c.next()){
						// Check if the removed one is the right edge
						assertTrue(c.getRemovedSize() == 1);
						assertTrue(c.getRemoved().get(0) == e);
					}
				}
			});
			g.disconnectNodes(n1, n2);
			assertTrue(g.getNumberOfEdges() == 0);
		} catch(GraphException ex){
			fail();
		}
	}

	public void testGetNumberOfEdges() {
		PDBEntry g = new PDBEntry();
		Atom n1 = new Atom();
		Atom n2 = new Atom();
		g.addNode(n1);
		g.addNode(n2);
		assertTrue(g.getNumberOfEdges() == 0);
		assertTrue(g.getNumberOfNodes() == 2);
		try {
			g.connectNodes(n1, n2);
			assertTrue(g.getNumberOfEdges() == 1);
			g.connectNodes(n2, n1);
			assertTrue(g.getNumberOfEdges() == 2);
		} catch (GraphException e) {
			fail();
		}
	}

	public void testAddNode() {
		PDBEntry g = new PDBEntry();
		assertTrue(g.getNumberOfNodes() == 0);
		g.addNode(new Atom());
		assertTrue(g.getNumberOfNodes() == 1);
	}

	public void testRemoveNode() {
		PDBEntry g = new PDBEntry();
		Atom node = new Atom();
		assertTrue(g.getNumberOfNodes() == 0);
		g.addNode(node);
		assertTrue(g.getNumberOfNodes() == 1);
		assertEquals(g.getNode(0), node);
		g.removeNode(node);
		assertTrue(g.getNumberOfNodes() == 0);
	}
}
