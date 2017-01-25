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

    public MyRibbonView3D(Residue residue) {
        if (MyRibbonView3D.lastResidue != null) {
            this.modelSource = lastResidue;
            this.modelTarget = residue;
            Point3D sourceAlpha = new Point3D(lastResidue.getCAlphaAtom().xCoordinateProperty().get(),
                    lastResidue.getCAlphaAtom().yCoordinateProperty().get(),
                    lastResidue.getCAlphaAtom().zCoordinateProperty().get());
            Point3D sourceBeta = new Point3D(lastResidue.getCBetaAtom().xCoordinateProperty().get(),
                    lastResidue.getCBetaAtom().yCoordinateProperty().get(),
                    lastResidue.getCBetaAtom().zCoordinateProperty().get());
            //Point3D sourceMirrorBeta = sourceBeta.subtract(sourceAlpha).multiply(-1).add(sourceAlpha);
            Point3D targetAlpha = new Point3D(residue.getCAlphaAtom().xCoordinateProperty().get(),
                    residue.getCAlphaAtom().yCoordinateProperty().get(),
                    residue.getCAlphaAtom().zCoordinateProperty().get());
            Point3D targetBeta = new Point3D(residue.getCBetaAtom().xCoordinateProperty().get(),
                    residue.getCBetaAtom().yCoordinateProperty().get(),
                    residue.getCBetaAtom().zCoordinateProperty().get());
            //Point3D targetMirrorBeta = targetBeta.subtract(targetAlpha).multiply(-1).add(targetAlpha);


            mesh = new TriangleMesh(VertexFormat.POINT_TEXCOORD);

            float[] points = {(float) sourceAlpha.getX(), (float) sourceAlpha.getY(), (float) sourceAlpha.getZ(),
                    (float) sourceBeta.getX(), (float) sourceBeta.getY(), (float) sourceBeta.getZ(),
                    (float) targetAlpha.getX(), (float) targetAlpha.getY(), (float) targetAlpha.getZ(),
                    (float) targetBeta.getX(), (float) targetBeta.getY(), (float) targetBeta.getZ(),
            };

            float[] texArray = {0, 0};

            int[] faces = {
                    0, 0, 1, 0, 3, 0,
                    0, 0, 3, 0, 1, 0,
                    3, 0, 2, 0, 0, 0,
                    0, 0, 2, 0, 3, 0,
            };

            int[] smoothing = {
                    1,2,1,2
            };
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
        }

        MyRibbonView3D.lastResidue = residue;
    }

    public static void reset() {
        lastResidue = null;
    }
}
