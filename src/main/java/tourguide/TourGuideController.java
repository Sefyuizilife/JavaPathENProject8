package tourguide;

import com.jsoniter.output.JsonStream;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tourguide.dto.NearbyAttractionDTO;
import tourguide.service.TourGuideService;
import tourguide.user.User;
import tripPricer.Provider;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
public class TourGuideController {

    private final TourGuideService tourGuideService;

    public TourGuideController(TourGuideService tourGuideService) {

        this.tourGuideService = tourGuideService;
    }

    @RequestMapping("/")
    public String index() {

        return "Greetings from TourGuide!";
    }

    @RequestMapping(value = "/getLocation", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getLocation(@RequestParam String userName) {

        VisitedLocation visitedLocation;

        try {

            visitedLocation = this.tourGuideService.getUserLocation(this.getUser(userName)).get();
        } catch (InterruptedException | ExecutionException e) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return JsonStream.serialize(visitedLocation.location);
    }

    //  DONE: Change this method to no longer return a List of Attractions.
    //  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
    //  Return a new JSON object that contains:
    //  Name of Tourist attraction,
    //  Tourist attractions lat/long,
    //  The user's location lat/long,
    //  The distance in miles between the user's location and each of the attractions.
    //  The reward points for visiting each Attraction.
    //  Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping(value = "/getNearbyAttractions", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getNearbyAttractions(@RequestParam String userName) {

        User user = this.getUser(userName);

        Map<Double, Attraction> distanceAttractions = this.tourGuideService.getDistanceAttractions(user);

        Collection<NearbyAttractionDTO> nearbyAttractionDTOList = new ArrayList<>();

        distanceAttractions.forEach((distance, attraction) -> {

            if (nearbyAttractionDTOList.size() < 5) {
                nearbyAttractionDTOList.add(
                        new NearbyAttractionDTO(
                                attraction.attractionName,
                                new Location(attraction.latitude, attraction.longitude),
                                user.getLastVisitedLocation().location,
                                distance,
                                this.tourGuideService.getRewardPoints(attraction, user)
                        )
                );
            }
        });

        return JsonStream.serialize(nearbyAttractionDTOList);
    }

    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName) {

        return JsonStream.serialize(this.tourGuideService.getUserRewards(this.getUser(userName)));
    }

    // DONE: Get a list of every user's most recent location as JSON
    //- Note: does not use gpsUtil to query for their current location,
    //        but rather gathers the user's current location from their stored location history.
    //
    // Return object should be the just a JSON mapping of userId to Locations similar to:
    //     {
    //        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
    //        ...
    //     }
    @RequestMapping(value = "/getAllCurrentLocations", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllCurrentLocations() {

        Map<String, Location> userIdLastLocation = new HashMap<>();

        this.tourGuideService.getAllUsers()
                             .forEach(item ->
                                              userIdLastLocation.put(
                                                      item.getUserId().toString(),
                                                      item.getLastVisitedLocation().location
                                              )
                             );

        return JsonStream.serialize(userIdLastLocation);
    }

    @RequestMapping(value = "/getTripDeals", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTripDeals(@RequestParam String userName) {

        List<Provider> providers = this.tourGuideService.getTripDeals(this.getUser(userName));
        return JsonStream.serialize(providers);
    }

    @RequestMapping(value = "/users")
    private void updateUser(String userName) {

        this.tourGuideService.updateUser(this.getUser(userName));
    }

    private User getUser(String userName) {

        return this.tourGuideService.getUser(userName);
    }

}