package pdbmodel;

import graph.MyNode;

/**
 * Representation of an atom.
 */
public class Atom extends MyNode{

    private Residue residue;
    private double xCoordinate;
    private double yCoordinate;
    private double zCoordinate;


    public Atom(Residue residue, double x, double y, double z){
        super(); //TODO call proper constructor
        this.residue = residue;
        this.xCoordinate = x;
        this.yCoordinate = y;
        this.zCoordinate = z;
    }

    public

}
