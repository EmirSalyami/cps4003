package librarysystem;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchAvailabilityJob implements Runnable {

    private BookRepository repoBooks;
    private List<Integer> lstBookIds;
    private String strNewStatus;
    private AtomicInteger numDoneCounter;

    public BatchAvailabilityJob(BookRepository repoBooks, List<Integer> lstBookIds, String strNewStatus,
        AtomicInteger numDoneCounter) {
        this.repoBooks = repoBooks;
        this.lstBookIds = lstBookIds;
        this.strNewStatus = strNewStatus;
        this.numDoneCounter = numDoneCounter;
    }

    @Override
    public void run() {
        int numIndex = 0;
        while (numIndex < lstBookIds.size()) {
            int numId = lstBookIds.get(numIndex).intValue();
            try {
                repoBooks.updateAvailabilityOnly(numId, strNewStatus);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            numDoneCounter.incrementAndGet();
            numIndex = numIndex + 1;
        }
    }
}
