package courier.provider;

import courier.model.*;
import courier.model.CourierException.Reason;
import courier.thirdparty.FedexResponse;
import courier.thirdparty.FedexSdkClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** The adaptee (FedexSdkClient) is mocked, so we test only the translation done by the adapter. */
class FedexAdapterTest {

    private final FedexSdkClient client = mock(FedexSdkClient.class);
    private final FedexAdapter adapter = new FedexAdapter(client);
    private final LocalDate date = LocalDate.of(2030, 1, 15);

    private ShipmentRequest request(ServiceLevel level) {
        return new ShipmentRequest(new Parcel("US", 5.0), level, date);
    }

    // ---------- success: input and output translation ----------

    @Test
    void translatesInputAndOutputOnSuccess() throws Exception {
        when(client.submitOrder(5000, "US", FedexSdkClient.SERVICE_OVERNIGHT, "2030-01-15"))
                .thenReturn(new FedexResponse(200, "OK", "FDX1", 7550));

        Shipment s = adapter.book(request(ServiceLevel.EXPRESS));

        assertEquals("FedEx", s.provider());
        assertEquals("FDX1", s.trackingNumber());
        assertEquals(75.50, s.price(), 0.001);       // cents -> price
        assertEquals(date, s.pickupDate());
    }

    @Test
    void standardLevelUsesGroundServiceCode() throws Exception {
        when(client.submitOrder(anyInt(), anyString(), eq(FedexSdkClient.SERVICE_GROUND), anyString()))
                .thenReturn(new FedexResponse(200, "OK", "FDX2", 3500));

        assertEquals("FDX2", adapter.book(request(ServiceLevel.STANDARD)).trackingNumber());
    }

    // ---------- failure translation: status code -> CourierException ----------

    @ParameterizedTest
    @CsvSource({
            "400, INVALID_REQUEST",
            "422, INVALID_REQUEST",
            "429, UNAVAILABLE",
            "500, UNAVAILABLE",
            "503, UNAVAILABLE",
            "401, REJECTED",
            "418, REJECTED"
    })
    void translatesStatusCodesToCourierException(int status, Reason expected) {
        when(client.submitOrder(anyInt(), anyString(), anyInt(), anyString()))
                .thenReturn(new FedexResponse(status, "problem", null, 0));

        CourierException e = assertThrows(CourierException.class, () -> adapter.book(request(ServiceLevel.STANDARD)));
        assertEquals(expected, e.getReason());
    }

    @Test
    void nullResponseBecomesUnavailable() {
        when(client.submitOrder(anyInt(), anyString(), anyInt(), anyString())).thenReturn(null);

        CourierException e = assertThrows(CourierException.class, () -> adapter.book(request(ServiceLevel.STANDARD)));
        assertEquals(Reason.UNAVAILABLE, e.getReason());
    }

    @Test
    void sdkRuntimeExceptionBecomesUnavailableAndKeepsCause() {
        RuntimeException boom = new IllegalStateException("socket closed");
        when(client.submitOrder(anyInt(), anyString(), anyInt(), anyString())).thenThrow(boom);

        CourierException e = assertThrows(CourierException.class, () -> adapter.book(request(ServiceLevel.STANDARD)));
        assertEquals(Reason.UNAVAILABLE, e.getReason());
        assertSame(boom, e.getCause());
    }

    @Test
    void onlyCourierExceptionEscapesTheAdapter() {
        when(client.submitOrder(anyInt(), anyString(), anyInt(), anyString()))
                .thenReturn(new FedexResponse(503, "maintenance", null, 0));

        // Any exception thrown must be exactly our type - nothing FedEx-specific.
        Exception e = assertThrows(Exception.class, () -> adapter.book(request(ServiceLevel.EXPRESS)));
        assertEquals(CourierException.class, e.getClass());
    }
}
