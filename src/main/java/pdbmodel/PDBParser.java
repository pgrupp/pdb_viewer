package pdbmodel;

import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * Parser for PDB files.
 *
 * @author Patrick Grupp
 */
public class PDBParser {

    private enum Status {header, remarks, helix, betasheet, atom, term}

    private static final int ATOM_DISTANCE_FACTOR = 20;

    /**
     * Parse the input in the given reader to the given pdbEntry model.
     *
     * @param pdbEntry The model to be written to.
     * @param reader   The reader with PDB entry conform information to be parsed.
     * @throws Exception If no nodes were added to the model until EOF. Or for any IOException.
     */
    public static void parse(PDBEntry pdbEntry, BufferedReader reader) throws Exception {

        String curr;
        // Here all atoms and secondary structures will be saved for later post processing in order to build up the
        // model, when all information is present
        ArrayList<Atom> atomArrayList = new ArrayList<>();
        ArrayList<Pair<String, String>> helices = new ArrayList<>();
        ArrayList<Pair<String, String>> betaSheets = new ArrayList<>();
        Status status = Status.header;
        // Loop over the pdb file and parse it
        while ((curr = reader.readLine()) != null) {
            status = processLine(curr, pdbEntry, atomArrayList, helices, betaSheets);
            if (status.equals(Status.term))
                break;
        }

        // Post process to build up an actual model of the protein described by the PDB file.
        ArrayList<Residue> residues = postProcess(pdbEntry, atomArrayList, helices, betaSheets);
        // Get nice coordinate positions out of the file
        normalizeCoordinates(residues);
        pdbEntry.residuesProperty().addAll(residues);
        // Bond the atoms together in a correct way, since a PDB dous not give awa information about
        // how the atoms are connected
        setUpBonds(pdbEntry);

        // Something went wrong, could not parse any nodes. Maybe wrong file format?
        if (pdbEntry.nodesProperty().size() == 0) {
            throw new Exception("No nodes were read from PDB file. Exiting.");
        }
    }

