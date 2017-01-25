package pdbmodel;

import java.util.ArrayList;

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
    private ArrayList<Residue> residuesContained;

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
    SecondaryStructure(StructureType secondaryStructureType, ArrayList<Residue> residues) {
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
    void addResidue(Residue residue){
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
    String getOneLetterSecondaryStructureType(){
        if(this.secondaryStructureType.toString().equals(StructureType.alphahelix.toString())){
            // alphahelix
            return "H";
        } else {
            // always betasheet
            return "E";
        }
    }

    /**
     * Get the first residue contained in the secondary structure.
     * @return The first residue contained in the secondary structure.
     */
    public Residue getFirstResidue(){
        return residuesContained.get(0);
    }

    /**
     * Get the last residue contained in the secondary structure.
     * @return The last residue contained in the secondary structure.
     */
    public Residue getLastResidue(){
        return residuesContained.get(residuesContained.size() - 1);
    }

    /**
     * Get a list of the contained residues.
     * @return List of residues contained in this secondary structure.
     */
    public ArrayList<Residue> getResiduesContained(){
        return residuesContained;
    }
}
