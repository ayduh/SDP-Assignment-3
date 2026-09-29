package courier.provider;

import courier.model.*;
import courier.model.CourierException.Reason;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Native providers already speak the contract, so they are tested directly. */
class NativeProvidersTest {

    private ShipmentRequest req(String country, double kg, ServiceLevel level) {
        return new ShipmentRequest(new Parcel(country, kg), level, LocalDate.now());
    }

    @Test
    void dhlPricesExpressHigherThanStandard() throws Exception {
        DhlProvider dhl = new DhlProvider();
        double standard = dhl.book(req("DE", 10, ServiceLevel.STANDARD)).price();
        double express = dhl.book(req("DE", 10, ServiceLevel.EXPRESS)).price();
        assertTrue(express > standard);
    }

    @Test
    void localCourierRejectsForeignCountry() {
        CourierException e = assertThrows(CourierException.class,
                () -> new LocalCourierProvider().book(req("DE", 1, ServiceLevel.STANDARD)));
        assertEquals(Reason.INVALID_REQUEST, e.getReason());
    }

    @Test
    void dhlRejectsOverweightParcel() {
        CourierException e = assertThrows(CourierException.class,
                () -> new DhlProvider().book(req("DE", 71, ServiceLevel.STANDARD)));
        assertEquals(Reason.INVALID_REQUEST, e.getReason());
    }
}
