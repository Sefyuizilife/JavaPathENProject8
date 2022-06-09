package tourguide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourguide.user.User;
import tourguide.user.UserReward;

import java.util.List;

@Service
public class RewardsService {

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private final int           defaultProximityBuffer = 10;
    private final GpsUtil       gpsUtil;
    private final RewardCentral rewardsCentral;
    private       int           proximityBuffer        = this.defaultProximityBuffer;

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {

        this.gpsUtil        = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {

        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {

        this.proximityBuffer = this.defaultProximityBuffer;
    }

    public void calculateRewards(User user) {

        List<VisitedLocation> userLocations = user.getVisitedLocations();
        List<Attraction>      attractions   = this.gpsUtil.getAttractions();

        for (VisitedLocation visitedLocation : userLocations) {

            for (Attraction attraction : attractions) {

                if (user.getUserRewards()
                        .stream()
                        .noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {

                    if (this.isNearAttraction(visitedLocation, attraction)) {

                        user.addUserReward(new UserReward(visitedLocation, attraction,
                                                          this.getRewardPoints(attraction, user)
                        ));
                    }
                }
            }
        }
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {

        final int proximityRange = 200;
        return !(this.getDistance(attraction, location) > proximityRange);
    }

    private boolean isNearAttraction(VisitedLocation visitedLocation, Attraction attraction) {

        return !(this.getDistance(attraction, visitedLocation.location) > this.proximityBuffer);
    }

    private int getRewardPoints(Attraction attraction, User user) {

        return this.rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    public double getDistance(Location loc1, Location loc2) {

        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                                 + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }

}
