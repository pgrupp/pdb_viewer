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

    boolean wasComputed(){
        return wasComputed;
    }

    void compute(){
        wasComputed = true;
        if (structure.getSecondaryStructureType().equals(SecondaryStructure.StructureType.betasheet)) {
            // structure is alpha helix
            TriangleMesh mesh = new TriangleMesh(VertexFormat.POINT_TEXCOORD);

            // set up the mesh arrays
            float[] points = new float[listOfResidues.size() * 6];
            float[] texArray = {0, 0};
            int[] faces = new int[listOfResidues.size() * 4 * 6]; // Four faces for each residue (two triangles with front and back) and six elements per point
            int[] smoothing = new int[listOfResidues.size() * 4];

            // Set first source coordinates
            Residue first = listOfResidues.get(0);
            Point3D initialBeta = getCBeta(first);
            Point3D initialMirrorBeta = getMirroredCBeta(first);
            setPoints(0, points, initialBeta, initialMirrorBeta); // This is important since for first element there is nothing to connect.

            for (int i = 1; i < listOfResidues.size(); i++) {
                Residue residue = listOfResidues.get(i);
                Residue lastResidue = listOfResidues.get(i - 1);
                // We need those for computation which points to connect.
                Point3D sourceBeta = getCBeta(lastResidue);
                Point3D sourceMirrorBeta = getMirroredCBeta(lastResidue);

                // These are the currently important two points. They will be connected with the last ones by two triangles
                Point3D currentBeta = getCBeta(residue);
                Point3D currentMirrorBeta = getMirroredCBeta(residue);

                // Set the current residues coordinates as points (the last residues points were set in the
                // last step (or for the first element in the initialization step)
                setPoints(i * 6, points, currentBeta, currentMirrorBeta);

//                    float[] points = {
//                            (float) sourceBeta.getX(), (float) sourceBeta.getY(), (float) sourceBeta.getZ(),
//                            (float) sourceMirrorBeta.getX(), (float) sourceMirrorBeta.getY(), (float) sourceMirrorBeta.getZ(),

//                            (float) currentBeta.getX(), (float) currentBeta.getY(), (float) currentBeta.getZ(),
//                            (float) currentMirrorBeta.getX(), (float) currentMirrorBeta.getY(), (float) currentMirrorBeta.getZ(),
//                    };


                // Solve minimization problem in order to connect source's and target's c betas or source's cbeta and target's mirrored c beta
                // This unwinds the planes in alpha helices significantly.
                boolean crossing = sourceMirrorBeta.distance(currentBeta) + sourceBeta.distance(currentMirrorBeta) <
                        sourceMirrorBeta.distance(currentMirrorBeta) + sourceBeta.distance(currentBeta);
                if(crossing) {
                    int positionInFaces = (i - 1) * 6 * 4;
                    // Only use position 0, +2, +4, ... for the faces to link to point (the other 3 point are texcoords
                    // which are by default initialized with 0 which is deterministic behaviour in Java and exactly
                    // what we want.
                    // First face connects sCB, sMCB and tCB
                    faces[positionInFaces] = i * 2 - 2;
                    faces[positionInFaces + 2] = i * 2 - 1;
                    faces[positionInFaces + 4] = i * 2;
                    // The first face's back (in order to be visible from both sides (when rotating)),
                    // connects sCB, tCB and sMCB
                    faces[positionInFaces + 6] = i * 2 - 2;
                    faces[positionInFaces + 8] = i * 2;
                    faces[positionInFaces + 10] = i * 2 - 1;
                    // Second face, connects sCB, tCB and tMCB
                    faces[positionInFaces + 12] = i * 2 - 2;
                    faces[positionInFaces + 14] = i * 2;
                    faces[positionInFaces + 16] = i * 2 + 1;
                    // Second face's back (same as above), connects sCB, tMCB and tCB
                    faces[positionInFaces + 18] = i * 2 - 2;
                    faces[positionInFaces + 20] = i * 2 + 1;
                    faces[positionInFaces + 22] = i * 2;


                    // This is when mirrored and unmirrored  point wil each be the outer edge
//                        faces = new int[]{
//                                0, 0, 1, 0, 2, 0, // First face connects sCB, sMCB and tCB
//                                0, 0, 2, 0, 1, 0, // The first face's back
//                                // (in order to be visible from both sides (when rotating)), connects sCB, tCB and sMCB
//                                0, 0, 2, 0, 3, 0, // Second face, connects sCB, tCB and tMCB
//                                0, 0, 3, 0, 2, 0  // Second face's back (same as above), connects sCB, tMCB and tCB
//                        };
                } else {
                    // This is when the two mirrored and the two unmirrored points will be the outer edges
                    int positionInFaces = (i - 1) * 6 * 4;
                    // Same as above, but with slightly different connecting the nodes.
                    // First face connects sCB, sMCB and tMCB
                    faces[positionInFaces] = i * 2 - 2;
                    faces[positionInFaces + 2] = i * 2 - 1;
                    faces[positionInFaces + 4] = i * 2 + 1;
                    // The first face's back (in order to be visible from both sides (when rotating))
                    // connects sCB, tMCB and sMCB
                    faces[positionInFaces + 6] = i * 2 - 2;
                    faces[positionInFaces + 8] = i * 2 + 1;
                    faces[positionInFaces + 10] = i * 2 - 1;
                    // Second face, connects sCB, tMCB and tCB
                    faces[positionInFaces + 12] = i * 2 - 2;
                    faces[positionInFaces + 14] = i * 2 + 1;
                    faces[positionInFaces + 16] = i * 2;
                    // Second face's back (same as above), connects sCB, tCB and tMCB
                    faces[positionInFaces + 18] = i * 2 - 2;
                    faces[positionInFaces + 20] = i * 2;
                    faces[positionInFaces + 22] = i * 2 + 1;
//                        faces = new int[]{
//                                0, 0, 1, 0, 3, 0, // First face connects sCB, sMCB and tMCB
//                                0, 0, 3, 0, 1, 0, // The first face's back
//                                // (in order to be visible from both sides (when rotating)), connects sCB, tMCB and sMCB
//                                0, 0, 3, 0, 2, 0, // Second face, connects sCB, tMCB and tCB
//                                0, 0, 2, 0, 3, 0  // Second face's back (same as above), connects sCB, tCB and tMCB
//                        };
                }

                if(crossing) {
                    System.out.println("crossing");
                    smoothing[(i - 1) * 4] = 0;
                    smoothing[(i - 1) * 4 + 1] = 0;
                    smoothing[(i - 1) * 4 + 2] = 0;
                    smoothing[(i - 1) * 4 + 3] = 0;
                } else {
                    System.out.println("not crossing");
                    smoothing[(i - 1) * 4] = 0;
                    smoothing[(i - 1) * 4 + 1] = 0;
                    smoothing[(i - 1) * 4 + 2] = 0;
                    smoothing[(i - 1) * 4 + 3] = 0;
                }

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

    private void setPoints(int idx, float[] points, Point3D sourceBeta, Point3D sourceMirrorBeta) {
        // Set C Beta
        points[idx] = (float) sourceBeta.getX();
        points[idx + 1] = (float) sourceBeta.getY();
        points[idx + 2] = (float) sourceBeta.getZ();
        // Set mirrored C Beta
        points[idx + 3] = (float) sourceMirrorBeta.getX();
        points[idx + 4] = (float) sourceMirrorBeta.getY();
        points[idx + 5] = (float) sourceMirrorBeta.getZ();
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
