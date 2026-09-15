# LLD Problem: Parking Lot

## Requirements (fixed scope — always pin these down first in a real interview)
- A parking lot has multiple **floors**, each floor has multiple **spots**.
- Spots come in different types: **Motorcycle, Compact, Large**.
- A vehicle can only park in a spot that fits its size — **smaller vehicles can use bigger spots,
  not vice versa** (a motorcycle can use a Large spot; a truck cannot use a Compact spot).
- Entry → system finds and assigns an available spot.
- Exit → spot is freed, and a **parking fee** is calculated based on duration.
- Track how many spots of each type are free.

---

## Step 1: Entity Identification (do this before writing any code, always)

| Entity | Why it's needed | Key attributes |
|---|---|---|
| `Vehicle` | The thing being parked | `vehicleId`, `type` |
| `ParkingSpot` | One individual, trackable space | `spotId`, `spotType`, `isOccupied` |
| `Floor` | Groups spots, searches within itself | `floorNo`, spots grouped by type |
| `ParkingTicket` | Represents one parking **session** | `ticketId`, `vehicle`, `spot`, `entryTime`, `exitTime` |
| `ParkingLot` | Top-level coordinator across all floors | `floors`, `feeStrategy` |
| `FeeStrategy` | Pricing logic, kept separate (SRP) | interface, swappable implementations |

### Why does `ParkingSpot` need to be its own entity, not just a count on `Floor`?
If `Floor` only stored a count like `int compactSpotsAvailable = 20`, there's no way to answer
"**which specific spot** did this car get?" — and no way to free the *correct* spot later when a
car leaves. A `ParkingTicket` needs a real `spotId` to know exactly where the vehicle is parked, and
`vacate()` needs to be called on one specific spot object, not on an abstract count. **Individual,
trackable identity requires an individual, trackable object** — a count can only ever tell you "how
many," never "which one."

### Why does `ParkingTicket` need to exist as its own entity, separate from `Vehicle`/`ParkingSpot`?
The requirement "calculate fee based on duration" needs **duration**, which needs **two timestamps**:
`entryTime` and `exitTime`. Neither `Vehicle` nor `ParkingSpot` naturally holds "when did THIS
specific visit start/end" — that's information about **one parking session**, not about the vehicle
or the spot as standalone things (the same vehicle might park many times; the same spot serves many
different vehicles over time). `ParkingTicket` is the entity that captures **one occurrence** of a
vehicle occupying a spot, for a bounded period of time.

### `entryTime` vs. `exitTime` — why is only one of them `final`?
- `entryTime` is known **completely, at the moment of construction** — the ticket is created exactly
  when the vehicle enters, so `entryTime` can be set once in the constructor and never needs to
  change again → `final long entryTime`.
- `exitTime` is **not known yet** at construction — the vehicle hasn't left. It must be filled in
  **later**, via a separate method (`markExit()`), potentially hours after the ticket was created.
  Since it genuinely needs a second, later write, it **cannot** be `final` (which only allows a
  single assignment, inside the constructor).

**General rule this illustrates:** `final` is for values that are completely known and fixed the
moment the object is built. If a field's real value can only become known *later*, after some event
happens, it cannot be `final` — no matter how tempting it is to lock everything down.

### Why store the **actual `Vehicle` object** on `ParkingTicket`, not just a `vehicleId` string?
Same reasoning as "why not store just spot IDs in `Floor`" (below) — storing only an ID means
anything that later needs vehicle details (type, etc.) would need a separate lookup mechanism
somewhere else. Storing the real object directly avoids that indirection, since the object is cheap
and already in hand at ticket-creation time.

### Why does `ParkingLot` need to exist, separate from `Floor`?
If floor 1 is completely full, **`Floor` has no way to even know floor 2 exists** — a `Floor`
object was only ever given its own list of spots, never a reference to sibling floors. Searching
across multiple floors requires something that holds the **full list of floors** — that's
`ParkingLot`'s job specifically. This isn't really an OCP issue (adding new floor types isn't the
concern) — it's a structural one: `Floor` literally cannot see beyond its own spot list, so
cross-floor coordination has to live one level up.

