package pdbview3d;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;

/**
 * JavaFX class representing a line in three dimensional space, using a cylinder
 */
class MyLine3D extends Group {
    private Cylinder cy;

    MyLine3D(DoubleProperty startXProperty, DoubleProperty startYProperty, DoubleProperty startZProperty,
             DoubleProperty endXProperty, DoubleProperty endYProperty, DoubleProperty endZProperty,
             DoubleProperty radiusProperty, ObjectProperty<Color> color) {
        // Initialize the shape
        cy = new Cylinder();

        // Bind the radius to the EdgeView's radius property
        cy.radiusProperty().bind(radiusProperty);
        // Set the shape's color and highlighting color
        PhongMaterial mat = new PhongMaterial();
        mat.diffuseColorProperty().bind(color);
        color.addListener(event -> mat.setSpecularColor(color.getValue().brighter()));
        cy.setMaterial(mat);

        // Add shape to scene graph
        this.getChildren().add(cy);

        InvalidationListener listener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {

                // create points of the start and end coordinates
                Point3D startPoint =
                        new Point3D(startXProperty.getValue(), startYProperty.getValue(), startZProperty.getValue());
                Point3D endPoint =
                        new Point3D(endXProperty.getValue(), endYProperty.getValue(), endZProperty.getValue());
                // center where to set the cylinders center (midpoint between start and end)
                Point3D centerOfCylinder = startPoint.midpoint(endPoint);
                // y axis point
                Point3D yAxis = new Point3D(0, 1, 0);

                // Compute a point representing the direction the shape should represent
                Point3D directionPoint = endPoint.subtract(startPoint);

                // Compute the rotation axis
                Point3D rotationAxis = directionPoint.crossProduct(yAxis);

                // angle
                double angle = -directionPoint.angle(yAxis);

                // Compute the height of the cylinder
                double heightOfCylinder = endPoint.distance(startPoint);


                // Use the computed values in order to set the cylinders properties appropriately
                cy.setRotationAxis(rotationAxis);
                cy.setRotate(angle);
                cy.setTranslateX(centerOfCylinder.getX());
                cy.setTranslateY(centerOfCylinder.getY());
                cy.setTranslateZ(centerOfCylinder.getZ());
                cy.setHeight(heightOfCylinder);
            }
        };

        // Add the listener to all properties in order to react to changes
        startXProperty.addListener(listener);
        startYProperty.addListener(listener);
        startZProperty.addListener(listener);

        endXProperty.addListener(listener);
        endYProperty.addListener(listener);
        endZProperty.addListener(listener);

        // invalidate initially
        listener.invalidated(startXProperty);
    }

    MyLine3D(double startX, double startY, double startZ, double endX, double endY, double endZ, double radius, Color color) {
        // Initialize the shape
        cy = new Cylinder();

        // Bind the radius to the EdgeView's radius property
        cy.radiusProperty().setValue(radius);
        // Set the shape's color and highlighting color
        PhongMaterial mat = new PhongMaterial();
        mat.diffuseColorProperty().setValue(color);
        mat.setSpecularColor(color.brighter());
        cy.setMaterial(mat);

        // Add shape to scene graph
        this.getChildren().add(cy);


        // create points of the start and end coordinates
        Point3D startPoint = new Point3D(startX, startY, startZ);
        Point3D endPoint =
                new Point3D(endX, endY, endZ);
        // center where to set the cylinders center (midpoint between start and end)
        Point3D centerOfCylinder = startPoint.midpoint(endPoint);
        // y axis point
        Point3D yAxis = new Point3D(0, 1, 0);

        // Compute a point representing the direction the shape should represent
        Point3D directionPoint = endPoint.subtract(startPoint);

        // Compute the rotation axis
        Point3D rotationAxis = directionPoint.crossProduct(yAxis);

        // angle
        double angle = -directionPoint.angle(yAxis);

        // Compute the height of the cylinder
        double heightOfCylinder = endPoint.distance(startPoint);


        // Use the computed values in order to set the cylinders properties appropriately
        cy.setRotationAxis(rotationAxis);
        cy.setRotate(angle);
        cy.setTranslateX(centerOfCylinder.getX());
        cy.setTranslateY(centerOfCylinder.getY());
        cy.setTranslateZ(centerOfCylinder.getZ());
        cy.setHeight(heightOfCylinder);

    }
}
