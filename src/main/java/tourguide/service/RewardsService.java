package tourguide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourguide.user.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardsService {

    private final RewardCentral   rewardsCentral;

    public RewardsService(RewardCentral rewardCentral) {

        this.rewardsCentral = rewardCentral;
    }

    public int getRewardPoints(Attraction attraction, User user) {

        return this.rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }
}
