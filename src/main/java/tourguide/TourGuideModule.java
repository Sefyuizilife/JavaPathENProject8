package tourguide;

import gpsUtil.GpsUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rewardCentral.RewardCentral;
import tourguide.service.RewardsService;

@Configuration
public class TourGuideModule {

    @Bean
    public GpsUtil getGpsUtil() {

        return new GpsUtil();
    }

    @Bean
    public RewardsService getRewardsService() {

        return new RewardsService(this.getGpsUtil(), this.getRewardCentral());
    }

    @Bean
    public RewardCentral getRewardCentral() {

        return new RewardCentral();
    }

}
