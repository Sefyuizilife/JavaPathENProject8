package tourguide;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourguide.helper.InternalTestHelper;
import tourguide.service.GpsUtilsService;
import tourguide.service.RewardsService;
import tourguide.service.TourGuideService;
import tourguide.service.TripPricerService;
import tourguide.user.User;
import tripPricer.TripPricer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestPerformance {

    /*
     * A note on performance improvements:
     *
     *     The number of users generated for the high volume tests can be easily adjusted via this method:
     *
     *     		InternalTestHelper.setInternalUserNumber(100000);
     *
     *     These tests can be modified to suit new solutions, just as long as the performance metrics
     *     at the end of the tests remains consistent.
     *
     *     These are performance metrics that we are trying to hit:
     *
     *     highVolumeTrackLocation: 100,000 users within 15 minutes:
     *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
     *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

    @Test
    public void highVolumeTrackLocation() {

        // Users should be incremented up to 100,000, and test finishes within 15 minutes
        InternalTestHelper.setInternalUserNumber(100000);

        TourGuideService tourGuideService = new TourGuideService(
                new GpsUtilsService(new GpsUtil()), new RewardsService(new RewardCentral()),
                new TripPricerService(new TripPricer())
        );

        List<User> allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Collection<CompletableFuture> completableFutures = new ArrayList<>();

        for (User user : allUsers) {

            completableFutures.add(tourGuideService.trackUserLocation(user));
        }

        completableFutures.forEach(CompletableFuture::join);

        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }


    @Test
    public void highVolumeGetRewards() throws InterruptedException {

        GpsUtilsService gpsUtilsService = new GpsUtilsService(new GpsUtil());

        TourGuideService tourGuideService = new TourGuideService(
                gpsUtilsService, new RewardsService(new RewardCentral()),
                new TripPricerService(new TripPricer())
        );

        // Users should be incremented up to 100,000, and test finishes within 20 minutes
        InternalTestHelper.setInternalUserNumber(100000);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();


        Attraction attraction = gpsUtilsService.getAllAttractions().get(0);
        List<User> allUsers   = tourGuideService.getAllUsers();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        allUsers.forEach(tourGuideService::calculateRewards);

        tourGuideService.getExecutorService().shutdown();
        boolean allTasksIsFinished = tourGuideService.getExecutorService().awaitTermination(19, TimeUnit.MINUTES);

        System.out.println("Tâches finies: " + allTasksIsFinished);

        for (User user : allUsers) {
            assertFalse(user.getUserRewards().isEmpty());
        }
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(
                stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

}
