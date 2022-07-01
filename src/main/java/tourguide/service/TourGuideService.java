package tourguide.service;

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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final Logger            LOGGER          = LoggerFactory.getLogger(TourGuideService.class);
    public final         Tracker           tracker;
    private final        GpsUtilsService   gpsUtilsService;
    private final        RewardsService    rewardsService;
    private final        TripPricerService tripPricerService;
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final        Map<String, User> internalUserMap = new HashMap<>();
    private final        boolean           testMode        = true;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1000);

    private final int defaultProximityBuffer = 10;
    private       int proximityBuffer        = this.defaultProximityBuffer;
    private       int proximityRange         = 200;

    public TourGuideService(GpsUtilsService gpsUtilsService, RewardsService rewardsService, TripPricerService tripPricerService) {

        this.gpsUtilsService   = gpsUtilsService;
        this.rewardsService    = rewardsService;
        this.tripPricerService = tripPricerService;

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

    public CompletableFuture<VisitedLocation> getUserLocation(User user) {

        return user.getVisitedLocations().isEmpty() ? this.trackUserLocation(user) : CompletableFuture.completedFuture(
                user.getLastVisitedLocation());
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

        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = this.tripPricerService.getPrice(user.getUserId(),
                                                                   user.getUserPreferences().getNumberOfAdults(),
                                                                   user.getUserPreferences().getNumberOfChildren(),
                                                                   user.getUserPreferences().getTripDuration(),
                                                                   cumulativeRewardPoints
        );
        user.setTripDeals(providers);
        return providers;
    }

    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {

        return CompletableFuture.supplyAsync(() -> {
            VisitedLocation visitedLocation = this.gpsUtilsService.getUserLocation(user.getUserId());
            user.addToVisitedLocations(visitedLocation);

            return visitedLocation;
        }, this.executorService).thenApplyAsync((visitedLocation) -> {
            this.calculateRewards(user);
            return visitedLocation;
        }, this.executorService);
    }

    public int getRewardPoints(Attraction attraction, User user) {

        return this.rewardsService.getRewardPoints(attraction, user);
    }

    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {

        List<Attraction> nearbyAttractions = new ArrayList<>();

        for (Attraction attraction : this.gpsUtilsService.getAllAttractions()) {

            if (this.isWithinAttractionProximity(attraction, visitedLocation.location)) {
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
            this.gpsUtilsService.generateUserLocationHistory(user);

            this.internalUserMap.put(userName, user);
        });
        LOGGER.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    public void setDefaultProximityBuffer() {

        this.proximityBuffer = this.defaultProximityBuffer;
    }

    public void setProximityBuffer(int proximityBuffer) {

        this.proximityBuffer = proximityBuffer;
    }

    public Map<Double, Attraction> getDistanceAttractions(User user) {

        return this.getDistanceAttractions(user, this.gpsUtilsService.getAllAttractions().size());
    }

    public Map<Double, Attraction> getDistanceAttractions(User user, int maxAttractions) {

        VisitedLocation userLocation = this.gpsUtilsService.getUserLocation(user.getUserId());

        List<Attraction> attractions = this.gpsUtilsService.getAllAttractions().subList(0, maxAttractions);

        Map<Double, Attraction> distanceAttractionMap = new TreeMap<>();

        attractions.forEach(attraction -> {
            distanceAttractionMap.put(this.gpsUtilsService.getDistance(userLocation.location, attraction), attraction);
        });

        return distanceAttractionMap;
    }


    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {

        return this.gpsUtilsService.getDistance(attraction, location) < this.proximityRange;
    }

    public boolean isNearAttraction(VisitedLocation visitedLocation, Attraction attraction) {

        return this.gpsUtilsService.getDistance(attraction, visitedLocation.location) < this.proximityBuffer;
    }

    public void calculateRewards(User user) {

        this.executorService.execute(() -> {
            Iterable<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
            List<Attraction>          attractions   = this.gpsUtilsService.getAllAttractions();

            for (VisitedLocation visitedLocation : userLocations) {

                for (Attraction attraction : attractions) {

                    if (user.getUserRewards()
                            .stream()
                            .noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {

                        if (this.isNearAttraction(visitedLocation, attraction)) {

                            user.addUserReward(new UserReward(visitedLocation, attraction,
                                                              this.rewardsService.getRewardPoints(attraction, user)
                            ));
                        }
                    }
                }
            }
        });
    }

    public ExecutorService getExecutorService() {

        return this.executorService;
    }

    public void waitAllWorkIsCompleted() {

        this.getExecutorService().shutdown();

        while (true) {

            boolean isFinish = this.getExecutorService().isTerminated();

            if (isFinish) {
                return;
            }
        }
    }

    public void setProximityRange(int customProximityRange) {

        this.proximityRange = customProximityRange;
    }

    public void updateUser(User user) {

        if (this.internalUserMap.containsKey(user.getUserName())) {

            this.internalUserMap.put(user.getUserName(), user);
        }
    }

    public Map<Double, Attraction> getFiveNearbyDistanceAttractions(User user) {

        return this.getDistanceAttractions(user);
    }
}
