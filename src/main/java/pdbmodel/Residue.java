package pdbmodel;

/**
 * Representation of a residue, holding all relevant atoms of a particular residue.
 */
public class Residue {
    Atom[] atoms;
    int resNum;

    /**
     * Create a new residue with an amount of atoms (0-...).
     * @param resNum The residue number in the sequence
     * @param atom The atoms of the residue. Arbitrary amount.
     */
    public Residue(int resNum, Atom... atom) {
        this.resNum = resNum;
        this.atoms = atom;
    }

}
