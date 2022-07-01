package tourguide.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

@Service
public class TripPricerService {

    private final TripPricer tripPricer;
    @Value("${tripPricer.api.key:test-server-api-key}")
    private       String     testServerApiKey;

    public TripPricerService(TripPricer tripPricer) {

        this.tripPricer = tripPricer;
    }

    public List<Provider> getPrice(UUID attractionId, int adults, int children, int nightsStay, int rewardsPoints) {

        return this.tripPricer.getPrice(
                this.testServerApiKey, attractionId, adults, children, nightsStay, rewardsPoints);
    }
}
