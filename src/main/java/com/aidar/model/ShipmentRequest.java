package courier.model;

// A parcel to deliver. Country is an ISO code such as "KZ", "DE", "US".
public record Parcel(String destinationCountry, double weightKg) {
    public Parcel {
        if (destinationCountry == null || destinationCountry.isBlank())
            throw new IllegalArgumentException("destination country is required");
        if (weightKg <= 0)
            throw new IllegalArgumentException("weight must be positive");
    }
}
