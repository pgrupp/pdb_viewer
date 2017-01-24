package blast;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import blast.RemoteBlastClient.Status;

/**
 * Service allowing to concurrently call a BLAST function and run it.
 */
public class BlastService extends Service<String> {

    private String sequence;

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }


    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {

                final StringBuilder result = new StringBuilder();
                final RemoteBlastClient remoteBlastClient = new RemoteBlastClient();
                remoteBlastClient.setProgram(RemoteBlastClient.BlastProgram.blastp).setDatabase("nr");
                updateTitle("BLASTing sequence");
                remoteBlastClient.startRemoteSearch(sequence);

                updateMessage("Request id: " + remoteBlastClient.getRequestId() + "\n" +
                        "Estimated time: " + remoteBlastClient.getEstimatedTime() + "s");
                updateProgress(0, remoteBlastClient.getEstimatedTime());
                long startTime = System.currentTimeMillis();
                Status status = null;

                do {
                    if (status != null)
                        Thread.sleep(5000);
                    status = remoteBlastClient.getRemoteStatus();
                    updateProgress((System.currentTimeMillis() - startTime)/1000, remoteBlastClient.getEstimatedTime());
                }
                while (status == RemoteBlastClient.Status.searching);
                updateTitle("Done");
                switch (status) {
                    case hitsFound:
                        for (String line : remoteBlastClient.getRemoteAlignments()) {
                            result.append(line);
                        }
                        break;
                    case noHitsFound:
                        System.err.println("No hits");
                        break;
                    default:
                        System.err.println("Status: " + status);
                }

                System.err.println("Actual time: " + remoteBlastClient.getActualTime() + "s");
                return result.toString();
            }
        };
    }
}
