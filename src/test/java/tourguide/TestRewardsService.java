package tourguide;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Before;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourguide.helper.InternalTestHelper;
import tourguide.service.GpsUtilsService;
import tourguide.service.RewardsService;
import tourguide.service.TourGuideService;
import tourguide.service.TripPricerService;
import tourguide.user.User;
import tourguide.user.UserReward;
import tripPricer.TripPricer;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRewardsService {

    private GpsUtilsService   gpsUtilsService;
    private RewardsService    rewardsService;
    private TripPricerService tripPricerService;
    private TourGuideService  tourGuideService;


    @Before
    public void setUp() {

        this.gpsUtilsService   = new GpsUtilsService(new GpsUtil());
        this.rewardsService    = new RewardsService(new RewardCentral());
        this.tripPricerService = new TripPricerService(new TripPricer());
    }

    @Test
    public void userGetRewards() {

        InternalTestHelper.setInternalUserNumber(0);
        this.tourGuideService = new TourGuideService(this.gpsUtilsService, this.rewardsService, this.tripPricerService);

        User       user       = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = this.gpsUtilsService.getAllAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        this.tourGuideService.calculateRewards(user);

        this.tourGuideService.waitAllWorkIsCompleted();

        List<UserReward> userRewards = user.getUserRewards();

        this.tourGuideService.tracker.stopTracking();

        assertEquals(1, userRewards.size());
    }

    @Test
    public void isWithinAttractionProximity() {

        InternalTestHelper.setInternalUserNumber(0);
        this.tourGuideService = new TourGuideService(this.gpsUtilsService, this.rewardsService, this.tripPricerService);

        Attraction      attraction      = this.gpsUtilsService.getAllAttractions().get(0);
        VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), attraction, new Date());
        assertTrue(this.tourGuideService.isNearAttraction(visitedLocation, attraction));
    }

    // Needs fixed - can throw ConcurrentModificationException
    @Test
    public void nearAllAttractions() {

        InternalTestHelper.setInternalUserNumber(1);
        this.tourGuideService = new TourGuideService(this.gpsUtilsService, this.rewardsService, this.tripPricerService);
        this.tourGuideService.setProximityBuffer(Integer.MAX_VALUE);

        User user = this.tourGuideService.getAllUsers().get(0);

        this.tourGuideService.calculateRewards(user);

        this.tourGuideService.waitAllWorkIsCompleted();

        List<UserReward> userRewards = this.tourGuideService.getUserRewards(user);
        this.tourGuideService.tracker.stopTracking();

        assertEquals(this.gpsUtilsService.getAllAttractions().size(), userRewards.size());
    }
}
