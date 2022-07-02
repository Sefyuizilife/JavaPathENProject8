package tourguide.dto;

import gpsUtil.location.Location;

public class NearbyAttractionDTO implements Comparable<NearbyAttractionDTO> {

    private String   attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private Double   distance;
    private Integer  rewardPoint;

    public NearbyAttractionDTO(String attractionName, Location attractionLocation, Location userLocation, Double distance, Integer rewardPoint) {

        this.attractionName     = attractionName;
        this.attractionLocation = attractionLocation;
        this.userLocation       = userLocation;
        this.distance           = distance;
        this.rewardPoint        = rewardPoint;
    }

    public String getAttractionName() {

        return this.attractionName;
    }

    public void setAttractionName(String attractionName) {

        this.attractionName = attractionName;
    }

    public Location getAttractionLocation() {

        return this.attractionLocation;
    }

    public void setAttractionLocation(Location attractionLocation) {

        this.attractionLocation = attractionLocation;
    }

    public Location getUserLocation() {

        return this.userLocation;
    }

    public void setUserLocation(Location userLocation) {

        this.userLocation = userLocation;
    }

    public Double getDistance() {

        return this.distance;
    }

    public void setDistance(Double distance) {

        this.distance = distance;
    }

    public Integer getRewardPoint() {

        return this.rewardPoint;
    }

    public void setRewardPoint(Integer rewardPoint) {

        this.rewardPoint = rewardPoint;
    }

    @Override
    public int compareTo(NearbyAttractionDTO o) {

        return this.getDistance().compareTo(o.getDistance());
    }
}
