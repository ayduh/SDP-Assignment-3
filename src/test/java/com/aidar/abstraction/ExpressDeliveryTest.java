package courier.abstraction;

import courier.model.*;
import courier.provider.CourierProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScheduledDeliveryTest {

    private final CourierProvider provider = mock(CourierProvider.class);

    @Test
    void passesChosenPickupDateToProvider() throws Exception {
        LocalDate date = LocalDate.now().plusDays(5);
        when(provider.book(any())).thenReturn(new Shipment("Mock", "T-1", 5, date));

        new ScheduledDelivery(provider, date).send(List.of(new Parcel("US", 4)));

        ArgumentCaptor<ShipmentRequest> captor = ArgumentCaptor.forClass(ShipmentRequest.class);
        verify(provider).book(captor.capture());
        assertEquals(date, captor.getValue().pickupDate());
        assertEquals(ServiceLevel.STANDARD, captor.getValue().level());
    }

    @Test
    void rejectsPastPickupDate() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledDelivery(provider, LocalDate.now().minusDays(1)));
    }
}
