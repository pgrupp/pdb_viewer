package pdbview3d;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import pdbmodel.Residue;
import pdbmodel.SecondaryStructure;

import java.util.Arrays;
import java.util.List;

/**
 * Allows for a cartoon-like display of secondary structures. Which can be both alphahelices or betasheets.
 */
class MySecondaryStructureView3D extends Group {

    SecondaryStructure structure;
    DoubleProperty radius;
    ObjectProperty<Color> color;
    boolean wasComputed;
    List<Residue> listOfResidues;
    static final double BETA_SHEET_DEPTH = 0.2;
    static String lastRef;

    /**
     * Create a view of a secondary structure.
     *
     * @param structure The model structure for which a view structure should be created.
     */
    MySecondaryStructureView3D(SecondaryStructure structure) {
        this.structure = structure;
        listOfResidues = structure.getResiduesContained();
        wasComputed = false;
    }

    boolean wasComputed() {
        return wasComputed;
    }

    void compute() {
        if (wasComputed) {
            this.getChildren().clear();
        }
        wasComputed = true;
        if (structure.getSecondaryStructureType().equals(SecondaryStructure.StructureType.betasheet)) {
            // structure is alpha helix
            TriangleMesh mesh = new TriangleMesh(VertexFormat.POINT_TEXCOORD);

            PhongMaterial material = new PhongMaterial(Color.AQUAMARINE);
            material.setSpecularColor(Color.AQUAMARINE.brighter());

            // set up the mesh arrays

            // 3 coords per point four points per residue
            float[] points = new float[listOfResidues.size() * 3 * 4];
            float[] texArray = {0, 0};

            // Six ints per face, eight triangles per residue
            // (top rectangle, bottom rectangle and two sides of the depth of the two rectangles).
            int[] faces = new int[listOfResidues.size() * 6 * 8];

            // and eight triangles per residue
            int[] smoothing = new int[listOfResidues.size() * 8];

            // Compute the direction of shifting for 3D beta sheet. Need the second residue for determining
            // which direction to shift the initial coordinates. For the following residues the for loop
            // will do that based on its context.
            Residue first = listOfResidues.get(0);
            Residue second = listOfResidues.get(1);
            Point3D tarCA = getCAlpha(second);
            Point3D sourCA = getCAlpha(first);
            Point3D sourMirCB = getMirroredCBeta(first);
            // Subtract position vector of source C beta and target c alpha. then compute their crossproduct. Normalize the resulting vector and
            // multiply it by 0.3 times the length of the C-C bond
            Point3D direction = tarCA.subtract(sourCA).crossProduct(sourMirCB.subtract(sourCA)).normalize().multiply(sourCA.distance(sourMirCB)).multiply(BETA_SHEET_DEPTH);
            lastRef = "CB";


            // Set first source coordinates. This is important since otherwise for first element there is nothing to connect.
            Point3D initialBeta = getCBeta(first);
            Point3D initialMirrorBeta = getMirroredCBeta(first);
            setPoints(0, points, initialBeta, initialMirrorBeta, direction);

            // We need to take care of the open end in the sheet at the beginning (first residue between c beta,
            // mirrored cbeta, shifted cbeta and shifted mirrored cbeta
            TriangleMesh endCap = new TriangleMesh(VertexFormat.POINT_TEXCOORD);
            endCap.getTexCoords().addAll(0, 0);
            endCap.getFaces().addAll(
                    0, 0, 2, 0, 1, 0,
                    1, 0, 2, 0, 3, 0
            );
            //Copy start points from other mesh
            endCap.getPoints().addAll(points, 0, 12);
            endCap.getFaceSmoothingGroups().addAll(1, 1);
            MeshView endCapView = new MeshView(endCap);
            endCapView.setMaterial(material);
            this.getChildren().add(endCapView);

            for (int i = 1; i < listOfResidues.size(); i++) {
                Residue residue = listOfResidues.get(i);
                Residue lastResidue = listOfResidues.get(i - 1);
                // We need those for computation which points to connect.
                Point3D lastBeta = getCBeta(lastResidue);
                Point3D lastMirrorBeta = getMirroredCBeta(lastResidue);

                // These are the currently important two points. They will be connected with the last ones by two triangles
                Point3D currentBeta = getCBeta(residue);
                Point3D currentMirrorBeta = getMirroredCBeta(residue);


                // Solve minimization problem in order to connect source's and target's c betas or source's cbeta and target's mirrored c beta
                // This unwinds the planes in alpha helices significantly.
                boolean crossing = lastMirrorBeta.distance(currentBeta) + lastBeta.distance(currentMirrorBeta) <
                        lastMirrorBeta.distance(currentMirrorBeta) + lastBeta.distance(currentBeta);

                //Get the shifting direction vector for this residue's atoms
                direction = computeDirection(lastResidue, residue, crossing);

                // Set the current residues coordinates as points (the last residues points were set in the
                // last step (or for the first element in the initialization step)
                // four points per residue 3 coordinates per point
                setPoints(i * 4 * 3, points, currentBeta, currentMirrorBeta, direction);

                int positionInFaces = (i - 1) * 6 * 8;
                if (crossing) {
                    // Only use position 0, +2, +4, ... for the faces to link to point (the other 3 points are texcoords
                    // which are by default initialized with 0 which is deterministic behaviour in Java and exactly
                    // what we want.

                    if (lastRef.equals("CBM")) {
                        //Original topping

                        // First face connects sCB, sMCB and tCB
                        faces[positionInFaces] = i * 4 - 4;
                        faces[positionInFaces + 2] = i * 4 - 3;
                        faces[positionInFaces + 4] = i * 4;

                        // Second face, connects sCB, tCB and tMCB
                        faces[positionInFaces + 6] = i * 4 - 4;
                        faces[positionInFaces + 8] = i * 4;
                        faces[positionInFaces + 10] = i * 4 + 1;

                        // Shifted topping (in the following four faces each node is the shifted one)

                        // First face connects sCB, sMCB and tCB
                        faces[positionInFaces + 12] = i * 4 - 2;
                        faces[positionInFaces + 14] = i * 4 + 2;
                        faces[positionInFaces + 16] = i * 4 - 1;

                        // Second face, connects sCB, tCB and tMCB
                        faces[positionInFaces + 18] = i * 4 - 2;
                        faces[positionInFaces + 20] = i * 4 + 3;
                        faces[positionInFaces + 22] = i * 4 + 2;

                        //faces for the two sides:

                        // first side, first triangle of two
                        // source cb and shifted source cb with target mirrored cb (front and back)
                        faces[positionInFaces + 24] = i * 4 - 4;
                        faces[positionInFaces + 26] = i * 4 + 1;
                        faces[positionInFaces + 28] = i * 4 - 2;

                        //second triangle completing the first side rectangle
                        //target mirrored cb, source shifted cb target mirrored shifted cb
                        faces[positionInFaces + 30] = i * 4 + 1;
                        faces[positionInFaces + 32] = i * 4 + 3;
                        faces[positionInFaces + 34] = i * 4 - 2;


                        //second side, first triangle of two
                        //source mirrored cb, source shifted mirrored cb with target cb (front and back)
                        faces[positionInFaces + 36] = i * 4 - 3;
                        faces[positionInFaces + 38] = i * 4 - 1;
                        faces[positionInFaces + 40] = i * 4;

                        //second triangle completing the second side rectangle
                        // target cb, source shifted mirrored cb to target shifted cb
                        faces[positionInFaces + 42] = i * 4;
                        faces[positionInFaces + 44] = i * 4 - 1;
                        faces[positionInFaces + 46] = i * 4 + 2;

                    } else {

                        // First face connects sCB, sMCB and tCB
                        faces[positionInFaces] = i * 4 - 4;
                        faces[positionInFaces + 2] = i * 4;
                        faces[positionInFaces + 4] = i * 4 - 3;

                        // Second face, connects sCB, tCB and tMCB
                        faces[positionInFaces + 6] = i * 4 - 4;
                        faces[positionInFaces + 8] = i * 4 + 1;
                        faces[positionInFaces + 10] = i * 4;

                        // Shifted topping (in the following four faces each node is the shifted one)

                        // First face connects sCB, sMCB and tCB
                        faces[positionInFaces + 12] = i * 4 - 2;
                        faces[positionInFaces + 14] = i * 4 - 1;
                        faces[positionInFaces + 16] = i * 4 + 2;

                        // Second face, connects sCB, tCB and tMCB
                        faces[positionInFaces + 18] = i * 4 - 2;
                        faces[positionInFaces + 20] = i * 4 + 2;
                        faces[positionInFaces + 22] = i * 4 + 3;

                        //faces for the two sides:

                        // first side, first triangle of two
                        // source cb and shifted source cb with target mirrored cb (front and back)
                        faces[positionInFaces + 24] = i * 4 - 4;
                        faces[positionInFaces + 26] = i * 4 - 2;
                        faces[positionInFaces + 28] = i * 4 + 1;

                        //second triangle completing the first side rectangle
                        //target mirrored cb, source shifted cb target mirrored shifted cb
                        faces[positionInFaces + 30] = i * 4 + 1;
                        faces[positionInFaces + 32] = i * 4 - 2;
                        faces[positionInFaces + 34] = i * 4 + 3;


                        //second side, first triangle of two
                        //source mirrored cb, source shifted mirrored cb with target cb (front and back)
                        faces[positionInFaces + 36] = i * 4 - 3;
                        faces[positionInFaces + 38] = i * 4;
                        faces[positionInFaces + 40] = i * 4 - 1;

                        //second triangle completing the second side rectangle
                        // target cb, source shifted mirrored cb to target shifted cb
                        faces[positionInFaces + 42] = i * 4;
                        faces[positionInFaces + 44] = i * 4 + 2;
                        faces[positionInFaces + 46] = i * 4 - 1;
                    }


                } else {
                    // This is when the two mirrored and the two unmirrored points will be the outer edges

                    if (lastRef.equals("CBM")) {
                        //Original topping

                        // Same as above, but with slightly different connecting the nodes.
                        // First face connects sCB, tMCB and sMCB
                        faces[positionInFaces] = i * 4 - 4;
                        faces[positionInFaces + 2] = i * 4 + 1;
                        faces[positionInFaces + 4] = i * 4 - 3;
                        // Second face, connects sCB, tCB and tMCB
                        faces[positionInFaces + 6] = i * 4 - 4;
                        faces[positionInFaces + 8] = i * 4;
                        faces[positionInFaces + 10] = i * 4 + 1;

                        // Shifted topping (in the following four faces each node is the shifted one)

                        // First face connects sCB, tMCB and sMCB
                        faces[positionInFaces + 12] = i * 4 - 2;
                        faces[positionInFaces + 14] = i * 4 - 1;
                        faces[positionInFaces + 16] = i * 4 + 3;
                        // Second face, connects sCB, tCB and tMCB
                        faces[positionInFaces + 18] = i * 4 - 2;
                        faces[positionInFaces + 20] = i * 4 + 3;
                        faces[positionInFaces + 22] = i * 4 + 2;


                        //faces for the two sides:

                        // first side, first triangle of two
                        // source cb and shifted source cb with target cb (front and back)
                        faces[positionInFaces + 24] = i * 4 - 4;
                        faces[positionInFaces + 26] = i * 4 - 2;
                        faces[positionInFaces + 28] = i * 4;


                        //second triangle completing the first side rectangle
                        //target cb, source shifted cb target shifted cb
                        faces[positionInFaces + 30] = i * 4;
                        faces[positionInFaces + 32] = i * 4 - 2;
                        faces[positionInFaces + 34] = i * 4 + 2;


                        //second side, first triangle of two
                        //source mirrored cb, source shifted mirrored cb with target mirrored cb (front and back)
                        faces[positionInFaces + 36] = i * 4 - 3;
                        faces[positionInFaces + 38] = i * 4 + 1;
                        faces[positionInFaces + 40] = i * 4 - 1;


                        //second triangle completing the second side rectangle
                        // target mirrored cb, source shifted mirrored cb to target shifted mirrored cb
                        faces[positionInFaces + 42] = i * 4 + 1;
                        faces[positionInFaces + 44] = i * 4 + 3;
                        faces[positionInFaces + 46] = i * 4 - 1;

                    } else {
                        // LAST REF WAS CB SO SIMPLE THING TO DO IS TO SWITCH THE ORDER OF THE LAST TWO COORDINATES OF
                        // EACH FACE IN ORDER TO SWITCH ITS ORIENTATION
                        //Original topping

                        // Same as above, but with slightly different connecting the nodes.
                        // First face connects sCB, tMCB and sMCB
                        faces[positionInFaces] = i * 4 - 4;
                        faces[positionInFaces + 2] = i * 4 - 3;
                        faces[positionInFaces + 4] = i * 4 + 1;
                        // Second face, connects sCB, tCB and tMCB
                        faces[positionInFaces + 6] = i * 4 - 4;
                        faces[positionInFaces + 8] = i * 4 + 1;
                        faces[positionInFaces + 10] = i * 4;

                        // Shifted topping (in the following four faces each node is the shifted one)

                        // First face connects sCB, tMCB and sMCB
                        faces[positionInFaces + 12] = i * 4 - 2;
                        faces[positionInFaces + 14] = i * 4 + 3;
                        faces[positionInFaces + 16] = i * 4 - 1;
                        // Second face, connects sCB, tCB and tMCB
                        faces[positionInFaces + 18] = i * 4 - 2;
                        faces[positionInFaces + 20] = i * 4 + 2;
                        faces[positionInFaces + 22] = i * 4 + 3;


                        //faces for the two sides:

                        // first side, first triangle of two
                        // source cb and shifted source cb with target cb (front and back)
                        faces[positionInFaces + 24] = i * 4 - 4;
                        faces[positionInFaces + 26] = i * 4;
                        faces[positionInFaces + 28] = i * 4 - 2;


                        //second triangle completing the first side rectangle
                        //target cb, source shifted cb target shifted cb
                        faces[positionInFaces + 30] = i * 4;
                        faces[positionInFaces + 32] = i * 4 + 2;
                        faces[positionInFaces + 34] = i * 4 - 2;


                        //second side, first triangle of two
                        //source mirrored cb, source shifted mirrored cb with target mirrored cb (front and back)
                        faces[positionInFaces + 36] = i * 4 - 3;
                        faces[positionInFaces + 38] = i * 4 - 1;
                        faces[positionInFaces + 40] = i * 4 + 1;


                        //second triangle completing the second side rectangle
                        // target mirrored cb, source shifted mirrored cb to target shifted mirrored cb
                        faces[positionInFaces + 42] = i * 4 + 1;
                        faces[positionInFaces + 44] = i * 4 - 1;
                        faces[positionInFaces + 46] = i * 4 + 3;
                    }


                }

                // Set smoothing
                // original topping
                smoothing[(i - 1) * 8] = 1 << 1;
                smoothing[(i - 1) * 8 + 1] = 1 << 1;
                // shifted topping
                smoothing[(i - 1) * 8 + 2] = 1 << 1;
                smoothing[(i - 1) * 8 + 3] = 1 << 1;
                //side 1
                smoothing[(i - 1) * 8 + 4] = 1 << 2;
                smoothing[(i - 1) * 8 + 5] = 1 << 2;
                //side 2
                smoothing[(i - 1) * 8 + 6] = 1 << 2;
                smoothing[(i - 1) * 8 + 7] = 1 << 2;

            }

            // Set the necessary arrays for the full mesh of the beta sheet.
            mesh.getPoints().addAll(points);
            mesh.getFaces().addAll(faces);
            mesh.getTexCoords().addAll(texArray);
            mesh.getFaceSmoothingGroups().addAll(smoothing);
            // Convert as mesh view in order to have a node to add to the scene graph
            MeshView meshView = new MeshView(mesh);
            meshView.setDrawMode(DrawMode.FILL);
            meshView.setMaterial(material);

            this.getChildren().add(meshView);

            createArrowHead(points, material,
                    listOfResidues.get(listOfResidues.size() - 2),
                    listOfResidues.get(listOfResidues.size() - 1)
            );

        } else {
            // structure is alphahelix. Simple case.
            radius = new SimpleDoubleProperty(20);
            color = new SimpleObjectProperty<>(Color.RED);
            Residue start = structure.getFirstResidue();
            Residue end = structure.getLastResidue();
            // Start alphahelix from the starting residue's N atom and end at ending residue's C atom -> draw 3D line
            MyLine3D shape = new MyLine3D(
                    start.getNAtom().xCoordinateProperty(), start.getNAtom().yCoordinateProperty(), start.getNAtom().zCoordinateProperty(),
                    end.getCAtom().xCoordinateProperty(), end.getCAtom().yCoordinateProperty(), end.getCAtom().zCoordinateProperty(),
                    radius, color
            );
            this.getChildren().add(shape);
        }
    }

