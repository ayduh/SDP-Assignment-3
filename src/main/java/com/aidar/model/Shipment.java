package courier.model;

/**
 * The ONE failure type of the Implementor contract
 * Every provider (native or adapted) reports problems only through this class
 */
public class CourierException extends Exception {

    public enum Reason {
        INVALID_REQUEST,  // our request is wrong (bad weight, unsupported country)
        UNAVAILABLE,      // courier is down, overloaded or unreachable so retry later
        REJECTED          // courier refused for any other reason
    }

    private final Reason reason;

    public CourierException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public CourierException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() { return reason; }
}
