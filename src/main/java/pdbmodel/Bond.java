package pdbmodel;

import graph.MyEdge;

/**
 * Representation of a Bond (covalent or cystein).
 */
public class Bond extends MyEdge{
    private final String bondType;

    public Bond() {
        this("covalent");
    }

    public Bond(String bondType){
        // TODO Bonds connecting atoms or residues?
        super()
        this.bondType = bondType;
    }
}