    /**
     * At the last residue of the beta sheet create a mesh structure which is pointing its direction in form of an
     * arrowhead.
     *
     * @param points     The points array of the mesh structure. The last four points are taken from there in order to
     *                   get the starting points for the arrow.
     * @param material   The material which will be set to the mesh structure.
     * @param secondLast The residue before last. Used to get the direction of the arrow.
     * @param last       The last residue in the structure. Used to get the direction of the arrow (using the C alphas).
     */
    private void createArrowHead(float[] points, PhongMaterial material, Residue secondLast, Residue last) {
        Point3D lastCAlpha = new Point3D(
                secondLast.getCAlphaAtom().xCoordinateProperty().get(),
                secondLast.getCAlphaAtom().yCoordinateProperty().get(),
                secondLast.getCAlphaAtom().zCoordinateProperty().get()
        );
        Point3D cAlpha = new Point3D(
                last.getCAlphaAtom().xCoordinateProperty().get(),
                last.getCAlphaAtom().yCoordinateProperty().get(),
                last.getCAlphaAtom().zCoordinateProperty().get()
        );
        Point3D direction = cAlpha.subtract(lastCAlpha).multiply(0.5); // calpha - lastCalpha ^= lastCalpha -> calpha

        float[] newPoints = new float[10 * 3]; // 10 points a 3 coordinates
        // Points 0:cbeta, 1:cbeta mirrored, 2: cbeta shifted, 3:cbeta mirrored shifted
        System.arraycopy(points, points.length - 12, newPoints, 0, 12);
        Point3D cBeta = new Point3D(newPoints[0], newPoints[1], newPoints[2]);
        Point3D cBetaMir = new Point3D(newPoints[3], newPoints[4], newPoints[5]);
        Point3D cBetaShif = new Point3D(newPoints[6], newPoints[7], newPoints[8]);
        Point3D cBetaShifMir = new Point3D(newPoints[9], newPoints[10], newPoints[11]);
        Point3D cAlphaShifted = cBetaShif.midpoint(cBetaShifMir);

        Point3D outerDirection = cBeta.subtract(cAlpha);


        Point3D outerPoint = cBeta.add(outerDirection);
        Point3D outerPointShifted = cBetaShif.add(outerDirection);

        Point3D outerMirroredPoint = cBetaMir.add(outerDirection.multiply(-1));
        Point3D outerMirroredPointShifted = cBetaShifMir.add(outerDirection.multiply(-1));

        Point3D arrowTip = cAlpha.add(direction);
        Point3D arrowTipShifted = cAlphaShifted.add(direction);

        //  arrow side, Point 4
        newPoints[12] = (float) outerPoint.getX();
        newPoints[13] = (float) outerPoint.getY();
        newPoints[14] = (float) outerPoint.getZ();
        // arrow side shifted, point 5
        newPoints[15] = (float) outerPointShifted.getX();
        newPoints[16] = (float) outerPointShifted.getY();
        newPoints[17] = (float) outerPointShifted.getZ();
        //arrow mirrored side, point 6
        newPoints[18] = (float) outerMirroredPoint.getX();
        newPoints[19] = (float) outerMirroredPoint.getY();
        newPoints[20] = (float) outerMirroredPoint.getZ();
        // arrow mirrored side shifted, point 7
        newPoints[21] = (float) outerMirroredPointShifted.getX();
        newPoints[22] = (float) outerMirroredPointShifted.getY();
        newPoints[23] = (float) outerMirroredPointShifted.getZ();

        //arrow tip, point 8
        newPoints[24] = (float) arrowTip.getX();
        newPoints[25] = (float) arrowTip.getY();
        newPoints[26] = (float) arrowTip.getZ();
        // arrow tip shifted, point 9
        newPoints[27] = (float) arrowTipShifted.getX();
        newPoints[28] = (float) arrowTipShifted.getY();
        newPoints[29] = (float) arrowTipShifted.getZ();

        // Define the faces
        int[] faces = new int[10 * 6]; // Ten faces: 4 for  tip sides, four for each outer and two for upper and lower
        if(lastRef.equals("CBM")) {
            // to arrow side
            faces[0] = 0;
            faces[2] = 2;
            faces[4] = 5;

            faces[6] = 0;
            faces[8] = 5;
            faces[10] = 4;

            //to mirrored arrowside
            faces[12] = 1;
            faces[14] = 6;
            faces[16] = 3;

            faces[18] = 3;
            faces[20] = 6;
            faces[22] = 7;

            // to arrow tip
            faces[24] = 4;
            faces[26] = 5;
            faces[28] = 8;

            faces[30] = 5;
            faces[32] = 9;
            faces[34] = 8;

            //to arrow tip mirrored

            faces[36] = 6;
            faces[38] = 9;
            faces[40] = 7;

            faces[42] = 9;
            faces[44] = 6;
            faces[46] = 8;

            //Arrow top (shifted)
            faces[48] = 5;
            faces[50] = 7;
            faces[52] = 9;

            //Arrow lower
            faces[54] = 6;
            faces[56] = 4;
            faces[58] = 8;
        } else {
            // Last ref was CBeta therfore take the same faces as above, but tunr them (take the other side, by
            // flipping the las two numbers in each face)
            // to arrow side
            faces[0] = 0;
            faces[2] = 5;
            faces[4] = 2;

            faces[6] = 0;
            faces[8] = 4;
            faces[10] = 5;

            //to mirrored arrowside
            faces[12] = 1;
            faces[14] = 3;
            faces[16] = 6;

            faces[18] = 3;
            faces[20] = 7;
            faces[22] = 6;

            // to arrow tip
            faces[24] = 4;
            faces[26] = 8;
            faces[28] = 5;

            faces[30] = 5;
            faces[32] = 8;
            faces[34] = 9;

            //to arrow tip mirrored

            faces[36] = 6;
            faces[38] = 7;
            faces[40] = 9;

            faces[42] = 9;
            faces[44] = 8;
            faces[46] = 6;

            //Arrow top (shifted)
            faces[48] = 5;
            faces[50] = 9;
            faces[52] = 7;

            //Arrow lower
            faces[54] = 6;
            faces[56] = 8;
            faces[58] = 4;
        }

        TriangleMesh arrowHead = new TriangleMesh(VertexFormat.POINT_TEXCOORD);
        arrowHead.getPoints().addAll(newPoints);
        arrowHead.getFaces().addAll(faces);
        arrowHead.getTexCoords().addAll(0,0);
        arrowHead.getFaceSmoothingGroups().addAll(0,0,0,0,0,0,0,0,0,0);
        MeshView arrowHeadMeshView = new MeshView(arrowHead);
        arrowHeadMeshView.setMaterial(material);
        this.getChildren().add(arrowHeadMeshView);
    }

