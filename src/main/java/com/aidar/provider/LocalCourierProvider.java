package com.aidar.provider;

import com.aidar.model.CourierException;
import com.aidar.model.CourierException.Reason;
import com.aidar.model.ServiceLevel;
import com.aidar.model.Shipment;
import com.aidar.model.ShipmentRequest;

import java.util.concurrent.atomic.AtomicInteger;

/** BRIDGE - Concrete Implementor #2 (native): domestic (KZ) only, up to 20 kg. */
public class LocalCourierProvider implements CourierProvider {

    private static final double MAX_KG = 20;
    private final AtomicInteger counter = new AtomicInteger(5000);

    @Override public String name() { return "LocalCourier"; }

    @Override public boolean supports(String countryCode) { return "KZ".equals(countryCode); }

    @Override
    public Shipment book(ShipmentRequest r) throws CourierException {
        double kg = r.parcel().weightKg();
        if (!supports(r.parcel().destinationCountry()))
            throw new CourierException(Reason.INVALID_REQUEST, "LocalCourier delivers only inside KZ");
        if (kg > MAX_KG)
            throw new CourierException(Reason.INVALID_REQUEST, "LocalCourier max weight is " + MAX_KG + " kg");

        double price = 3 + 0.5 * kg;
        if (r.level() == ServiceLevel.EXPRESS) price *= 2;
        return new Shipment(name(), "LOC-" + counter.incrementAndGet(), price, r.pickupDate());
    }
}
