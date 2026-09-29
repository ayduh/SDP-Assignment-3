# Shipping & Courier Aggregator - Bridge + Adapter

One platform, many couriers. Merchants choose **how** to deliver (Express, Bulk, Scheduled);
the system decides **which courier** carries it (Local, DHL, FedEx) from the destination country.

## Build, test, run (Java 17+, Maven)

```
mvn test              # builds and runs all 24 JUnit 5 tests
mvn compile exec:java # runs the demo (courier.Main)
```

## Where is each pattern?

| Role | Class |
|---|---|
| Bridge - Abstraction | `abstraction/Delivery` |
| Bridge - Refined Abstractions | `ExpressDelivery`, `BulkShipping`, `ScheduledDelivery` |
| Bridge - Implementor | `provider/CourierProvider` |
| Concrete Implementors | `DhlProvider`, `LocalCourierProvider` (native), `FedexAdapter` (adapted) |
| Adapter | `provider/FedexAdapter` |
| Adaptee (third-party, untouched) | `thirdparty/FedexSdkClient`, `FedexResponse` |
| Complexity module: dynamic implementor selection | `provider/ProviderSelector` |

## Documents

* `uml-class-diagram.png` UML
* `design-rationale.md` design rationale