    /**
     * Set points in the points array for the triangle mesh.
     *
     * @param idx            The index in the points array where to put the points.
     * @param points         The points array which will be used to store the points in. Will be in the form for the
     *                       triangle mesh structure.
     * @param cBeta          The c Beta point to put in.
     * @param mirroredCBeta  The mirrored point of C beta (should be mirrored at the residue's c alpha.
     * @param directionShift The shift direction (only the direction, not the location vector) with appropriate length
     *                       where to put the shifted c beta and shifted mirrored c beta.
     */
    private void setPoints(int idx, float[] points, Point3D cBeta, Point3D mirroredCBeta, Point3D directionShift) {
        // Set C Beta
        points[idx] = (float) cBeta.getX();
        points[idx + 1] = (float) cBeta.getY();
        points[idx + 2] = (float) cBeta.getZ();
        // Set mirrored C Beta
        points[idx + 3] = (float) mirroredCBeta.getX();
        points[idx + 4] = (float) mirroredCBeta.getY();
        points[idx + 5] = (float) mirroredCBeta.getZ();
        // Set shifted C Beta. (Shift both points in order to have a plane depth -> 3D effect)
        points[idx + 6] = (float) cBeta.add(directionShift).getX();
        points[idx + 7] = (float) cBeta.add(directionShift).getY();
        points[idx + 8] = (float) cBeta.add(directionShift).getZ();
        // Set shifted mirrored C Beta
        points[idx + 9] = (float) mirroredCBeta.add(directionShift).getX();
        points[idx + 10] = (float) mirroredCBeta.add(directionShift).getY();
        points[idx + 11] = (float) mirroredCBeta.add(directionShift).getZ();
    }

