package courier.provider;

import courier.model.CourierException;
import courier.model.CourierException.Reason;

import java.util.List;

/**
 * complexity Module: dynamic implementor selection.
 * Picks the courier at runtime from the input (destination country).
 * It only talks to CourierProvider, so the adapted courier is chosen exactly like the native ones.
 * Adding a new courier = write a class + add it to the list. This class never changes.
 */
public class ProviderSelector {

    private final List<CourierProvider> providers;

    public ProviderSelector(List<CourierProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public CourierProvider select(String countryCode) throws CourierException {
        return providers.stream()
                .filter(p -> p.supports(countryCode))
                .findFirst()
                .orElseThrow(() -> new CourierException(Reason.INVALID_REQUEST,
                        "No courier delivers to " + countryCode));
    }
}
