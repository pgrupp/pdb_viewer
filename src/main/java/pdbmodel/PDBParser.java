package pdbmodel;

import javafx.geometry.Point3D;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Parser for PDB files.
 *
 * @author Patrick Grupp
 */
public class PDBParser {

    private enum Status {header, remarks, helix, betasheet, atom, term}

    private static final int ATOM_DISTANCE_FACTOR = 20;

    public static void parse(PDBEntry pdbEntry, File file) throws Exception {

        if (!file.exists()) {
            throw new FileNotFoundException("The pdbFile " + file.getPath() + " does not exist.");
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String curr;
        ArrayList<Atom> atomArrayList = new ArrayList<>();
        ArrayList<Pair<String, String>> helices = new ArrayList<>();
        ArrayList<Pair<String, String>> betaSheets = new ArrayList<>();
        Status status = Status.header;
        while ((curr = reader.readLine()) != null) {
            status = processLine(curr, pdbEntry, atomArrayList, helices, betaSheets);
            if (status.equals(Status.term))
                break;
        }

        postProcess(pdbEntry, atomArrayList, helices, betaSheets);

        if (pdbEntry.nodesProperty().size() == 0) {
            throw new Exception("No nodes were read from PDB file. Exiting.");
        }
    }

    private static Status processLine(String line, PDBEntry pdbEntry, ArrayList<Atom> atoms,
                                      ArrayList<Pair<String, String>> helices,
                                      ArrayList<Pair<String, String>> betaSheets) {
        if (line.startsWith("HEADER")) {
            pdbEntry.titleProperty().setValue(line.substring(10, 50).trim());
            pdbEntry.pdbCodeProperty().setValue(line.substring(62, 66).trim());

            return Status.header;
        } else if (line.startsWith("HELIX")) {
            String startResSeqNum = line.substring(21, 26);
            String endResSeqNum = line.substring(33, 38);
            helices.add(new Pair<>(startResSeqNum, endResSeqNum));
            return Status.helix;
        } else if (line.startsWith("SHEET")) {
            String startResSeqNum = line.substring(22, 27);
            String endResSeqNum = line.substring(33, 38);
            betaSheets.add(new Pair<>(startResSeqNum, endResSeqNum));
            return Status.betasheet;
        } else if (line.startsWith("ATOM")) {

            String atomName = line.substring(12, 16).trim();
            if (atomName.equals("CA") || atomName.equals("CB") || atomName.equals("C") || atomName.equals("N")
                    || atomName.equals("O")) {
                double x = Double.parseDouble(line.substring(30, 38).trim()) * ATOM_DISTANCE_FACTOR;
                double y = Double.parseDouble(line.substring(38, 46).trim()) * ATOM_DISTANCE_FACTOR;
                double z = Double.parseDouble(line.substring(46, 54).trim()) * ATOM_DISTANCE_FACTOR;
                String residueName = line.substring(17, 20).trim();
                String resSeqNum = line.substring(22, 27).trim();

                atoms.add(new Atom(x, y, z, atomName, resSeqNum + "$" + residueName));
            }
            return Status.atom;
        } else if (line.startsWith("TER"))
            return Status.term;
        else
            return Status.remarks;
    }

    /**
     * After having read in all the necessary lines from the PDB file. Use the data structures built up in order
     * to construct a proper model of the information.
     *
     * @param pdbEntry      The containing element holding all information of a pdb file (not fully built yet,
     *                      may be empty).
     * @param atomArrayList List of all atoms in PDB file.
     * @param helices       List of all helices in PDB file as {@link Pair} of Strings with starting and ending
     *                      sequence residue number as key and value.
     * @param betaSheets    List of all beta sheets in PDB file as {@link Pair} of Strings with starting and
     *                      ending sequence residue number as key and value.
     */
    private static void postProcess(PDBEntry pdbEntry, ArrayList<Atom> atomArrayList,
                                    ArrayList<Pair<String, String>> helices,
                                    ArrayList<Pair<String, String>> betaSheets) {
        Residue currentResidue = null;
        for (Atom a : atomArrayList) {
            String residueSeqNum = a.textProperty().getValue().split("\\$")[0];
            String residueName = a.textProperty().getValue().split("\\$")[1];
            if (currentResidue == null) {
                currentResidue = new Residue(residueSeqNum, residueName);
            } else if (!currentResidue.getResNum().equals(residueSeqNum)) {
                pdbEntry.addResidue(currentResidue);
                // If the now completed Residue is Glycine, add an interpolated C beta atom to the residue.
                if (currentResidue.getAminoAcid().equals(Residue.AminoAcid.GLY)) {
                    handleGlycine(currentResidue);
                }
                addToSceneGraph(pdbEntry, currentResidue);
                currentResidue = new Residue(residueSeqNum, residueName);
            }
            switch (a.chemicalElementProperty().getValue().toString()) {
                case "CA":
                    currentResidue.setCAlphaAtom(a);
                    break;
                case "CB":
                    currentResidue.setCBetaAtom(a);
                    break;
                case "C":
                    currentResidue.setCAtom(a);
                    break;
                case "N":
                    currentResidue.setNAtom(a);
                    break;
                case "O":
                    currentResidue.setOAtom(a);
                    break;
            }
            a.residueProperty().setValue(currentResidue);
        }
        if (currentResidue != null) {
            pdbEntry.addResidue(currentResidue);
            // If last amino acid is glycine, add an interpolated C beta atom to the residue
            if (currentResidue.getAminoAcid().equals(Residue.AminoAcid.GLY)) {
                handleGlycine(currentResidue);
            }
            addToSceneGraph(pdbEntry, currentResidue);
        }

        for (Pair<String, String> struc : helices) {
            handleSecondaryStructures(pdbEntry, struc, SecondaryStructure.StructureType.alphahelix);
        }

        for (Pair<String, String> struc : betaSheets) {
            handleSecondaryStructures(pdbEntry, struc, SecondaryStructure.StructureType.betasheet);
        }

    }

    private static void addToSceneGraph(PDBEntry pdbEntry, Residue currentResidue) {
        pdbEntry.addNode(currentResidue.getCAtom());
        pdbEntry.addNode(currentResidue.getCBetaAtom());
        pdbEntry.addNode(currentResidue.getCAlphaAtom());
        pdbEntry.addNode(currentResidue.getNAtom());
        pdbEntry.addNode(currentResidue.getOAtom());
    }

    /**
     * Compute an interpolated position for C beta of Glycine, which does not have a C beta atom.
     *
     * @param residue The glycine residue to be handled.
     */
    private static void handleGlycine(Residue residue) {
        Atom ca = residue.getCAlphaAtom();
        Atom c = residue.getCAtom();
        Atom n = residue.getNAtom();

        Point3D caPoint = new Point3D(ca.xCoordinateProperty().get(), ca.yCoordinateProperty().get(),
                ca.zCoordinateProperty().get());
        Point3D cPoint = new Point3D(c.xCoordinateProperty().get(), c.yCoordinateProperty().get(),
                c.zCoordinateProperty().get());
        Point3D nPoint = new Point3D(n.xCoordinateProperty().get(), n.yCoordinateProperty().get(),
                n.zCoordinateProperty().get());

        // Get two vectors spanning the plane on N - Calpha and Calpha - C and create a point on its normal vector.
        Point3D ncaVec = nPoint.subtract(caPoint);
        Point3D ccaVec = cPoint.subtract(caPoint);
        Point3D axis = ncaVec.crossProduct(ccaVec).normalize().multiply(ATOM_DISTANCE_FACTOR);
        Point3D result = caPoint.add(axis);
        residue.setCBetaAtom(new Atom(result.getX(), result.getY(), result.getZ(), "CB", ""));
        residue.getCBetaAtom().residueProperty().setValue(residue);
    }

    /**
     * Handle the read secondary structures and add them to the {@link PDBEntry} model.
     *
     * @param pdbEntry The model to be manipulated.
     * @param struc    The structure read from PDB file. Pair of Strings key being begin residue Number and value being end residue Number.
     * @param type     The type of the {@link SecondaryStructure}. Can either be alphahelix or betasheet.
     */
    private static void handleSecondaryStructures(PDBEntry pdbEntry, Pair<String, String> struc, SecondaryStructure.StructureType type) {
        SecondaryStructure current = null;
        for (Residue res : pdbEntry.residuesProperty()) {
            if (current == null && res.getResNum().equals(struc.getKey())) {
                current = new SecondaryStructure(type);
                current.addResidue(res);
            } else if (current != null && !res.getResNum().equals(struc.getValue())) {
                current.addResidue(res);
            } else if (current != null && res.getResNum().equals(struc.getValue())) {
                current.addResidue(res);
                break;
            }
        }
        if (current != null) {
            pdbEntry.secondaryStructuresProperty().add(current);
        }
    }
}
