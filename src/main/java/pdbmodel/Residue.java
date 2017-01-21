package pdbmodel;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a residue, holding all relevant atoms of a particular residue.
 */
public class Residue {

    public enum AminoAcid {
        ALA, ARG, ASN, ASP, CYS, GLU, GLN, GLY, HIS, ILE, LEU, LYS, MET, PHE, PRO, SER, THR, TRP, TYR, VAL;

    }

    private static Map<AminoAcid, Pair<String, String>> aminoAcidMap;

    static {
        aminoAcidMap = new HashMap<>();
        aminoAcidMap.put(AminoAcid.ALA, new Pair<>("A", "Alanine"));
        aminoAcidMap.put(AminoAcid.ARG, new Pair<>("R", "Arginine"));
        aminoAcidMap.put(AminoAcid.ASN, new Pair<>("N", "Asparagine"));
        aminoAcidMap.put(AminoAcid.ASP, new Pair<>("D", "Aspartic Acid"));
        aminoAcidMap.put(AminoAcid.CYS, new Pair<>("C", "Cysteine"));
        aminoAcidMap.put(AminoAcid.GLU, new Pair<>("E", "Glutamic Acid"));
        aminoAcidMap.put(AminoAcid.GLN, new Pair<>("Q", "Glutamine"));
        aminoAcidMap.put(AminoAcid.GLY, new Pair<>("G", "Glycine"));
        aminoAcidMap.put(AminoAcid.HIS, new Pair<>("H", "Histidine"));
        aminoAcidMap.put(AminoAcid.ILE, new Pair<>("I", "Isoleucine"));
        aminoAcidMap.put(AminoAcid.LEU, new Pair<>("L", "Leucine"));
        aminoAcidMap.put(AminoAcid.LYS, new Pair<>("K", "Lysine"));
        aminoAcidMap.put(AminoAcid.MET, new Pair<>("M", "Methionine"));
        aminoAcidMap.put(AminoAcid.PHE, new Pair<>("F", "Phenylalanine"));
        aminoAcidMap.put(AminoAcid.PRO, new Pair<>("P", "Proline"));
        aminoAcidMap.put(AminoAcid.SER, new Pair<>("S", "Serine"));
        aminoAcidMap.put(AminoAcid.THR, new Pair<>("T", "Threonine"));
        aminoAcidMap.put(AminoAcid.TRP, new Pair<>("W", "Tryptophan"));
        aminoAcidMap.put(AminoAcid.TYR, new Pair<>("Y", "Tyrosine"));
        aminoAcidMap.put(AminoAcid.VAL, new Pair<>("V", "Valine"));
    }

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
     * If the residue is part of a secondary structure it is referenced here, else null.
     */
    private SecondaryStructure secondaryStructure;

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
        this.secondaryStructure = null;
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

    public SecondaryStructure getSecondaryStructure() {
        return secondaryStructure;
    }

    public void setSecondaryStructure(SecondaryStructure secondaryStructure) {
        this.secondaryStructure = secondaryStructure;
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

    /**
     * Return the one letter code for each residue.
     * @return One letter code of the amino acid.
     */
    public String getOneLetterAminoAcidName(){
        return aminoAcidMap.get(this.aminoAcid).getKey();
    }

    /**
     * Return the one letter code for each residue.
     * @return
     */
    public String toString(){
        return getResNum();
    }

    /**
     * Get the human readable name of the amino acid which this residue represents.
     * @return Human readable name of an amino acid residue.
     */
    public String getName(){
        return aminoAcidMap.get(this.aminoAcid).getValue();
    }

    /**
     * Get a one letter type of this SecondaryStructure instance. H for Helix, E for beta sheet.
     * @return H for helix, E for beta sheet.
     */
    public String getOneLetterSecondaryStructureType(){
        if(this.secondaryStructure == null){
            // Not part of a secondary structue.
            return " ";
        } else{
            return this.secondaryStructure.getOneLetterSecondaryStructureType();
        }
    }
}
