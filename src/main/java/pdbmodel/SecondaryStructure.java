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

    /**
     * Add a residue to the secondary structure. Assumes that it has been checked if the residue
     * fits to this secondary structure.
     * @param residue The residue to be added.
     */
    public void addResidue(Residue residue){
        this.residuesContained.add(residue);
    }

    /**
     * Get the type of the secondary structure.
     * @return Type of the secondary structure.
     */
    public StructureType getSecondaryStructureType(){
        return this.secondaryStructureType;
    }

    /**
     * Get a one letter type of this SecondaryStructure instance. H for Helix, E for beta sheet.
     * @return H for helix, E for beta sheet.
     */
    public String getOneLetterSecondaryStructureType(){
        if(this.secondaryStructureType.toString().equals(StructureType.alphahelix.toString())){
            // alphahelix
            return "H";
        } else {
            // always betasheet
            return "E";
        }
    }
}
