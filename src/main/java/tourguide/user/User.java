package tourguide.user;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class User {

    private final UUID                  userId;
    private final String                userName;
    private final List<VisitedLocation> visitedLocations = new ArrayList<>();
    private final List<UserReward>      userRewards      = new ArrayList<>();
    private       String                phoneNumber;
    private       String                emailAddress;
    private       Date                  latestLocationTimestamp;
    private       UserPreferences       userPreferences  = new UserPreferences();
    private       List<Provider>        tripDeals        = new ArrayList<>();

    public User(UUID userId, String userName, String phoneNumber, String emailAddress) {

        this.userId       = userId;
        this.userName     = userName;
        this.phoneNumber  = phoneNumber;
        this.emailAddress = emailAddress;
    }

    public UUID getUserId() {

        return this.userId;
    }

    public String getUserName() {

        return this.userName;
    }

    public String getPhoneNumber() {

        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {

        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {

        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {

        this.emailAddress = emailAddress;
    }

    public Date getLatestLocationTimestamp() {

        return this.latestLocationTimestamp;
    }

    public void setLatestLocationTimestamp(Date latestLocationTimestamp) {

        this.latestLocationTimestamp = latestLocationTimestamp;
    }

    public void addToVisitedLocations(VisitedLocation visitedLocation) {

        this.visitedLocations.add(visitedLocation);
    }

    public List<VisitedLocation> getVisitedLocations() {

        return this.visitedLocations;
    }

    public void clearVisitedLocations() {

        this.visitedLocations.clear();
    }

    public void addUserReward(UserReward userReward) {

        if (this.userRewards.stream()
                            .filter(r -> !r.attraction.attractionName.equals(userReward.attraction.attractionName))
                            .count() == 0) {
            this.userRewards.add(userReward);
        }
    }

    public List<UserReward> getUserRewards() {

        return this.userRewards;
    }

    public UserPreferences getUserPreferences() {

        return this.userPreferences;
    }

    public void setUserPreferences(UserPreferences userPreferences) {

        this.userPreferences = userPreferences;
    }

    public VisitedLocation getLastVisitedLocation() {

        return this.visitedLocations.get(this.visitedLocations.size() - 1);
    }

    public List<Provider> getTripDeals() {

        return this.tripDeals;
    }

    public void setTripDeals(List<Provider> tripDeals) {

        this.tripDeals = tripDeals;
    }

}
