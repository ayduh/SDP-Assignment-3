package courier.abstraction;

import courier.model.CourierException;
import courier.model.Parcel;
import courier.model.ServiceLevel;
import courier.model.Shipment;
import courier.provider.CourierProvider;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// [BRIDGE] Refined Abstraction #3: standard speed, pickup on a chosen future date
public class ScheduledDelivery extends Delivery {

    private final LocalDate pickupDate;

    public ScheduledDelivery(CourierProvider provider, LocalDate pickupDate) {
        super(provider);
        if (pickupDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Pickup date cannot be in the past");
        this.pickupDate = pickupDate;
    }

    @Override
    public List<Shipment> send(List<Parcel> parcels) throws CourierException {
        List<Shipment> result = new ArrayList<>();
        for (Parcel p : parcels) result.add(book(p, ServiceLevel.STANDARD, pickupDate));
        return result;
    }
}
