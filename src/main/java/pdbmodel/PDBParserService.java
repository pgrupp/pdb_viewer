package pdbmodel;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.*;
import java.util.Map;

/**
 * Parser for PDB files.
 *
 * @author Patrick Grupp
 */
public class PDBParserService extends Service<PDBEntry> {

    /**
     * PDB pdbFile to be opened.
     */
    File pdbFile;

    /**
     * Set the file to be opened by the service.
     *
     * @param pdbFile
     */

    public void setPdbFile(File pdbFile) {
        this.pdbFile = pdbFile;
    }

    private enum Status {header, remarks, helix, betasheet, atom, term}

    @Override
    protected Task<PDBEntry> createTask() {
        return new Task<PDBEntry>() {
            @Override
            protected PDBEntry call() throws Exception {
                return parse(pdbFile);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage("Done!");
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                updateMessage("Cancelled!");
            }

            @Override
            protected void failed() {
                super.failed();
                updateMessage("Failed!");
            }

            private PDBEntry parse(File file) throws Exception {

                if (!file.exists()) {
                    throw new FileNotFoundException("The pdbFile " + file.getPath() + " does not exist.");
                }

                PDBEntry pdbEntry = new PDBEntry();

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String curr;
                Status status = Status.header;
                while ((curr = reader.readLine()) != null) {
                    status = processLine(curr, status, pdbEntry);
                    updateMessage("Reading " + status.toString());
                    if (status.equals(Status.term))
                        break;
                }

                if (pdbEntry.nodesProperty().size() == 0) {
                    throw new Exception("No nodes were read from pdbFile. Exiting.");
                }

                return pdbEntry;
            }

            private Status processLine(String line, Status status, PDBEntry pdbEntry) {
                if (line.startsWith("HEADER")) {
                    pdbEntry.titleProperty().setValue(line.substring(10, 49).trim());
                    pdbEntry.pdbCodeProperty().setValue(line.substring(62, 65).trim());

                    return Status.header;
                } else if (line.startsWith("HELIX")) {
                    pdbEntry.secondaryStructuresProperty().add(new SecondaryStructure())
                    return Status.helix;
                } else if (line.startsWith("SHEET")) {

                    return Status.betasheet;
                } else if (line.startsWith("ATOM")) {

                    return Status.atom;
                } else if (line.startsWith("TERM"))
                    return Status.term;
                else
                    return Status.remarks;
            }


        };
    }
}
