package pdbmodel;

import java.util.List;

/**
 * An entry of the Protein Data Bank (PDB) containing all Residues, which in turn contain all atoms associated with
 * them, and all secondary structures.
 */
public class PDBEntry {
    private List<Residue> residues;
    private List<BetaSheet> betaSheets;
    private List<AlphaHelix> alphaHelices;

    public PDBEntry(){

    }
}
