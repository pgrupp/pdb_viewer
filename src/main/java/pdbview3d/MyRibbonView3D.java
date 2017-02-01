package pdbview3d;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import pdbmodel.Residue;

/**
 * Mesh wrapper for ribbon view of residues.
 */
public class MyRibbonView3D extends Group {
    Residue modelSource;
    Residue modelTarget;
    TriangleMesh mesh;
    static Residue lastResidue = null;

    MyRibbonView3D(Residue residue) {
        if (MyRibbonView3D.lastResidue != null) {
            this.modelSource = lastResidue;
            this.modelTarget = residue;
            Point3D sourceAlpha = new Point3D(lastResidue.getCAlphaAtom().xCoordinateProperty().get(),
                    lastResidue.getCAlphaAtom().yCoordinateProperty().get(),
                    lastResidue.getCAlphaAtom().zCoordinateProperty().get());
            Point3D sourceBeta = new Point3D(lastResidue.getCBetaAtom().xCoordinateProperty().get(),
                    lastResidue.getCBetaAtom().yCoordinateProperty().get(),
                    lastResidue.getCBetaAtom().zCoordinateProperty().get());
            Point3D sourceMirrorBeta = sourceBeta.subtract(sourceAlpha).multiply(-1).add(sourceAlpha);
            Point3D targetAlpha = new Point3D(residue.getCAlphaAtom().xCoordinateProperty().get(),
                    residue.getCAlphaAtom().yCoordinateProperty().get(),
                    residue.getCAlphaAtom().zCoordinateProperty().get());
            Point3D targetBeta = new Point3D(residue.getCBetaAtom().xCoordinateProperty().get(),
                    residue.getCBetaAtom().yCoordinateProperty().get(),
                    residue.getCBetaAtom().zCoordinateProperty().get());
            Point3D targetMirrorBeta = targetBeta.subtract(targetAlpha).multiply(-1).add(targetAlpha);


            mesh = new TriangleMesh(VertexFormat.POINT_TEXCOORD);

            float[] points = {
                    (float) sourceBeta.getX(), (float) sourceBeta.getY(), (float) sourceBeta.getZ(),
                    (float) sourceMirrorBeta.getX(), (float) sourceMirrorBeta.getY(), (float) sourceMirrorBeta.getZ(),
                    (float) targetBeta.getX(), (float) targetBeta.getY(), (float) targetBeta.getZ(),
                    (float) targetMirrorBeta.getX(), (float) targetMirrorBeta.getY(), (float) targetMirrorBeta.getZ(),
            };

            float[] texArray = {0, 0};

            int[] faces;

            // Solve minimization problem in order to connect source's and target's c betas or source's cbeta and target's mirrored c beta
            // This unwinds the planes in alphahelices significantly.
            if ( sourceMirrorBeta.distance(targetBeta) + sourceBeta.distance(targetMirrorBeta) <
                    sourceMirrorBeta.distance(targetMirrorBeta) + sourceBeta.distance(targetBeta)) {
                // This is when mirrored and unmirrored  point wil each be the outer edge
                faces = new int[] {
                        0, 0, 1, 0, 2, 0, // First face connects sCB, sMCB and tCB
                        0, 0, 2, 0, 1, 0, // The first face's back
                        // (in order to be visible from both sides (when rotating)), connects sCB, tCB and sMCB
                        0, 0, 2, 0, 3, 0, // Second face, connects sCB, tCB and tMCB
                        0, 0, 3, 0, 2, 0  // Second face's back (same as above), connects sCB, tMCB and tCB
                };
            } else {
                // This is when the two mirrored and the two unmirrored points will be the outer edges
                faces = new int[] {
                        0, 0, 1, 0, 3, 0, // First face connects sCB, sMCB and tMCB
                        0, 0, 3, 0, 1, 0, // The first face's back
                        // (in order to be visible from both sides (when rotating)), connects sCB, tMCB and sMCB
                        0, 0, 3, 0, 2, 0, // Second face, connects sCB, tMCB and tCB
                        0, 0, 2, 0, 3, 0  // Second face's back (same as above), connects sCB, tCB and tMCB
                };
            }

            int[] smoothing = {
                    1, 2, 1, 2
            };
            mesh.getPoints().addAll(points);
            mesh.getFaces().addAll(faces);
            mesh.getTexCoords().addAll(texArray);
            mesh.getFaceSmoothingGroups().addAll(smoothing);

            MeshView meshView = new MeshView(mesh);
            meshView.setDrawMode(DrawMode.FILL);

            PhongMaterial mat = new PhongMaterial(Color.MEDIUMAQUAMARINE);
            mat.setSpecularColor(Color.MEDIUMAQUAMARINE.brighter());
            meshView.setMaterial(mat);
            this.getChildren().add(meshView);
        }

        MyRibbonView3D.lastResidue = residue;
    }

    public static void reset() {
        lastResidue = null;
    }
}
