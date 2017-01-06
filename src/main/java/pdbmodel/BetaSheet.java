package pdbmodel;

import java.util.List;

/**
 * Representation of beta sheet secondary structures.
 */
public class BetaSheet extends SecondaryStructure {
    public BetaSheet(List<Residue> residues){
        super(residues);
    }
}
