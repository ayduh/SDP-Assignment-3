package courier.abstraction;

import courier.model.CourierException;
import courier.model.Parcel;
import courier.model.Shipment;
import courier.model.ShipmentRequest;
import courier.model.ServiceLevel;
import courier.provider.CourierProvider;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

// BRIDGE Abstraction. Holds the "bridge" (the provider field). Depends ONLY on CourierProvider nno DHL, no FedEx, no adapter, no third-party class
public abstract class Delivery {

    protected final CourierProvider provider;

    protected Delivery(CourierProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    /** Each delivery type has its own business rules for how parcels are sent. */
    public abstract List<Shipment> send(List<Parcel> parcels) throws CourierException;

    /** Shared helper: build a request and hand it over the bridge. */
    protected Shipment book(Parcel parcel, ServiceLevel level, LocalDate pickupDate) throws CourierException {
        return provider.book(new ShipmentRequest(parcel, level, pickupDate));
    }
}
