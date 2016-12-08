package graph;

import javafx.collections.ListChangeListener;
import junit.framework.TestCase;

/**
 * Testing for graph.MyGraph class.
 */
public class MyGraphTest extends TestCase {

	public MyGraphTest(String name) {
		super(name);
	}


	public void testGetNumberOfNodes() {
		MyGraph g = new MyGraph();
		assertTrue(g.getNumberOfNodes() == 0);
		g.addNode(new MyNode());
		assertTrue(g.getNumberOfNodes() == 1);
		g.addNode(new MyNode());
		assertTrue(g.getNumberOfNodes() == 2);
	}

	public void testConnectNodes() {
		MyGraph g = new MyGraph();
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		g.addNode(n1);
		g.addNode(n2);
		try {
			MyEdge e = new MyEdge(n1, n2);
			g.edgesProperty().addListener(new ListChangeListener<MyEdge>() {
				@Override
				public void onChanged(Change<? extends MyEdge> c) {
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
		g = new MyGraph();
		g.addNode(n1);
		g.nodesProperty().addListener(new ListChangeListener<MyNode>() {
			@Override
			public void onChanged(Change<? extends MyNode> c) {
				// Check if node is added to the graph, if it wasn't contained, until a edge to/from it was added
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
		MyGraph g = new MyGraph();
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
		g.addNode(n1);
		g.addNode(n2);

		assertTrue(g.getNumberOfEdges() == 0);
		try {
			MyEdge e = new MyEdge(n1, n2);
			g.connectNodes(e);
			assertTrue(g.getNumberOfEdges() == 1);
			g.edgesProperty().addListener(new ListChangeListener<MyEdge>() {
				@Override
				public void onChanged(Change<? extends MyEdge> c) {
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
		MyGraph g = new MyGraph();
		MyNode n1 = new MyNode();
		MyNode n2 = new MyNode();
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
		MyGraph g = new MyGraph();
		assertTrue(g.getNumberOfNodes() == 0);
		g.addNode(new MyNode());
		assertTrue(g.getNumberOfNodes() == 1);
	}

	public void testRemoveNode() {
		MyGraph g = new MyGraph();
		MyNode node = new MyNode();
		assertTrue(g.getNumberOfNodes() == 0);
		g.addNode(node);
		assertTrue(g.getNumberOfNodes() == 1);
		assertEquals(g.getNode(0), node);
		g.removeNode(node);
		assertTrue(g.getNumberOfNodes() == 0);
	}
}