### Why is `FeeStrategy` a separate interface, not logic inside `ParkingLot`?
If pricing rules change (different rates for motorcycles vs. cars, weekend pricing, etc.), you don't
want to edit `ParkingLot.releaseVehicle()` every time — that's exactly the Strategy pattern
reasoning: pricing is a **swappable algorithm**, kept behind an interface so `ParkingLot` never
needs to change when pricing rules evolve (OCP).

---

## Q&A — every point of confusion from this build, explained in detail

### Q1: Why should `Floor` store a `List<ParkingSpot>` (real objects) instead of just a `List<String> spotIds`?
Storing only IDs doesn't actually save memory — it just moves the same objects somewhere else (you'd
still need a `Map<String, ParkingSpot>` to look up real spot data like `isOccupied`), adding an
extra layer of indirection for **zero actual savings**. Every parking spot has real, constantly
changing state (`isOccupied`) that must live in memory as an object regardless of how you reference
it. Storing IDs-only only makes sense when the referenced object is **expensive** and **rarely
needed** (e.g., fetched from a remote database occasionally) — neither is true here: `ParkingSpot`
is tiny and checked constantly.

**Follow-up confusion:** "can't we make `ParkingSpot` a method that takes an ID and returns
`isOccupied`?" — no, because a method needs an object to be called on. If `Floor` only has ID
strings, there's still no actual `ParkingSpot` object anywhere to check `isOccupied` on — you'd
need a map from ID → object regardless, which just brings you back to needing the real objects
stored somewhere.

