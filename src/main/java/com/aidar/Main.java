package courier;

import courier.abstraction.BulkShipping;
import courier.abstraction.Delivery;
import courier.abstraction.ExpressDelivery;
import courier.abstraction.ScheduledDelivery;
import courier.model.CourierException;
import courier.model.Parcel;
import courier.model.Shipment;
import courier.provider.CourierProvider;
import courier.provider.DhlProvider;
import courier.provider.FedexAdapter;
import courier.provider.LocalCourierProvider;
import courier.provider.ProviderSelector;
import courier.thirdparty.FedexSdkClient;

import java.time.LocalDate;
import java.util.List;

// Demo. This is the ONLY place that names concrete couriers (composition root). */
public class Main {

    public static void main(String[] args) {
        ProviderSelector selector = new ProviderSelector(List.of(
                new LocalCourierProvider(),
                new DhlProvider(),
                new FedexAdapter(new FedexSdkClient())));

        LocalDate nextWeek = LocalDate.now().plusDays(7);

        // The courier is chosen from the input (country), never hard-coded here
        run(selector, "KZ", "Express, 2 kg to Kazakhstan",  new Parcel("KZ", 2),  c -> new ExpressDelivery(c));
        run(selector, "DE", "Bulk, 2 parcels to Germany",   null,                 c -> new BulkShipping(c));
        run(selector, "US", "Scheduled, 5 kg to the USA",   new Parcel("US", 5),  c -> new ScheduledDelivery(c, nextWeek));
        run(selector, "US", "Express, 90 kg to the USA (too heavy)", new Parcel("US", 90), c -> new ExpressDelivery(c));
        run(selector, "JP", "Express, 1 kg to Japan (no courier)",   new Parcel("JP", 1),  c -> new ExpressDelivery(c));
    }

    private static void run(ProviderSelector selector, String country, String title, Parcel parcel,
                            java.util.function.Function<CourierProvider, Delivery> deliveryType) {
        System.out.println("\n== " + title);
        try {
            CourierProvider provider = selector.select(country);          // dynamic selection
            Delivery delivery = deliveryType.apply(provider);              // bridge: abstraction + implementor
            List<Parcel> parcels = (parcel != null) ? List.of(parcel)
                                                    : List.of(new Parcel(country, 3), new Parcel(country, 8));
            for (Shipment s : delivery.send(parcels))
                System.out.printf("   %s  tracking=%s  price=%.2f  pickup=%s%n",
                        s.provider(), s.trackingNumber(), s.price(), s.pickupDate());
        } catch (CourierException e) {
            System.out.println("   FAILED [" + e.getReason() + "] " + e.getMessage());
        }
    }
}
