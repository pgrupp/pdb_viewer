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
        wasComputed = true;
        if (structure.getSecondaryStructureType().equals(SecondaryStructure.StructureType.betasheet)) {
            // structure is alpha helix
            TriangleMesh mesh = new TriangleMesh(VertexFormat.POINT_TEXCOORD);

            // set up the mesh arrays

            // 3 coords per point four points per residue
            float[] points = new float[listOfResidues.size() * 3 * 4];
            float[] texArray = {0, 0};

            // Six ints per face two faces for each triangle (front and back), eight triangles per residue
            // (top rectangle, bottom rectangle and two sides of the depth of the two rectangles).
            int[] faces = new int[listOfResidues.size() * 6 * 8];

            // two faces per triangle (front and back) and eight triangles per residue
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
                    // Only use position 0, +2, +4, ... for the faces to link to point (the other 3 point are texcoords
                    // which are by default initialized with 0 which is deterministic behaviour in Java and exactly
                    // what we want.

                    //Original topping

                    if (lastRef.equals("CBM")) {
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

                    //Original topping

                    // Same as above, but with slightly different connecting the nodes.
                    // First face connects sCB, sMCB and tMCB
                    faces[positionInFaces] = i * 4 - 4;
                    faces[positionInFaces + 2] = i * 4 - 3;
                    faces[positionInFaces + 4] = i * 4 + 1;
                    // Second face, connects sCB, tMCB and tCB
                    faces[positionInFaces + 6] = i * 4 - 4;
                    faces[positionInFaces + 8] = i * 4 + 1;
                    faces[positionInFaces + 10] = i * 4;

                    // Shifted topping (in the following four faces each node is the shifted one)

                    // First face connects sCB, sMCB and tMCB
                    faces[positionInFaces + 12] = i * 4 - 2;
                    faces[positionInFaces + 14] = i * 4 - 1;
                    faces[positionInFaces + 16] = i * 4 + 3;
                    // Second face, connects sCB, tMCB and tCB
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
                    faces[positionInFaces + 38] = i * 4 + 2;
                    faces[positionInFaces + 40] = i * 4 - 1;


                    //second triangle completing the second side rectangle
                    // target mirrored cb, source shifted mirrored cb to target shifted mirrored cb
                    faces[positionInFaces + 42] = i * 4 + 1;
                    faces[positionInFaces + 44] = i * 4 + 2;
                    faces[positionInFaces + 46] = i * 4 - 1;


                }

                //if (crossing) {
                System.out.println("crossing");
                // original topping
                smoothing[(i - 1) * 8] = 1 << 1;
                smoothing[(i - 1) * 8 + 1] = 1 << 1;

                smoothing[(i - 1) * 8 + 2] = 1 << 2;
                smoothing[(i - 1) * 8 + 3] = 1 << 2;
                // shifted topping
                smoothing[(i - 1) * 8 + 4] = 1 << 3;
                smoothing[(i - 1) * 8 + 5] = 1 << 3;

                smoothing[(i - 1) * 8 + 6] = 1 << 4;
                smoothing[(i - 1) * 8 + 7] = 1 << 4;

//                // first side
//                smoothing[(i - 1) * 8 * 2 + 8] = 1<<3;
//                smoothing[(i - 1) * 8 * 2 + 9] = 1<<4;
//                smoothing[(i - 1) * 8 * 2 + 10] = 1<<3;
//                smoothing[(i - 1) * 8 * 2 + 11] = 1<<4;
//                // second side
//                smoothing[(i - 1) * 8 * 2 + 12] = 1<<3;
//                smoothing[(i - 1) * 8 * 2 + 13] = 1<<4;
//                smoothing[(i - 1) * 8 * 2 + 14] = 1<<3;
//                smoothing[(i - 1) * 8 * 2 + 15] = 1<<4;
//                } else {
//                    System.out.println("not crossing");
//                    // Original topping
//                    smoothing[(i - 1) * 8 * 2] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 1] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 2] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 3] = 0;
//                    //Shifted topping
//                    smoothing[(i - 1) * 8 * 2 + 4] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 5] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 6] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 7] = 0;
//
//                    // first side
//                    smoothing[(i - 1) * 8 * 2 + 8] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 9] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 10] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 11] = 0;
//                    // second side
//                    smoothing[(i - 1) * 8 * 2 + 12] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 13] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 14] = 0;
//                    smoothing[(i - 1) * 8 * 2 + 15] = 0;
//                }

                // smoothing        1, 2, 1, 2
            }

            mesh.getPoints().addAll(points);
            mesh.getFaces().addAll(faces);
            mesh.getTexCoords().addAll(texArray);
            mesh.getFaceSmoothingGroups().addAll(smoothing);

            MeshView meshView = new MeshView(mesh);
            meshView.setDrawMode(DrawMode.FILL);

            PhongMaterial mat = new PhongMaterial(Color.GREEN);
            mat.setSpecularColor(Color.GREEN.brighter());
            meshView.setMaterial(mat);
            this.getChildren().add(meshView);

        } else {
            // structure is betasheet
            radius = new SimpleDoubleProperty(20);
            color = new SimpleObjectProperty<>(Color.RED);
            Residue start = structure.getFirstResidue();
            Residue end = structure.getLastResidue();
            // Start alphahelix from the starting residue's N atom and end at ending residue's C atom
            MyLine3D shape = new MyLine3D(
                    start.getNAtom().xCoordinateProperty(), start.getNAtom().yCoordinateProperty(), start.getNAtom().zCoordinateProperty(),
                    end.getCAtom().xCoordinateProperty(), end.getCAtom().yCoordinateProperty(), end.getCAtom().zCoordinateProperty(),
                    radius, color
            );
            this.getChildren().add(shape);
        }
    }

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

        if (crossing) {
            if (lastRef.equals("CB")) {
                ref = targetMirrorBeta.subtract(targetAlpha);
                lastRef = "CBM";
            } else {
                ref = targetBeta.subtract(targetAlpha);
                lastRef = "CB";
            }
        } else {
            if (lastRef.equals("CB")) {
                ref = targetBeta.subtract(targetAlpha);
            } else {
                ref = targetMirrorBeta.subtract(targetAlpha);
            }
        }
        result = dest.crossProduct(ref).normalize().multiply(sourceAlpha.distance(targetAlpha)).multiply(BETA_SHEET_DEPTH);

        return result;
    }

    private Point3D getMirroredCBeta(Residue residue) {
        Point3D alpha = getCAlpha(residue);
        Point3D beta = getCBeta(residue);
        return beta.subtract(alpha).multiply(-1).add(alpha);
    }

    private Point3D getCAlpha(Residue residue) {
        return new Point3D(residue.getCAlphaAtom().xCoordinateProperty().get(),
                residue.getCAlphaAtom().yCoordinateProperty().get(),
                residue.getCAlphaAtom().zCoordinateProperty().get());
    }

    private Point3D getCBeta(Residue residue) {
        return new Point3D(residue.getCBetaAtom().xCoordinateProperty().get(),
                residue.getCBetaAtom().yCoordinateProperty().get(),
                residue.getCBetaAtom().zCoordinateProperty().get());
    }
}
