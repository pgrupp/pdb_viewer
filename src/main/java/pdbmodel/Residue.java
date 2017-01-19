package pdbmodel;

/**
 * Representation of a residue, holding all relevant atoms of a particular residue.
 */
public class Residue {

    public enum AminoAcid {ALA, ARG, ASN, ASP, CYS, GLU, GLN, GLY, HIS, ILE, LEU, LYS, MET, PHE, PRO, SER, THR, TRP, TYR, VAL}

    /**
     * C atom of the residue.
     */
    private Atom cAtom;
    /**
     * O atom of the residue.
     */
    private Atom oAtom;
    /**
     * N atom of the residue.
     */
    private Atom nAtom;
    /**
     * C alpha atom of the residue.
     */
    private Atom cAlphaAtom;
    /**
     * C beta atom of the residue.
     */
    private Atom cBetaAtom;

    /**
     * PDB index of the residue (not necessarily starts with 1 and not necessarily continuous).
     */
    String resNum;

    /**
     * The current residues
     */
    AminoAcid aminoAcid;

    /**
     * Create a new residue.
     *
     * @param resNum    The residue number in the sequence
     * @param aminoAcid The amino acid of the  residue. Must be of enum {@link AminoAcid}
     */
    Residue(String resNum, String aminoAcid) {
        this.aminoAcid = AminoAcid.valueOf(aminoAcid);
        this.resNum = resNum;
    }

    public Atom getCAtom() {
        return cAtom;
    }

    public void setCAtom(Atom cAtom) {
        this.cAtom = cAtom;
    }

    public Atom getOAtom() {
        return oAtom;
    }

    public void setOAtom(Atom oAtom) {
        this.oAtom = oAtom;
    }

    public Atom getNAtom() {
        return nAtom;
    }

    public void setNAtom(Atom nAtom) {
        this.nAtom = nAtom;
    }

    public Atom getCAlphaAtom() {
        return cAlphaAtom;
    }

    public void setCAlphaAtom(Atom cAlphaAtom) {
        this.cAlphaAtom = cAlphaAtom;
    }

    public Atom getCBetaAtom() {
        return cBetaAtom;
    }

    public void setCBetaAtom(Atom cBetaAtom) {
        this.cBetaAtom = cBetaAtom;
    }

    /**
     * Get the PDB residue number property.
     *
     * @return The number of the residue as defined by PDB pdbFile as property.
     */
    public String getResNum() {
        return this.resNum;
    }

    public void setResNum(String resNum) {
        this.resNum = resNum;
    }

    /**
     * The amino acid this residue represents
     *
     * @return The amino acid this residue represents.
     */
    public AminoAcid getAminoAcid() {
        return this.aminoAcid;
    }

    public void setAminoAcid(String aminoAcid) {
        this.aminoAcid = AminoAcid.valueOf(aminoAcid);
    }

}
