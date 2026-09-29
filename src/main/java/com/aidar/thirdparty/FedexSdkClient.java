package com.aidar.thirdparty;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulated third-party SDK. We do NOT own this code and it is NEVER modified.
 * Incompatible with CourierProvider in four ways:
 *   1. method name      : submitOrder(...)        vs  book(...)
 *   2. parameters       : (int grams, String iso, int serviceCode, String isoDate)
 *                         vs one ShipmentRequest object (kg as double, enum, LocalDate)
 *   3. result           : FedexResponse with cents vs Shipment with price
 *   4. failure mechanism: status codes inside the response (never throws)
 *                         vs CourierException
 */
public class FedexSdkClient {

    public static final int SERVICE_GROUND = 1;
    public static final int SERVICE_OVERNIGHT = 2;

    public static final int STATUS_OK = 200;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNPROCESSABLE = 422;
    public static final int STATUS_RATE_LIMITED = 429;
    public static final int STATUS_UNAVAILABLE = 503;

    private final AtomicInteger counter = new AtomicInteger(9000);

    public FedexResponse submitOrder(int weightGrams, String isoCountry, int serviceCode, String pickupDateIso) {
        if (!"US".equals(isoCountry) && !"CA".equals(isoCountry))
            return new FedexResponse(STATUS_BAD_REQUEST, "Unknown destination", null, 0);
        if (weightGrams <= 0 || weightGrams > 68_000)
            return new FedexResponse(STATUS_UNPROCESSABLE, "Weight out of range", null, 0);

        int cents = 3000 + weightGrams / 10;
        if (serviceCode == SERVICE_OVERNIGHT) cents *= 2;
        return new FedexResponse(STATUS_OK, "OK", "FDX" + counter.incrementAndGet(), cents);
    }
}
