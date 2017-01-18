package pdbview3d;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

/**
 *
 */
public class BoundingBox2D extends Group {

    /**
     * Generate a group with all 2D rectangles enclosing a node.
     * @param pane The pane containing the 3D objects.
     * @param nodeViews List of all 3D nodes in the graph.
     * @param transformProperty The transformation property of the world.
     * @param subScene The SubScene on which the 3D graph is set on.
     */
	public BoundingBox2D(Pane pane, List<Node> nodeViews, Property transformProperty, SubScene subScene){
        nodeViews.stream().map(node -> (MyNodeView3D) node).forEach( (MyNodeView3D node) -> {
            Property[] properties = new Property[]{
                    transformProperty,
                    node.translateXProperty(), node.translateYProperty(), node.translateZProperty(),
                    node.scaleXProperty(), node.scaleYProperty(), node.scaleZProperty(),
                    subScene.widthProperty(), subScene.heightProperty()
            };
            ObjectBinding<Rectangle> binding = createBoundingBoxBinding(pane, node, properties);
            Rectangle box = new Rectangle();
            // Add the rectangle to this group (scene graph)
            this.getChildren().add(box);
            box.setStroke(Color.CORNFLOWERBLUE);
            box.setFill(null);

            box.xProperty().bind(new DoubleBinding() {
                {
                    bind(binding);
                }
                @Override
                protected double computeValue() {
                    return binding.get().getX();
                }
            });

            box.yProperty().bind(new DoubleBinding() {
                {
                    bind(binding);
                }
                @Override
                protected double computeValue() {
                    return binding.get().getY();
                }
            });

            box.scaleXProperty().bind(new DoubleBinding() {
                {
                    bind(binding);
                }
                @Override
                protected double computeValue() {
                    return binding.get().getScaleX();
                }
            });

            box.scaleYProperty().bind(new DoubleBinding() {
                {
                    bind(binding);
                }
                @Override
                protected double computeValue() {
                    return binding.get().getScaleY();
                }
            });

            box.scaleZProperty().bind(new DoubleBinding() {
                {
                    bind(binding);
                }
                @Override
                protected double computeValue() {
                    return binding.get().getScaleZ();
                }
            });

            box.heightProperty().bind(new DoubleBinding() {
                {
                    bind(binding);
                }
                @Override
                protected double computeValue() {
                    return binding.get().getHeight();
                }
            });

            box.widthProperty().bind(new DoubleBinding() {
                {
                    bind(binding);
                }
                @Override
                protected double computeValue() {
                    return binding.get().getWidth();
                }
            });
        });
	}

	private static  ObjectBinding<Rectangle> createBoundingBoxBinding(Pane pane, Node node, Property[] properties) {
		ObjectBinding<Rectangle> rectangle = new ObjectBinding<Rectangle>() {
            {
                for(int i = 0; i < properties.length; i++){
                    super.bind(properties[i]);
                }
            }

			@Override
			protected Rectangle computeValue() {
				final Bounds boundsOnScreen = node.localToScreen(node.getBoundsInLocal());
				final Bounds paneBoundsOnScreen = pane.localToScreen(pane.getBoundsInLocal());
				final double xInScene = boundsOnScreen.getMinX() - paneBoundsOnScreen.getMinX();
				final double yInScene = boundsOnScreen.getMinY() - paneBoundsOnScreen.getMinY();
				return new Rectangle((int) xInScene, (int) yInScene, (int) boundsOnScreen.getWidth(),
											(int) boundsOnScreen.getHeight());
			}

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(properties);
            }

            @Override
            public void dispose() {
                for(int i = 0; i < properties.length; i++){
                    super.unbind(properties[i]);
                }
            }
        };
		return rectangle;
	}
}
