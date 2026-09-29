package courier.provider;

import courier.model.CourierException;
import courier.model.Shipment;
import courier.model.ShipmentRequest;

// BRIDGE - Implementor. The only thing the Abstraction side knows about couriers
public interface CourierProvider {

    String name();

    //Used for dynamic selection: does this courier deliver to that country?
    boolean supports(String countryCode);

    // Books a shipment. All failures are reported as CourierException
    Shipment book(ShipmentRequest request) throws CourierException;
}