### Q2: Why use an `enum` for `VehicleType`/`SpotType` instead of a `List<String>` or raw `String`?
A `List<String>` is just data — nothing stops invalid values (`"Motocycle"` typo,
`"motorcycle"` wrong case) from being used anywhere in the code; the compiler has zero awareness
that only specific values are valid. An `enum` makes the **compiler itself** enforce that only the
declared values can ever exist — a typo becomes a **compile error**, not a silent runtime bug.
Enums also give exhaustive `switch` checking (the compiler can confirm you've handled every case)
and safe `==` comparison (fast, reliable, no case-sensitivity risk).

**Case sensitivity, explained concretely:** `"CAR".equals("car")` returns `false` — Java treats
uppercase and lowercase letters as different characters. So `String`-based "types" can silently
create bugs like a car stored as `"car"` failing to match a check for `"CAR"` — no crash, just a
silently wrong branch taken. With an enum, there's exactly **one** valid spelling of each value,
enforced by the compiler — this class of bug becomes structurally impossible.

### Q3: `VehicleType` and `SpotType` have the same three value names (MOTORCYCLE, COMPACT, LARGE) — why not reuse one enum for both?
They currently share label names, but they represent **two different concepts** — `VehicleType`
describes a property of a `Vehicle`; `SpotType` describes a property of a `ParkingSpot`. They have
**different reasons to change** (an SRP-style argument): tomorrow, a new vehicle type (`SUV`) might
be added without any new spot type existing for it, or a new spot type (`ELECTRIC`, for EV
charging) might be added with no corresponding vehicle type. Coupling them into one shared enum
would force artificial, awkward growth of one concept just because the other needed a new value.
**Verdict: keep them as two separate enums**, even with matching names today.

### Q4: The "first available spot" bug — why is returning the first fitting spot wrong?
If `findAvailableSpot` just returns the first spot where `canFitVehicle()` is true, a motorcycle
could get assigned a `COMPACT` spot (which it also fits) even while a `MOTORCYCLE` spot sits empty
— simply because the compact spot happened to appear earlier in the list. This wastes a
scarcer/exact-fit resource on a vehicle that didn't need it, and could cause a real car to find no
compact spot later, even though one was "available" all along (just given away).

**The fix:** search by **priority order** — try the exact-fit spot type first, and only fall back to
a larger type if the exact-fit type is full:
- Motorcycle → try MOTORCYCLE, then COMPACT, then LARGE
- Compact (car) → try COMPACT, then LARGE
- Large (truck) → only LARGE

### Q5: Why `Map<SpotType, List<ParkingSpot>>` instead of `Map<SpotType, ParkingSpot>` or `Map<SpotType, Integer>` (a count)?
- `Map<SpotType, ParkingSpot>` can only hold **one spot per type** — but a real floor has many
  spots of the same type (e.g., 20 compact spots). A `Map` can't hold multiple values under one key
  without the value itself being a collection.
- `Map<SpotType, Integer>` (just a count) has the same flaw as Q1 (IDs-only) — a count tells you
  "how many," but never "**which** specific spot," and you can't construct a `ParkingTicket`
  (needs a real `spotId`) or call `.vacate()` on a specific object from a bare number. A count is
  also a **second source of truth** that could drift out of sync with the actual spot states if not
  updated perfectly in lockstep — an unnecessary risk when the real objects are already available.

**The correct structure:** `Map<SpotType, List<ParkingSpot>>` — grouped by type for fast,
priority-ordered lookup, but the value is a list of **real spot objects**, so you always have the
actual `spotId` and `isOccupied` state, no separate counter needed, no risk of drift.

### Q6: Isn't this the same as Observer pattern (calling multiple things one after another)?
No — see the Behavioral patterns notes for the full distinction. Quick recap: Observer broadcasts
the *same* method to a dynamic, unknown-sized list of *identical*-interface listeners. Here,
`findAvailableSpot` looping through spot types in priority order isn't a broadcast to listeners at
all — it's a **search with priority ordering**, checking a fixed, known structure. Different
shape entirely, just superficially "looping through things."

### Q7: Why can two decorator-style objects (or, here: why don't concurrent requests corrupt shared state)?
This came up while discussing `final`/immutability on wrapped references (from the Decorator
pattern), but the same reasoning applies to this whole codebase: **every request that calls
`parkVehicle()` operates on independent, already-existing `ParkingSpot`/`Floor`/`ParkingLot`
objects that are shared, but each request's local variables (`spot`, `ticket`) are its own,
thread-local data.** The only genuinely *shared, mutable* piece of state across concurrent requests
here is `ParkingSpot.isOccupied` and `ParkingLot.ticketCounter` — these ARE real candidates for a
race condition if two requests try to park into the **same spot** or increment the counter at the
**same instant**, and in a real production system, this code would need `synchronized` (or a
database-level atomic update) around `spot.occupy()` and `ticketCounter++`, exactly like the
`totalOrders` example from the Singleton notes. This implementation, as built, is a clean
**single-threaded correct design** — the multi-threaded safety layer would be a deliberate next
step, not something to assume for free.

### Q8: If a Spring `@Service` bean is a Singleton, doesn't that mean only one request can use it at a time?
No — this misunderstanding is worth restating clearly, since it applies directly to a `ParkingLot`
`@Service` too. Being a Singleton means **one shared object exists**; it says nothing about
requests being serialized/queued to use it. Multiple threads can call `parkVehicle()`
**simultaneously**, in parallel, on the same `ParkingLot` instance — each call's **local variables**
(the `vehicle` parameter, the local `spot` variable) are safe, thread-local copies. Waiting/blocking
only happens if you explicitly add `synchronized` around code that touches **shared mutable fields**
(like `isOccupied`, `ticketCounter`) — it is never automatic. Without explicit protection, concurrent
requests can silently corrupt shared state (e.g., two vehicles both being assigned the same spot) —
exactly the kind of bug this design would need to guard against before going into a real
multi-threaded production service.

---

## Bugs encountered and fixed during this build (useful to review before revising)

1. **`Floor` constructor: `NullPointerException`** — `spotsByType` map field was declared but never
   initialized with `new HashMap<>()` before being used.
2. **`Floor` constructor: `UnsupportedOperationException`** — used `List.of(spot)` (creates an
   **immutable** list) when first adding a spot type, then tried to `.add()` more spots to it later
   — crashes on the second spot of the same type. Fixed with `new ArrayList<>(...)`.
3. **`ParkingSpot.getSpotType()` return type mismatch** — field was `SpotType` but the getter was
   declared to return `String` — wouldn't compile.
4. **`ParkingTicket` constructor was `private`** — meant `ParkingLot` (which needs to create
   tickets) couldn't call it at all. Fixed to `public`.
5. **`HourlyFeeStrategy` — unit conversion bug** — divided milliseconds by `1000` only (→ seconds),
   forgetting to also divide by `60 * 60` to reach hours. Fixed divisor: `1000 * 60 * 60`.
6. **`HourlyFeeStrategy` — manual rounding bug** — tried to hand-roll "round up" with
   `(duration % multiplier == 0) ? ... : duration/multiplier + 1`. This is wrong for non-exact
   durations: e.g. 1hr 1min → `1.0166... + 1 = 2.0166...`, not a clean `2.0`. Even wrapping this in
   `Math.ceil()` afterward double-applies rounding to an already-wrong number. **Fix: apply
   `Math.ceil()` directly to the raw fractional division (`duration / multiplier`), with no manual
   branching at all** — `Math.ceil()` already fully solves "round up," and combining it with
   hand-written rounding logic corrupts the result.
7. **Integer-division truncation risk (general lesson, caught early)** — when dividing to get a
   fractional result (e.g., ms → hours), at least one operand must be a `double`/`float`
   (`1000.0`), or Java performs integer division and silently truncates the fractional part
   **before** any rounding function ever sees it.

---

## Final class list (all pieces, in build order)

```
VehicleType (enum)          — MOTORCYCLE, COMPACT, LARGE
Vehicle                     — vehicleId, type
SpotType (enum)              — MOTORCYCLE, COMPACT, LARGE (separate from VehicleType, same names, different concept)
ParkingSpot                  — spotId, spotType, isOccupied, occupy(), vacate(), canFitVehicle()
Floor                        — floorNo, Map<SpotType, List<ParkingSpot>>, findAvailableSpot() with priority order
ParkingTicket                 — ticketId, vehicle, spot, entryTime (final), exitTime (not final), markExit()
FeeStrategy (interface)       — calculateFee(ParkingTicket)
HourlyFeeStrategy             — Math.ceil(duration / (1000.0*60*60)) * ratePerHour
ParkingLot                    — floors, feeStrategy, ticketCounter, parkVehicle(), releaseVehicle()
```

## Full working code

```java
public enum VehicleType {
    MOTORCYCLE, COMPACT, LARGE
}

public class Vehicle {
    private final VehicleType vehicleType;
    private final String vehicleId;

    public Vehicle(VehicleType vehicleType, String vehicleId) {
        this.vehicleType = vehicleType;
        this.vehicleId = vehicleId;
    }

    public VehicleType getVehicleType() { return vehicleType; }
    public String getVehicleId() { return vehicleId; }
}

public enum SpotType {
    MOTORCYCLE, COMPACT, LARGE
}

public class ParkingSpot {
    private final String spotId;
    private final SpotType spotType;
    private boolean isOccupied;

    public ParkingSpot(String spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.isOccupied = false;
    }

    public String getSpotId() { return spotId; }
    public SpotType getSpotType() { return spotType; }
    public boolean isOccupied() { return isOccupied; }
    public void occupy() { isOccupied = true; }
    public void vacate() { isOccupied = false; }

    public boolean canFitVehicle(Vehicle vehicle) {
        if (isOccupied) return false;
        return switch (spotType) {
            case MOTORCYCLE -> vehicle.getVehicleType() == VehicleType.MOTORCYCLE;
            case COMPACT -> vehicle.getVehicleType() == VehicleType.COMPACT || vehicle.getVehicleType() == VehicleType.MOTORCYCLE;
            case LARGE -> true;
        };
    }
}

public class Floor {
    private final int floorNo;
    private final Map<SpotType, List<ParkingSpot>> spotsByType;

    public Floor(int floorNo, List<ParkingSpot> spots) {
        this.floorNo = floorNo;
        this.spotsByType = new HashMap<>();
        for (ParkingSpot spot : spots) {
            SpotType type = spot.getSpotType();
            if (spotsByType.containsKey(type)) {
                spotsByType.get(type).add(spot);
            } else {
                spotsByType.put(type, new ArrayList<>(List.of(spot)));
            }
        }
    }

    public int getFloorNo() { return floorNo; }

    public ParkingSpot findAvailableSpot(Vehicle vehicle) {
        List<SpotType> priorityOrder = getPriorityOrder(vehicle.getVehicleType());
        for (SpotType type : priorityOrder) {
            List<ParkingSpot> candidates = spotsByType.getOrDefault(type, List.of());
            for (ParkingSpot spot : candidates) {
                if (!spot.isOccupied()) return spot;
            }
        }
        return null;
    }

    private List<SpotType> getPriorityOrder(VehicleType vehicleType) {
        return switch (vehicleType) {
            case MOTORCYCLE -> List.of(SpotType.MOTORCYCLE, SpotType.COMPACT, SpotType.LARGE);
            case COMPACT -> List.of(SpotType.COMPACT, SpotType.LARGE);
            case LARGE -> List.of(SpotType.LARGE);
        };
    }
}

public class ParkingTicket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final long entryTime;
    private long exitTime;

    public ParkingTicket(String ticketId, Vehicle vehicle, ParkingSpot spot, long entryTime) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = entryTime;
    }

    public void markExit(long exitTime) { this.exitTime = exitTime; }
    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public long getEntryTime() { return entryTime; }
    public long getExitTime() { return exitTime; }
}

public interface FeeStrategy {
    double calculateFee(ParkingTicket parkingTicket);
}

public class HourlyFeeStrategy implements FeeStrategy {
    @Override
    public double calculateFee(ParkingTicket parkingTicket) {
        double duration = parkingTicket.getExitTime() - parkingTicket.getEntryTime();
        double multiplier = 1000 * 60 * 60;
        double durationInHours = Math.ceil(duration / multiplier);
        return 20 * durationInHours;
    }
}

public class ParkingLot {
    private final List<Floor> floors;
    private final FeeStrategy feeStrategy;
    private int ticketCounter;

    public ParkingLot(List<Floor> floors, FeeStrategy feeStrategy) {
        this.floors = floors;
        this.feeStrategy = feeStrategy;
        this.ticketCounter = 0;
    }

    public ParkingTicket parkVehicle(Vehicle vehicle) {
        for (Floor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicle);
            if (spot != null) {
                spot.occupy();
                ticketCounter++;
                String ticketId = "T" + ticketCounter;
                return new ParkingTicket(ticketId, vehicle, spot, System.currentTimeMillis());
            }
        }
        throw new IllegalStateException("No available spot for this vehicle: " + vehicle.getVehicleId());
    }

    public double releaseVehicle(ParkingTicket ticket) {
        ticket.markExit(System.currentTimeMillis());
        ticket.getSpot().vacate();
        return feeStrategy.calculateFee(ticket);
    }
}
```

## Patterns identified in this design (and where they were deliberately NOT forced)
- **Strategy** — `FeeStrategy` interface, swappable pricing implementations (`HourlyFeeStrategy`).
- **No Factory used** — spot/vehicle creation was simple enough that a factory would have added
  complexity without real benefit. Good reminder: **don't force a pattern where plain, clean SOLID
  design is already sufficient.**
- **No Singleton used directly in the domain model** — but `ParkingLot` would typically be exposed
  as a Spring `@Service` (Singleton-scoped) in a real application, which is where the earlier
  Singleton/thread-safety discussion becomes directly relevant to this exact system.