    /**
     * Get the direction (with appropriate length) to shift the target's points (C alpha, C beta and mirrored C beta)
     * in 3D space in order to create points to reference to in order to get a beta sheet with a depth.
     *
     * @param source   the last residue
     * @param target   The residue to get the shift for.
     * @param crossing Are the two C Betas of the two residues on 'the same side of the backbone' or crossing. Crossing
     *                 means that Cbeta of the source is connected to mirrored C Beta of the target and vice versa for
     *                 mirrored CBeta of the source.
     * @return The direction to shift the three reference points of the target by.
     */
    private Point3D computeDirection(Residue source, Residue target, boolean crossing) {
        Point3D result;
        Point3D sourceAlpha = getCAlpha(source);

        // Especially for when the residue is 'crossing' we need to switch the crossproduct's direction by -1
        // or referencing the mirrored CB instead of CB
        Point3D targetAlpha = getCAlpha(target);
        Point3D targetBeta = getCBeta(target);
        Point3D targetMirrorBeta = getMirroredCBeta(target);

        Point3D dest = sourceAlpha.subtract(targetAlpha);
        Point3D ref;

        // Depending on the last reference point (either C beta or CBeta mirrored) we need to use the correct one
        // in order to get the shift right and not crossing.
        if (crossing) {
            if (lastRef.equals("CB")) {
                // Last ref was CBeta so we need to take the mirrored target CB point since we are 'crossing'
                ref = targetMirrorBeta.subtract(targetAlpha);
                lastRef = "CBM";
            } else {
                ref = targetBeta.subtract(targetAlpha);
                lastRef = "CB";
            }
        } else {
            if (lastRef.equals("CB")) {
                // Last ref was CBeta so we need to take the CBeta point fo the target since we are NOT 'crossing'
                ref = targetBeta.subtract(targetAlpha);
            } else {
                ref = targetMirrorBeta.subtract(targetAlpha);
            }
        }
        // Get the perpendicular vectore of the CB CB mirrored line and the CAlpha source CALpha target line -> this is the shift
        // But always neccessaryly in the correct direction.
        result = dest.crossProduct(ref).normalize().multiply(sourceAlpha.distance(targetAlpha)).multiply(BETA_SHEET_DEPTH);
        return result;
    }

