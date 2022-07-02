package tourguide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import tourguide.user.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class GpsUtilsService {

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private final int     defaultProximityBuffer = 10;
    private final GpsUtil gpsUtil;
    private       int     proximityBuffer        = this.defaultProximityBuffer;

    public GpsUtilsService(GpsUtil gpsUtil) {

        this.gpsUtil = gpsUtil;
    }

    public List<Attraction> getAllAttractions() {

        return this.gpsUtil.getAttractions();
    }

    public VisitedLocation getUserLocation(UUID userId) {

        return this.gpsUtil.getUserLocation(userId);
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

    public void generateUserLocationHistory(User user) {

        IntStream.range(0, 3)
                 .forEach(i -> user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(
                         this.generateRandomLatitude(), this.generateRandomLongitude()), this.getRandomTime())));
    }

    private Date getRandomTime() {

        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));

        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
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
}
