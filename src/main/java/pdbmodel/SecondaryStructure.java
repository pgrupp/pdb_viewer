package pdbmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Secondary Structure representation.
 */
public abstract class SecondaryStructure {
    private List<Residue> residuesContained;

    SecondaryStructure(){
        residuesContained = new ArrayList<>();
    }

    SecondaryStructure(List<Residue> residues){
        residuesContained = residues;
    }

    public int getLength(){
        return residuesContained.size();
    }
}
