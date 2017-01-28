package pdbview3d;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import pdbmodel.Bond;

/**
 * view.View of an edge in 2 dimensional space. NOTE: Always add the two nodes to the model, before adding the connecting edge.
 *
 * @author Patrick Grupp
 */
public class MyEdgeView3D extends Group {

    private MyLine3D line;
    private DoubleProperty radius;
    private ObjectProperty<Color> color;
    private Bond modelEdgeReference;
    private MyNodeView3D source;
    private MyNodeView3D target;

    /**
     * Constructor
     *
     * @param reference     Reference to the model edge.
     * @param source        The Source node
     * @param target        The Target node
     * @param radiusScaling The scaling factor with which the radius will be scaled.
     */
    MyEdgeView3D(Bond reference, MyNodeView3D source, MyNodeView3D target, DoubleProperty radiusScaling) {

        this.modelEdgeReference = reference;
        this.source = source;
        this.target = target;
        // color for this edge
        this.color = new SimpleObjectProperty<>(Color.LIGHTGRAY);
        this.radius = new SimpleDoubleProperty();
        radius.bind(radiusScaling.multiply(3));

        // Bind line start point to source node's start coordinates
        // Bind line to end/target nodes coordinates
        line = new MyLine3D(source.translateXProperty(), source.translateYProperty(), source.translateZProperty(),
                target.translateXProperty(), target.translateYProperty(), target.translateZProperty(),
                radius, color);

        // Add line to scene graph/ this group
        this.getChildren().add(line);
    }

    /**
     * Get the reference to the model's edge.
     *
     * @return Model edge represented by this view representation.
     */
    Bond getModelEdgeReference() {
        return modelEdgeReference;
    }

    /**
     * Get the radius property. Determining the lines radius.
     *
     * @return Radius property.
     */
    public DoubleProperty radiusProperty() {
        return this.radius;
    }


    /**
     * Get the color property. Determines the line's color. Default is lightgray.
     * @return Color property of the edge.
     */
    public ObjectProperty<Color> colorProperty(){
        return this.color;
    }

    /**
     * Get the view's source node.
     *
     * @return view.View source node.
     */
    public MyNodeView3D getSourceNodeView() {
        return this.source;
    }

    /**
     * Get the view's target node.
     *
     * @return view.View target ndoe.
     */
    public MyNodeView3D getTargetNodeView() {
        return this.target;
    }

    /**
     * Get the edge's line shape.
     *
     * @return The line of the edge.
     */
    public MyLine3D getLine() {
        return this.line;
    }
}
