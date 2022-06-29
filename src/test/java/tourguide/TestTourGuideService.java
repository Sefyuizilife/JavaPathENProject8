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
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestTourGuideService {

    private GpsUtilsService   gpsUtilsService;
    private RewardsService    rewardsService;
    private TripPricerService tripPricerService;

    @Before
    public void setUp() {

        this.gpsUtilsService   = new GpsUtilsService(new GpsUtil());
        this.rewardsService    = new RewardsService(new RewardCentral());
        this.tripPricerService = new TripPricerService(new TripPricer());
    }

    @Test
    public void getUserLocation() throws ExecutionException, InterruptedException {

        InternalTestHelper.setInternalUserNumber(0);

        TourGuideService tourGuideService = new TourGuideService(
                this.gpsUtilsService, this.rewardsService, this.tripPricerService);

        User            user            = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).get();
        tourGuideService.tracker.stopTracking();
        assertEquals(visitedLocation.userId, user.getUserId());
    }

    @Test
    public void addUser() {

        InternalTestHelper.setInternalUserNumber(0);

        TourGuideService tourGuideService = new TourGuideService(
                this.gpsUtilsService, this.rewardsService, this.tripPricerService);

        User user  = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrievedUser  = tourGuideService.getUser(user.getUserName());
        User retrievedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrievedUser);
        assertEquals(user2, retrievedUser2);
    }

    @Test
    public void getAllUsers() {

        InternalTestHelper.setInternalUserNumber(0);

        TourGuideService tourGuideService = new TourGuideService(
                this.gpsUtilsService, this.rewardsService, this.tripPricerService);

        User user  = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() throws ExecutionException, InterruptedException {

        InternalTestHelper.setInternalUserNumber(0);

        TourGuideService tourGuideService = new TourGuideService(
                this.gpsUtilsService, this.rewardsService, this.tripPricerService);

        User            user            = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).get();

        tourGuideService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }


    @Test
    public void getNearbyAttractions() {

        InternalTestHelper.setInternalUserNumber(0);

        TourGuideService tourGuideService = new TourGuideService(
                this.gpsUtilsService, this.rewardsService, this.tripPricerService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        Collection<Attraction> attractions = tourGuideService.getDistanceAttractions(user, 5).values();

        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractions.size());
    }

    @Test
    public void getTripDeals() {

        InternalTestHelper.setInternalUserNumber(0);

        TourGuideService tourGuideService = new TourGuideService(
                this.gpsUtilsService, this.rewardsService, this.tripPricerService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, providers.size());
    }
}
