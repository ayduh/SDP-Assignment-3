package courier.provider;

import courier.model.CourierException;
import courier.model.CourierException.Reason;
import courier.model.ServiceLevel;
import courier.model.Shipment;
import courier.model.ShipmentRequest;
import courier.thirdparty.FedexResponse;
import courier.thirdparty.FedexSdkClient;

import java.util.Set;

/**
 * ADAPTER + BRIDGE Concrete Implementor #3
 * Wraps the incompatible FedexSdkClient so it fits CourierProviderr
 * All FedEx-specific things (codes, units, constants) stop here
 */
public class FedexAdapter implements CourierProvider {

    private static final Set<String> COUNTRIES = Set.of("US", "CA");
    private final FedexSdkClient client;   // the adaptee

    public FedexAdapter(FedexSdkClient client) { this.client = client; }

    @Override public String name() { return "FedEx"; }

    @Override public boolean supports(String countryCode) { return COUNTRIES.contains(countryCode); }

    @Override
    public Shipment book(ShipmentRequest r) throws CourierException {
        // 1) translate INPUT: kg -> grams, enum -> service code, LocalDate -> ISO string
        int grams = (int) Math.round(r.parcel().weightKg() * 1000);
        int serviceCode = (r.level() == ServiceLevel.EXPRESS)
                ? FedexSdkClient.SERVICE_OVERNIGHT
                : FedexSdkClient.SERVICE_GROUND;

        FedexResponse response;
        try {
            response = client.submitOrder(grams, r.parcel().destinationCountry(),
                                          serviceCode, r.pickupDate().toString());
        } catch (RuntimeException e) {                       // SDK crashed / network error
            throw new CourierException(Reason.UNAVAILABLE, "FedEx is unreachable", e);
        }

        // 2) translate FAILURE: status codes -> CourierException
        if (response == null)
            throw new CourierException(Reason.UNAVAILABLE, "FedEx returned no answer");

        int status = response.statusCode();
        if (status != FedexSdkClient.STATUS_OK) {
            throw new CourierException(reasonFor(status), "FedEx: " + response.message());
        }

        // 3) translate OUTPUT: cents -> price, FedexResponse -> Shipment
        return new Shipment(name(), response.trackingId(), response.costCents() / 100.0, r.pickupDate());
    }

    private static Reason reasonFor(int status) {
        if (status == FedexSdkClient.STATUS_BAD_REQUEST || status == FedexSdkClient.STATUS_UNPROCESSABLE)
            return Reason.INVALID_REQUEST;
        if (status == FedexSdkClient.STATUS_RATE_LIMITED || status >= 500)
            return Reason.UNAVAILABLE;
        return Reason.REJECTED;
    }
}
