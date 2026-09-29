# Design Rationale - Shipping & Courier Aggregator

## 1. Problem
E-commerce merchants want to ship parcels without caring which courier carries them. The platform offers
three **delivery types** (Express, Bulk, Scheduled) and works with several **couriers** (Local courier, DHL,
FedEx). Delivery types and couriers change independently: the business adds a new delivery type
(e.g. Same-day) or a new courier (e.g. UPS) at different times and for different reasons.

## 2. Why Bridge alone is not enough
Bridge splits the code into `Delivery` (what kind of delivery) and `CourierProvider` (who carries it), so
3 delivery types x 3 couriers needs 3 + 3 = 6 classes, not 9 (and the gap grows with every addition).
But Bridge assumes every courier already implements `CourierProvider`. Real couriers do not: FedEx ships an
SDK we cannot edit. Bridge alone gives no way to plug such a class in.

## 3. Why Adapter alone is not enough
Adapter makes one FedEx SDK look like `CourierProvider`, but it does not organise the business side. Without
Bridge, Express/Bulk/Scheduled logic would be duplicated per courier (`ExpressDhl`, `ExpressFedex`, ...) or
mixed with courier code. Bridge keeps the delivery rules in one place; Adapter keeps the vendor mess at the
edge. Each pattern solves a different half of the problem, and the Adapter fills the Bridge's Implementor slot.

## 4. Why the wrapped class is genuinely incompatible
`FedexSdkClient` differs from `CourierProvider` in more than a name:

| | `CourierProvider` (ours) | `FedexSdkClient` (third party) |
|---|---|---|
| Method | `book(ShipmentRequest)` | `submitOrder(int, String, int, String)` |
| Parameters | one object: weight in **kg** (double), `ServiceLevel` enum, `LocalDate` | weight in **grams** (int), service **code** (int), date as ISO **String** - different order and types |
| Result | `Shipment` with price in currency units | `FedexResponse` with cost in **cents** |
| Failure | throws `CourierException` (with a `Reason`) | never throws; returns a **status code** inside the response (or null) |

`FedexAdapter` translates the input, the output and every failure: 400/422 -> `INVALID_REQUEST`;
429 and 5xx -> `UNAVAILABLE`; any other code -> `REJECTED`; null response or an SDK `RuntimeException`
-> `UNAVAILABLE` (cause kept). No FedEx constant, type or code leaves the adapter; `Delivery` and its
subclasses never import anything from `courier.thirdparty` (checked by `grep`, see defence notes).

## 5. Required complexity module: **Dynamic implementor selection**
`ProviderSelector` receives all `CourierProvider`s and picks the first one whose `supports(country)` is true.
KZ -> LocalCourier, DE/FR/... -> DHL, US/CA -> FedEx (adapted). The client code never names a courier, and
the adapted courier is selected exactly like the native ones because the selector only sees the interface.

## 6. Open/Closed Principle on both axes
* **New abstraction** (e.g. `SameDayDelivery extends Delivery`): one new class; no existing class changes.
* **New implementor** (e.g. `UpsProvider implements CourierProvider`): one new class, registered in the
  provider list in `Main` (the composition root); `Delivery`, all refined abstractions and `ProviderSelector`
  stay untouched.

## 7. One limitation
`BulkShipping` books parcels one by one. If parcel 3 of 5 fails, parcels 1-2 are already booked and there is
no rollback, because `CourierProvider` has no `cancel` operation. A fix would add `cancel(trackingNumber)`
to the interface (and to the adapter, mapped to the SDK's own cancel call) so Bulk could compensate.