    /**
     * Process a line of the given PDB file and persist the contents in the pdbEntry. This assumes a certain order of lines.
     * Especially atoms of residues must be according to PDB guidelines in consecutive lines in the file.
     *
     * @param line       Line of a PDB file to be processed.
     * @param pdbEntry   The pdb model to be updated with the data from the file.
     * @param atoms      List of atoms, where results will be saved into for post processing.
     * @param helices    List of secondary structure helices, used for post processing. Should be empty from the get-go.
     * @param betaSheets List of secondary structure betasheets, used for post processing. Should be empty at startup.
     * @return A status which cna be used to update messages to the user. And {@link Status} term, when the outer
     * program should end parsind, since EOF or end of model is reached.
     */
    private static Status processLine(String line, PDBEntry pdbEntry, ArrayList<Atom> atoms,
                                      ArrayList<Pair<String, String>> helices,
                                      ArrayList<Pair<String, String>> betaSheets) {
        if (line.startsWith("HEADER")) {
            // Read the protein description and the four letter PDB ID and save it in the model for later
            // reference and presentation
            pdbEntry.titleProperty().setValue(line.substring(10, 50).trim());
            pdbEntry.pdbCodeProperty().setValue(line.substring(62, 66).trim());
            return Status.header;
        } else if (line.startsWith("HELIX")) {
            // Read alpha helix secondary structures.
            String startResSeqNum = line.substring(21, 26).trim();
            String endResSeqNum = line.substring(33, 38).trim();
            helices.add(new Pair<>(startResSeqNum, endResSeqNum));
            return Status.helix;
        } else if (line.startsWith("SHEET")) {
            // Read beta sheet secondary structures.
            String startResSeqNum = line.substring(22, 27).trim();
            String endResSeqNum = line.substring(33, 38).trim();
            betaSheets.add(new Pair<>(startResSeqNum, endResSeqNum));
            return Status.betasheet;
        } else if (line.startsWith("ATOM")) {
            // Read atom instances, used to determine each atoms place in 3d space. and to determine the protein's
            // residue sequence.
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
            // This terminates the process in outer method, since it is the end of the model.
            return Status.term;
        else
            // This is output when anything is read which is not parsed by this program, since the information are
            // of no use for its purposes.
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
     * @return List of residues still to be added to the model, after the coordinated have been normalized.
     */
    private static ArrayList<Residue> postProcess(PDBEntry pdbEntry, ArrayList<Atom> atomArrayList,
                                                  ArrayList<Pair<String, String>> helices,
                                                  ArrayList<Pair<String, String>> betaSheets) {
        ArrayList<Residue> residues = new ArrayList<>();
        Residue currentResidue = null;
        for (Atom a : atomArrayList) {
            String residueSeqNum = a.textProperty().getValue().split("\\$")[0];
            String residueName = a.textProperty().getValue().split("\\$")[1];
            if (currentResidue == null) {
                currentResidue = new Residue(residueSeqNum, residueName);
            } else if (!currentResidue.getResNum().equals(residueSeqNum)) {
                residues.add(currentResidue);
                // If the now completed Residue is Glycine, add an interpolated C beta atom to the residue.
                if (currentResidue.getAminoAcid().equals(Residue.AminoAcid.GLY)) {
                    handleGlycine(currentResidue);
                }
                addToGraph(pdbEntry, currentResidue);
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
            residues.add(currentResidue);
            // If last amino acid is glycine, add an interpolated C beta atom to the residue
            if (currentResidue.getAminoAcid().equals(Residue.AminoAcid.GLY)) {
                handleGlycine(currentResidue);
            }
            addToGraph(pdbEntry, currentResidue);
        }

        for (Pair<String, String> struc : helices) {
            handleSecondaryStructures(pdbEntry, residues, struc, SecondaryStructure.StructureType.alphahelix);
        }

        for (Pair<String, String> struc : betaSheets) {
            handleSecondaryStructures(pdbEntry, residues, struc, SecondaryStructure.StructureType.betasheet);
        }

        return residues;
    }

    /**
     * Add the nodes of a residue to the nodes list in the graph of the pdbentry.
     *
     * @param pdbEntry       Where the nodes will be added.
     * @param currentResidue The residue to be added to the graph model.
     */
    private static void addToGraph(PDBEntry pdbEntry, Residue currentResidue) {
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

        // Point of C alpha in 3D space
        Point3D caPoint = new Point3D(ca.xCoordinateProperty().get(), ca.yCoordinateProperty().get(),
                ca.zCoordinateProperty().get());
        // Point of C in 3D space
        Point3D cPoint = new Point3D(c.xCoordinateProperty().get(), c.yCoordinateProperty().get(),
                c.zCoordinateProperty().get());
        // Point of N in 3D space
        Point3D nPoint = new Point3D(n.xCoordinateProperty().get(), n.yCoordinateProperty().get(),
                n.zCoordinateProperty().get());

        // In the following we subtract C alpha in order to have it as our origin (0,0,0)
        // Find the middle between N and C
        Point3D midNC = cPoint.midpoint(nPoint).subtract(caPoint); // subtract ca in order to get the direction vector
        // Find the rotation axis which will rotate this point by 1/3 (120°) keeping the same distance to C and N around C alpha
        // For that we need the normal vector of C alpha -> N and C alpha -> C. With that vector we compute the perpendicular
        // vector of it and the C alpha -> midpoint (of N and C). That is our rotation axis.
        Point3D nCaCPerpendicular = cPoint.subtract(caPoint).crossProduct(nPoint.subtract(caPoint)); // Perpendiculat on plane C -> C alpha -> N
        Point3D rotationAxis = nCaCPerpendicular.crossProduct(midNC);

        // Set the correct C - C bond length for the resulting vector since Calpha and Cbeta will have similar distance
        Point3D resultingPoint = midNC.normalize().multiply(cPoint.subtract(caPoint).magnitude());

        // The rotation is approx. 120°. At -120° we expect the H-Atom of C alpha to be. So they will have equal distance.
        Rotate rotate = new Rotate(120, rotationAxis);
        resultingPoint = rotate.transform(resultingPoint);

        // Set C-alpha as origin point (the point was moved to (0,0,0) as origin for computation)
        resultingPoint = resultingPoint.add(caPoint);

        // Set the results
        residue.setCBetaAtom(new Atom(resultingPoint.getX(), resultingPoint.getY(), resultingPoint.getZ(), "CB", ""));
        residue.getCBetaAtom().residueProperty().setValue(residue);

    }

    /**
     * Handle the read secondary structures and add them to the {@link PDBEntry} model.
     *
     * @param pdbEntry The model to be manipulated.
     * @param residues List of residues for which the secondary structures should be added.
     * @param struc    The structure read from PDB file. Pair of Strings key being begin residue Number and value being end residue Number.
     * @param type     The type of the {@link SecondaryStructure}. Can either be alphahelix or betasheet.
     */
    private static void handleSecondaryStructures(PDBEntry pdbEntry, ArrayList<Residue> residues,
                                                  Pair<String, String> struc, SecondaryStructure.StructureType type) {
        SecondaryStructure current = null;
        for (Residue res : residues) {
            if (current == null && res.getResNum().equals(struc.getKey())) {
                // at the begin of a secondary structure
                current = new SecondaryStructure(type);
                res.setSecondaryStructure(current); // Set this secondary structure for the residue
                current.addResidue(res);
            } else if (current != null && !res.getResNum().equals(struc.getValue())) {
                // in the 'middle' of a secondary structure
                res.setSecondaryStructure(current); // Set this secondary structure for the residue
                current.addResidue(res);
            } else if (current != null && res.getResNum().equals(struc.getValue())) {
                // at the end of a secondary structure
                res.setSecondaryStructure(current); // Set this secondary structure for the residue
                current.addResidue(res);
                break;
            }
        }
        if (current != null) {
            pdbEntry.secondaryStructuresProperty().add(current);
        }
    }

    /**
     * Normalize the coordinated given by PDB aound the (0,0,0) point in the 3d model, in order to have it
     * centered at all times.
     */
    private static void normalizeCoordinates(ArrayList<Residue> residues) {
        double x = 0;
        double y = 0;
        double z = 0;

        int atoms = 0;
        for (Residue res : residues) {
            for (Atom a : res.getAtoms()) {
                x += a.xCoordinateProperty().getValue();
                y += a.yCoordinateProperty().getValue();
                z += a.zCoordinateProperty().getValue();
                atoms++;
            }
        }
        x = x / atoms;
        y = y / atoms;
        z = z / atoms;

        for (Residue res : residues) {
            for (Atom a : res.getAtoms()) {
                a.xCoordinateProperty().setValue(a.xCoordinateProperty().getValue() - x);
                a.yCoordinateProperty().setValue(a.yCoordinateProperty().getValue() - y);
                a.zCoordinateProperty().setValue(a.zCoordinateProperty().getValue() - z);
                a.textProperty().setValue("Residue: " + a.residueProperty().getValue().getResNum() +
                        ", amino acid: " + a.residueProperty().getValue().getName());
            }
        }
    }

    /**
     * Set up bonds, using the given residues in the model's (pdbEntry) nodes list.
     *
     * @param pdbEntry The model instance for which bons should be built up.
     */
    private static void setUpBonds(PDBEntry pdbEntry) {
        for (int i = 0; i < pdbEntry.residuesProperty().size(); i++) {
            Residue res = pdbEntry.residuesProperty().get(i);
            try {
                if (i != 0) {
                    // not N terminal, N terminus does not need to be connected to anything (to the 'left')
                    // Connect C of last ('left') amino acid with current amino acid's N
                    pdbEntry.connectNodes(pdbEntry.residuesProperty().get(i - 1).getCAtom(), res.getNAtom());
                }
                // internal amino acid or c terminus and n terminus need this
                // Connect N - Calpha
                pdbEntry.connectNodes(res.getNAtom(), res.getCAlphaAtom());
                // Connect Calpha - Cbeta
                pdbEntry.connectNodes(res.getCAlphaAtom(), res.getCBetaAtom());
                // Connect Calpha - C
                pdbEntry.connectNodes(res.getCAlphaAtom(), res.getCAtom());
                // Connect C - O
                pdbEntry.connectNodes(res.getCAtom(), res.getOAtom());

            } catch (GraphException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
