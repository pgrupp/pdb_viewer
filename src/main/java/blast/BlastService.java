package blast;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import blast.RemoteBlastClient.Status;

/**
 * Service allowing to concurrently call a BLAST function and run it.
 */
public class BlastService extends Service<String> {

    private String sequence = null;

    /**
     * Set the sequence to BLAST.
     *
     * @param sequence The sequence to BLAST.
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                // No sequence was set, the task needs to fail.
                if (sequence == null)
                    throw new Exception("No sequence set. Cannot BLAST.");
                // Build the result
                final StringBuilder result = new StringBuilder();
                // Use and call the client to handle BLAST queries
                final RemoteBlastClient remoteBlastClient = new RemoteBlastClient();
                remoteBlastClient.setProgram(RemoteBlastClient.BlastProgram.blastp).setDatabase("nr");

                // Set the Task's title to this
                updateTitle("BLAST sequence...");
                remoteBlastClient.startRemoteSearch(sequence);

                updateMessage("Request id: " + remoteBlastClient.getRequestId() + "\n" +
                        "Estimated time: " + remoteBlastClient.getEstimatedTime() + "s");
                updateProgress(0, remoteBlastClient.getEstimatedTime());
                long startTime = System.currentTimeMillis();
                Status status = null;
                // Query BLAST for status, if sequence is done or not.
                do {
                    if (status != null)
                        Thread.sleep(5000);
                    status = remoteBlastClient.getRemoteStatus();
                    updateMessage("Request id: " + remoteBlastClient.getRequestId() + "\n" +
                            "Estimated time: " + remoteBlastClient.getEstimatedTime() + "s\n" +
                            "Passed time: " + (System.currentTimeMillis() - startTime) / 1000 + "s");
                    updateProgress((System.currentTimeMillis() - startTime) / 1000, remoteBlastClient.getEstimatedTime());
                    if (isCancelled())
                        break;
                } while (status == RemoteBlastClient.Status.searching);

                if (isCancelled()) {
                    updateTitle("Cancelled");
                    result.append("Cancelled");
                    return result.toString();
                }

                switch (status) {
                    case hitsFound:
                        updateTitle("BLAST done: Hits found.");
                        for (String line : remoteBlastClient.getRemoteAlignments()) {
                            result.append(line + "\n");
                        }
                        break;
                    case noHitsFound:
                        updateTitle("BLAST done: no hits were found.");
                        result.append("No hits found.");
                        System.err.println("No hits");
                        break;
                    default:
                        updateMessage("BLAST failed.");
                        updateTitle("BLAST failed.");
                        System.err.println("Status: " + status);
                        throw new Exception("This might be because you are not connected to the Internet.");
                }

                System.err.println("Actual time: " + remoteBlastClient.getActualTime() + "s");
                return result.toString();
            }
        };
    }
}
