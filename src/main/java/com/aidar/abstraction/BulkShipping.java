package com.aidar.abstraction;

import com.aidar.model.CourierException;
import com.aidar.model.Parcel;
import com.aidar.model.ServiceLevel;
import com.aidar.model.Shipment;
import com.aidar.provider.CourierProvider;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** BRIDGE - Refined Abstraction #2: many parcels at once, standard speed. Needs 2+ parcels. */
public class BulkShipping extends Delivery {

    public BulkShipping(CourierProvider provider) { super(provider); }

    @Override
    public List<Shipment> send(List<Parcel> parcels) throws CourierException {
        if (parcels.size() < 2)
            throw new IllegalArgumentException("Bulk shipping needs at least 2 parcels");
        List<Shipment> result = new ArrayList<>();
        for (Parcel p : parcels) result.add(book(p, ServiceLevel.STANDARD, LocalDate.now()));
        return result;
    }
}