    /**
     * Get the mirrored point of the given residue's c beta.
     *
     * @param residue Reference residue
     * @return Point which is C beta mirrored at C alpha.
     */
    private Point3D getMirroredCBeta(Residue residue) {
        Point3D alpha = getCAlpha(residue);
        Point3D beta = getCBeta(residue);
        return beta.subtract(alpha).multiply(-1).add(alpha);
    }

    /**
     * Get a points C alpha in 3D space.
     *
     * @param residue The residue to get C alpha from.
     * @return Position in 3D space of C alpha of the given residue.
     */
    private Point3D getCAlpha(Residue residue) {
        return new Point3D(residue.getCAlphaAtom().xCoordinateProperty().get(),
                residue.getCAlphaAtom().yCoordinateProperty().get(),
                residue.getCAlphaAtom().zCoordinateProperty().get());
    }

    /**
     * Get the point in 3D space of C Beta of the given residue.
     *
     * @param residue The residue to get C beta from.
     * @return Position in 3D space of C beta of the given residue.
     */
    private Point3D getCBeta(Residue residue) {
        return new Point3D(residue.getCBetaAtom().xCoordinateProperty().get(),
                residue.getCBetaAtom().yCoordinateProperty().get(),
                residue.getCBetaAtom().zCoordinateProperty().get());
    }
}
