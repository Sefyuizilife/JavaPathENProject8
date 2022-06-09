package tourguide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourguide.helper.InternalTestHelper;
import tourguide.tracker.Tracker;
import tourguide.user.User;
import tourguide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class TourGuideService {

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String            TEST_SERVER_API_KEY = "test-server-api-key";
    private static final Logger            LOGGER              = LoggerFactory.getLogger(TourGuideService.class);
    public final         Tracker           tracker;
    private final        GpsUtil           gpsUtil;
    private final        RewardsService    rewardsService;
    private final        TripPricer        tripPricer          = new TripPricer();
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final        Map<String, User> internalUserMap     = new HashMap<>();
    boolean testMode = true;

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {

        this.gpsUtil        = gpsUtil;
        this.rewardsService = rewardsService;

        if (this.testMode) {
            LOGGER.info("TestMode enabled");
            LOGGER.debug("Initializing users");
            this.initializeInternalUsers();
            LOGGER.debug("Finished initializing users");
        }

        this.tracker = new Tracker(this);
        this.addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {

        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {

        return (!user.getVisitedLocations().isEmpty()) ? user.getLastVisitedLocation() : this.trackUserLocation(user);
    }

    public User getUser(String userName) {

        return this.internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {

        return new ArrayList<>(this.internalUserMap.values());
    }

    public void addUser(User user) {

        if (!this.internalUserMap.containsKey(user.getUserName())) {
            this.internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {

        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = this.tripPricer.getPrice(TEST_SERVER_API_KEY, user.getUserId(),
                                                            user.getUserPreferences().getNumberOfAdults(),
                                                            user.getUserPreferences().getNumberOfChildren(),
                                                            user.getUserPreferences().getTripDuration(),
                                                            cumulatativeRewardPoints
        );
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {

        VisitedLocation visitedLocation = this.gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        this.rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {

        List<Attraction> nearbyAttractions = new ArrayList<>();
        for (Attraction attraction : this.gpsUtil.getAttractions()) {
            if (this.rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
                nearbyAttractions.add(attraction);
            }
        }

        return nearbyAttractions;
    }

    private void addShutDownHook() {

        Runtime.getRuntime().addShutdownHook(new Thread(TourGuideService.this.tracker::stopTracking));
    }

    private void initializeInternalUsers() {

        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone    = "000";
            String email    = userName + "@tourGuide.com";
            User   user     = new User(UUID.randomUUID(), userName, phone, email);
            this.generateUserLocationHistory(user);

            this.internalUserMap.put(userName, user);
        });
        LOGGER.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {

        IntStream.range(0, 3).forEach(i -> user.addToVisitedLocations(
                new VisitedLocation(
                        user.getUserId(), new Location(
                        this.generateRandomLatitude(),
                        this.generateRandomLongitude()
                ), this.getRandomTime())
        ));
    }

    private double generateRandomLongitude() {

        double leftLimit  = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {

        double leftLimit  = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {

        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
}
