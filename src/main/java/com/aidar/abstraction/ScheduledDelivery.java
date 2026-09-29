package courier.abstraction;

import courier.model.CourierException;
import courier.model.Parcel;
import courier.model.ServiceLevel;
import courier.model.Shipment;
import courier.provider.CourierProvider;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// [BRIDGE] Refined Abstraction #1: fastest service, picked up today
public class ExpressDelivery extends Delivery {

    public ExpressDelivery(CourierProvider provider) { super(provider); }

    @Override
    public List<Shipment> send(List<Parcel> parcels) throws CourierException {
        List<Shipment> result = new ArrayList<>();
        for (Parcel p : parcels) result.add(book(p, ServiceLevel.EXPRESS, LocalDate.now()));
        return result;
    }
}
