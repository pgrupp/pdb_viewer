package pdbmodel;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Representation of a residue, holding all relevant atoms of a particular residue.
 */
public class Residue {

    public enum AminoAcid {ALA, ARG, ASN, ASP, CYS, GLU, GLN, GLY, HIS, ILE, LEU, LYS, MET, PHE, PRO, SER, THR, TRP, TYR, VAL}

    /**
     * Atoms contained in the residue.
     */
    ArrayList<Atom> atoms;

    /**
     * PDB index of the residue (not necessarily starts with 1 and not necessarily continuous).
     */
    int resNum;

    /**
     * The current residues
     */
    AminoAcid aminoAcid;

    /**
     * Create a new residue with an amount of atoms (0-...).
     *
     * @param resNum The residue number in the sequence
     * @param atom   The atoms of the residue. Arbitrary amount.
     */
    public Residue(String aminoAcid, int resNum, Atom... atom) {
        this.resNum = resNum;
        if(atom.length > 0) {
            this.atoms = new ArrayList<>(Arrays.asList(atom));
        } else {
            this.atoms = new ArrayList<>();
        }
        this.aminoAcid = AminoAcid.valueOf(aminoAcid);
    }

    /**
     * Get the PDB residue number.
     * @return The number of the residue as defined by PDB pdbFile.
     */
    public int getResNum() {
        return resNum;
    }

    /**
     * Add an atom to this residues atom list.
     * @param atom The atom to be added.
     */
    public void addAtom(Atom atom){
        this.atoms.add(atom);
    }

}
