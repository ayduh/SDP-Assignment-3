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

class BulkShippingTest {

    private final CourierProvider provider = mock(CourierProvider.class);

    @Test
    void booksEveryParcelAsStandard() throws Exception {
        when(provider.book(any())).thenReturn(new Shipment("Mock", "T-1", 5, LocalDate.now()));

        List<Shipment> result = new BulkShipping(provider)
                .send(List.of(new Parcel("DE", 1), new Parcel("DE", 2), new Parcel("DE", 3)));

        ArgumentCaptor<ShipmentRequest> captor = ArgumentCaptor.forClass(ShipmentRequest.class);
        verify(provider, times(3)).book(captor.capture());
        captor.getAllValues().forEach(r -> assertEquals(ServiceLevel.STANDARD, r.level()));
        assertEquals(3, result.size());
    }

    @Test
    void rejectsASingleParcelWithoutCallingProvider() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new BulkShipping(provider).send(List.of(new Parcel("DE", 1))));
        verify(provider, never()).book(any());
    }
}
