package tourguide.tracker;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourguide.service.TourGuideService;
import tourguide.user.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tracker extends Thread {

    private static final long             TRACKING_POLLING_INTERVAL = TimeUnit.MINUTES.toSeconds(5);
    private static final Logger           LOGGER                    = LoggerFactory.getLogger(Tracker.class);
    private final        ExecutorService  executorService           = Executors.newSingleThreadExecutor();
    private final        TourGuideService tourGuideService;
    private              boolean          stop                      = false;

    public Tracker(TourGuideService tourGuideService) {

        this.tourGuideService = tourGuideService;

        this.executorService.submit(this);
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {

        this.stop = true;
        this.executorService.shutdownNow();
    }

    @Override
    public void run() {

        StopWatch stopWatch = new StopWatch();
        while (true) {
            if (Thread.currentThread().isInterrupted() || this.stop) {
                LOGGER.debug("Tracker stopping");
                break;
            }

            List<User> users = this.tourGuideService.getAllUsers();
            LOGGER.debug("Begin Tracker. Tracking " + users.size() + " users.");
            stopWatch.start();
            users.forEach(this.tourGuideService::trackUserLocation);
            stopWatch.stop();
            LOGGER.debug(
                    "Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
            stopWatch.reset();
            try {
                LOGGER.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(TRACKING_POLLING_INTERVAL);
            } catch (InterruptedException e) {
                break;
            }
        }

    }
}
