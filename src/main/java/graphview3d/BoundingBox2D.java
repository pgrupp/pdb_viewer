package graphview3d;

import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.util.Properties;

/**
 *
 */
public class BoundingBox2D extends Group {
	
//	Rectangle boundingBox;
//	final ObjectBinding<Rectangle> binding;
//
//	public BoundingBox2D(Pane pane, Node node, Properties[] properties){
//		boundingBox = new Rectangle();
//
//		binding = createBoundingBoxBinding(pane, node, properties);
//		boundingBox.getBounds().
//	}
//
//	private ObjectBinding<Rectangle> createBoundingBoxBinding(Pane pane, Node node, Properties[] properties) {
//		ObjectBinding<Rectangle> rectangle = new ObjectBinding<Rectangle>() {
//			@Override
//			protected Rectangle computeValue() {
//				final Bounds boundsOnScreen = node.localToScreen(node.getBoundsInLocal());
//				final Bounds paneBoundsOnScreen = pane.localToScreen(pane.getBoundsInLocal());
//				final double xInScene = boundsOnScreen.getMinX() - paneBoundsOnScreen.getMinX();
//				final double yInScene = boundsOnScreen.getMinY() - paneBoundsOnScreen.getMinY();
//				return new Rectangle((int) xInScene, (int) yInScene, (int) boundsOnScreen.getWidth(),
//											(int) boundsOnScreen.getHeight());
//			}
//		};
//		return rectangle;
//	}
}
