package courier.model;

import java.time.LocalDate;

// What every courier returns on success (the Implementor's output type)
public record Shipment(String provider, String trackingNumber, double price, LocalDate pickupDate) { }
