package com.aidar.provider;

import com.aidar.model.CourierException;
import com.aidar.model.CourierException.Reason;
import com.aidar.model.ServiceLevel;
import com.aidar.model.Shipment;
import com.aidar.model.ShipmentRequest;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** BRIDGE - Concrete Implementor #1 (native): Europe, up to 70 kg. */
public class DhlProvider implements CourierProvider {

    private static final Set<String> COUNTRIES = Set.of("DE", "FR", "IT", "ES", "PL", "NL");
    private static final double MAX_KG = 70;
    private final AtomicInteger counter = new AtomicInteger(1000);

    @Override public String name() { return "DHL"; }

    @Override public boolean supports(String countryCode) { return COUNTRIES.contains(countryCode); }

    @Override
    public Shipment book(ShipmentRequest r) throws CourierException {
        double kg = r.parcel().weightKg();
        if (!supports(r.parcel().destinationCountry()))
            throw new CourierException(Reason.INVALID_REQUEST, "DHL does not deliver to " + r.parcel().destinationCountry());
        if (kg > MAX_KG)
            throw new CourierException(Reason.INVALID_REQUEST, "DHL max weight is " + MAX_KG + " kg");

        double price = 20 + 5 * kg;
        if (r.level() == ServiceLevel.EXPRESS) price *= 1.5;
        return new Shipment(name(), "DHL-" + counter.incrementAndGet(), price, r.pickupDate());
    }
}
