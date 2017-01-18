package pdbmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Secondary Structure representation.
 */
public class SecondaryStructure {

    /**
     * Define possible types of secondary structure.
     */
    public enum StructureType {
        betasheet, alphahelix
    }

    /**
     * Residues contained by a secondary structure.
     */
    private List<Residue> residuesContained;

    /**
     * The secondaryStructureType of the secondary structure.
     */
    private StructureType secondaryStructureType;

    /**
     * Declare a new secondary structure.
     * @param secondaryStructureType The type of the secondary structure.
     */
    SecondaryStructure(StructureType secondaryStructureType) {
        this.secondaryStructureType = secondaryStructureType;
        residuesContained = new ArrayList<>();
    }

    /**
     * Declare a new secondary structure.
     *
     * @param residues               The residues part of the secondary structure.
     * @param secondaryStructureType The type of the secondary structure.
     */
    SecondaryStructure(StructureType secondaryStructureType, List<Residue> residues) {
        this.secondaryStructureType = secondaryStructureType;
        residuesContained = residues;
    }

    /**
     * Get the length of the secondary structure element.
     *
     * @return length of the secondary structure element.
     */
    public int getLength() {
        return residuesContained.size();
    }

    public void addResidue(Residue residue){
        this.residuesContained.add(residue);
    }
}
