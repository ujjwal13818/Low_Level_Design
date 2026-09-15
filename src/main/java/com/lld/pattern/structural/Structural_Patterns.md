# Structural Design Patterns

## Why are they called "Structural"?

Recall the three pattern categories:

| Category | What varies / what it solves |
|---|---|
| **Creational** | *How objects are created* |
| **Structural** | *How objects/classes are composed together to form a larger structure* |
| **Behavioral** | *How objects communicate, interact, and change behavior at runtime* |

**Structural patterns** are named for exactly this: they're about **the static shape/structure** you
build by combining objects and classes — who holds a reference to whom, who wraps whom, who
implements what. The focus is on **composition** ("has-a" relationships) and sometimes inheritance,
to build a larger, more useful structure out of smaller pieces — **not** about runtime
decision-making or reacting to events (that's Behavioral).

**The general mechanic behind every Structural pattern:** one class holds a **reference to another
object** as a field (composition, a "has-a" relationship), and uses that reference internally —
either to translate its interface (Adapter), add behavior on top of it (Decorator), or hide a group
of them behind one simpler interface (Facade). Contrast this with Behavioral patterns, where the
held reference is usually **swapped at runtime** (State) or **broadcast to** (Observer) — in
Structural patterns, the composition is more often **fixed at construction time**, forming a
permanent structural relationship.

---

## 1. Adapter Pattern

### Why called "Adapter"
Same idea as a physical travel plug adapter — it doesn't change what your laptop charger *does*,
it just changes the **shape/interface** so an incompatible plug fits an incompatible socket. In
code: it takes an existing class with the "wrong shape" of interface and wraps it so it fits the
shape your code actually expects — nothing about the underlying behavior changes, just the shape
of how you call it.

### What problem it solves
You have an existing class (often third-party, legacy, or something you can't modify) whose
interface **doesn't match** what your code expects. Adapter wraps that incompatible interface and
translates calls into the interface your code needs.

### If NOT used — problems
- Client code (`ExerciseService`, `PaymentProcessor`, etc.) ends up directly coupled to the
  incompatible class's exact method names/signatures/data formats — violates **DIP** (depending on
  a concrete, awkward detail instead of a clean abstraction).
- If the incompatible class's API changes, or you add a second/third incompatible provider, you
  have to edit client code again and again — violates **OCP**.
- Parsing/translation logic (e.g., converting `double` to a `String` in cents) gets scattered
  wherever the incompatible class is used, instead of centralized in one place.

### Benefits
- Client code only ever talks to a clean, consistent interface it controls (`PaymentGateway`,
  `ExerciseProvider`) — it has **zero knowledge** that the incompatible class even exists.
- Swap providers/legacy systems freely by writing a new adapter — client code never changes.
- All the messy translation/parsing logic lives in exactly one place: the adapter.

### Real example (from your own code)
```java
public interface PaymentGateway {
    void processPayment(double amount);
}

// Incompatible legacy class — different signature, different units (cents, String)
public class OldPaymentSystem {
    public void makeTransaction(String amountInCents) {
        System.out.println("Making transaction: " + amountInCents + " cents");
    }
}

// Adapter — translates PaymentGateway calls into OldPaymentSystem calls
public class PaymentGatewayAdapter implements PaymentGateway {
    private final OldPaymentSystem oldPaymentSystem;

    public PaymentGatewayAdapter(OldPaymentSystem oldPaymentSystem) {
        this.oldPaymentSystem = oldPaymentSystem;
    }

    public void processPayment(double amount) {
        String amountInCents = String.valueOf((int) (amount * 100));
        oldPaymentSystem.makeTransaction(amountInCents);
    }
}

// Client code — references the INTERFACE, never the concrete adapter or OldPaymentSystem
PaymentGateway gateway = new PaymentGatewayAdapter(new OldPaymentSystem());
gateway.processPayment(150.50);
```

### Interview weightage: ⭐⭐⭐⭐ (High)
Comes up whenever a question involves integrating a third-party/legacy system — "design a payment
system that supports multiple providers," "integrate with an external API." Also a good pattern to
mention when discussing real-world migrations (e.g., moving between library versions without
breaking existing callers).

---

## 2. Decorator Pattern

### Why called "Decorator"
Like decorating a base object with layers — each "decoration" adds something extra **on top of**
what's already there, without changing the object underneath. Just like wrapping a gift: the gift
itself (`BasicCoffee`) doesn't change, but each layer of wrapping paper (`MilkDecorator`,
`SugarDecorator`) adds something visible/functional on top, and you can stack as many layers as
you want.

### What problem it solves
You want to add new behavior/responsibilities to an object **dynamically, at runtime**, without
modifying its class or creating a new subclass for every possible combination of features.

### The core mechanic
A Decorator **implements the same interface** as the object it wraps, **and holds a reference to an
object of that same interface** inside itself. This lets you stack decorators arbitrarily — wrap a
decorator around an object, then wrap another decorator around that decorator — and the outside
world can't tell the difference, because everything still satisfies the same interface.

### Why the abstract base class (`CoffeeDecorator` / `PizzaDecorator`) exists
Without it, every concrete decorator (`MilkDecorator`, `SugarDecorator`, `CheeseTopping`,
`OliveTopping`) would have to repeat identical boilerplate: declaring the wrapped-object field and
writing the same constructor to store it. The abstract class writes that shared "hold a wrapped
object" plumbing **once**. It's marked `abstract` (not a normal concrete class) because it
deliberately does **not** implement `cost()`/`description()` itself — those are left for each
concrete decorator to define uniquely. Since a bare `CoffeeDecorator` would have no meaningful
`cost()`, `abstract` prevents anyone from ever doing `new CoffeeDecorator(...)` directly — the
compiler enforces that only complete, meaningful subclasses can be instantiated.

### `final` on the wrapped reference — why, and does it cause concurrency problems?
```java
protected final Coffee wrappedCoffee;
```
`final` just means this specific field can never be reassigned to a *different* object after
construction — it does not freeze the object it points to, it only locks the reference itself.
This is used because a `MilkDecorator` should always wrap the exact same coffee it was built
with — there's never a legitimate reason to swap it out later (swapping-a-reference is what State
pattern does, not Decorator).

**Concurrency:** this does NOT cause problems with concurrent requests — the opposite, actually.
Every request builds its own **independent object graph** (`new SugarDecorator(new
MilkDecorator(new BasicCoffee()))`), so two concurrent requests never share the same decorator
chain — each `new` call allocates fresh objects, nothing is shared. Because the fields are `final`
and never mutated after construction, these objects are effectively **immutable**, which makes them
automatically thread-safe with zero extra effort (no locks/synchronization needed). Real
concurrency concerns only arise with **shared, mutable** state — e.g., a mutable counter field on a
singleton Spring `@Service` bean — which is an entirely different scenario from this wrapped
reference.

### If NOT used — problems
- Subclass explosion: every combination of add-ons needs its own subclass
  (`CoffeeWithMilk`, `CoffeeWithSugar`, `CoffeeWithMilkAndSugar`, ...) — grows combinatorially
  with each new add-on (4 add-ons → up to 16 subclasses).
- Can't add/remove behavior at runtime — it's baked into the class hierarchy at compile time.

### Benefits
- Add new "toppings"/behaviors by writing exactly one new decorator class — no combinatorial
  subclassing.
- Behaviors can be combined in any order, any combination, decided at runtime by the client.
- Each layer only needs to know about the interface, not the whole chain beneath it.

### Real example (from your own code)
```java
public interface Pizza {
    double cost();
    String description();
}

public class PlainPizza implements Pizza {
    public double cost() { return 150.00; }
    public String description() { return "Plain Pizza"; }
}

public abstract class PizzaDecorator implements Pizza {
    protected final Pizza wrappedPizza;
    public PizzaDecorator(Pizza wrappedPizza) { this.wrappedPizza = wrappedPizza; }
}

public class CheeseTopping extends PizzaDecorator {
    public CheeseTopping(Pizza wrappedPizza) { super(wrappedPizza); }
    public double cost() { return wrappedPizza.cost() + 40.00; }
    public String description() { return wrappedPizza.description() + ", Cheese"; }
}

public class OliveTopping extends PizzaDecorator {
    public OliveTopping(Pizza wrappedPizza) { super(wrappedPizza); }
    public double cost() { return wrappedPizza.cost() + 25.00; }
    public String description() { return wrappedPizza.description() + ", Olives"; }
}

// Stack decorators — all layers stay alive and contribute simultaneously
Pizza completePizza = new OliveTopping(new CheeseTopping(new PlainPizza()));
// description(): "Plain Pizza, Cheese, Olives"    cost(): 215.0
```

### Decorator vs. State — the distinction that matters (asked and answered in this conversation)
Both use interfaces + runtime polymorphism, but the mechanism is opposite:

| | State | Decorator |
|---|---|---|
| How many "behavior objects" active at once? | Exactly ONE — old one discarded on transition | MANY — all layers stay alive simultaneously |
| Is behavior swapped or stacked? | Swapped (replaced) | Stacked (cumulative, wraps around previous layer) |
| What triggers the change? | Internal lifecycle/event (workout paused, etc.) | Client explicitly composes layers upfront, before use |
| Concrete test | Old behavior discarded? → State | Old behavior wrapped underneath, still contributing? → Decorator |

### Decorator vs. Adapter — the distinction that matters
- **Adapter**: translates ONE incompatible interface into another (different shape in, different
  shape out) — used to make something **compatible**.
- **Decorator**: wraps an object of the **same interface** to add behavior, layer by layer — the
  shape never changes, only what happens when you call it. Used to **enhance**, not to fix
  incompatibility.

### Interview weightage: ⭐⭐⭐⭐ (High)
Classic in "design a coffee shop ordering system," "design a pizza ordering system," and comes up
in real frameworks too (Java's `BufferedReader(new FileReader(...))` is literally Decorator pattern
in the standard library — good one to mention in an interview to show real-world recognition).

---

## 3. Facade Pattern

### Why called "Facade"
A facade, architecturally, is the **outward-facing wall of a building** — it presents a clean,
simple face to the outside world while hiding all the complex wiring, plumbing, and structure
behind it. Same idea in code: the Facade class presents one simple method call to the outside
world, while hiding the complex orchestration of multiple subsystems behind it.

### What problem it solves
A system has multiple complex subsystems that need to be used together, often in a specific
sequence, to accomplish one conceptual task. Forcing every client to know how to orchestrate all
of them directly makes client code messy, repetitive, and tightly coupled to internal subsystem
details.

### If NOT used — problems
- Every entry point that needs to perform the task (e.g., "end a workout," "watch a movie") has to
  know all the subsystems involved, their exact APIs, and the correct calling order.
- Duplicated orchestration logic across multiple callers (e.g., a controller AND an admin tool) —
  if the process changes, you have to find and update every duplicate, which will eventually drift
  out of sync.
- Client code becomes tightly coupled to low-level subsystem details it shouldn't need to know
  about at all.

### Benefits
- Callers only need to know **one simple method** (`watchMovie()`, `completeWorkout()`) — all
  subsystem complexity is centralized and hidden.
- If the orchestration order changes, or a new subsystem is added, you edit the facade **once**,
  and every caller benefits automatically with no changes on their end.
- Reduces coupling between client code and the subsystems' internal APIs.

### Real example (from your own code)
```java
public class Projecter {
    public void turnOn() { System.out.println("Projecter is on"); }
    public void turnOff() { System.out.println("Projecter is off"); }
}
public class SoundSystem {
    public void turnOn() { System.out.println("System is turned on"); }
    public void turnOff() { System.out.println("System is turned off"); }
    public void setVolume(double volume) { System.out.println("System is set volume " + volume); }
}
public class DvdPlayer {
    public void play(String movie) { System.out.println("DVD is playing " + movie); }
    public void stop() { System.out.println("DVD is stopped"); }
}

public class HomeTheatreFacade {
    private final Projecter projecter = new Projecter();
    private final SoundSystem soundSystem = new SoundSystem();
    private final DvdPlayer dvdPlayer = new DvdPlayer();

    public void watchMovie(String movie) {
        projecter.turnOn();
        soundSystem.turnOn();
        soundSystem.setVolume(50);
        dvdPlayer.play(movie);
    }

    public void endMovie() {
        projecter.turnOff();
        soundSystem.turnOff();
        dvdPlayer.stop();
    }
}

// Client only ever touches the facade — never the subsystems directly
HomeTheatreFacade homeTheatreFacade = new HomeTheatreFacade();
homeTheatreFacade.watchMovie("Inception");
homeTheatreFacade.endMovie();
```

### Important implementation detail — field visibility
The subsystem fields (`projecter`, `soundSystem`, `dvdPlayer`) **must be `private`**, not
`public`. If they're `public`, outside code could bypass the facade entirely
(`homeTheatreFacade.projecter.turnOn()`), which breaks the entire guarantee the pattern is supposed
to provide — that the facade is the **only** entry point. This is a real hole in the pattern's
contract if missed, not just a style nitpick.

### Facade vs. Observer — the distinction that matters (asked and answered in this conversation)
Surface similarity: "calling multiple things one after another" in both. But the shape underneath
is different:

| | Observer | Facade |
|---|---|---|
| How many objects are called? | Unknown, dynamic (0 to N, can change at runtime via `addObserver()`) | Fixed, known upfront (a specific, hardcoded set of subsystems) |
| Same interface for all callees? | Yes — all observers implement one shared interface | No — each subsystem has its own distinct, unrelated interface |
| Same method called on each? | Yes — always the same interface method (e.g. `onPriceChange()`) | No — different method per subsystem (`turnOn()`, `play()`, `setVolume()`) |
| Can callees be added at runtime? | Yes | No — fixed at compile time (unless redesigned) |
| Core intent | **Decouple** the subject from not knowing who's listening (broadcast to unknown listeners) | **Simplify** access to a fixed, known set of complex subsystems for the caller |

**One-line gut check:** Observer = broadcast one event to an unknown, dynamic list of identical
listeners. Facade = orchestrate a fixed, known set of different subsystems behind one simple call.

### Facade vs. Adapter — the distinction that matters
- **Adapter** solves a **shape mismatch** — one incompatible interface, translated into another.
  Always involves exactly one wrapped object whose interface doesn't fit.
- **Facade** solves **coordination complexity** — multiple, often unrelated subsystems that need
  to be used together in the right sequence. Doesn't translate anything; it **simplifies** access.

### Interview weightage: ⭐⭐⭐⭐ (High)
Common in system-design-adjacent LLD questions — "design a home theater system," "design an order
checkout flow" (payment + inventory + shipping + notification, all orchestrated by one
`CheckoutFacade`), "design a compiler" (classic GoF example: `Compiler` facade hides
lexer/parser/code-generator subsystems). Also a good pattern to mention when discussing simplifying
a messy microservice orchestration layer, directly relevant to your Wells Fargo distribution
finance platform work.

---

## Quick recall table (for fast revision)

| Pattern | Real-world analogy | Solves | Key mechanic | Weightage |
|---|---|---|---|---|
| **Adapter** | Travel plug adapter | Interface **shape mismatch** | Wraps ONE incompatible object, translates calls | ⭐⭐⭐⭐ |
| **Decorator** | Wrapping a gift in layers | Adding behavior **without subclass explosion** | Wraps object(s) of the SAME interface, stacks layers | ⭐⭐⭐⭐ |
| **Facade** | A building's front wall hiding internal wiring | **Coordination complexity** across multiple subsystems | Hides a FIXED, KNOWN set of different subsystems behind one simple call | ⭐⭐⭐⭐ |

## "Which Structural pattern fits?" — fast decision test for interviews
Ask, in this order:
1. Is there **one** object whose interface doesn't match what you need? → **Adapter**
2. Do you need to **add optional, stackable behavior** to an object, without a subclass per
   combination? → **Decorator**
3. Do you have **multiple different subsystems** that need to be orchestrated together in a
   specific sequence, and you want to hide that complexity behind one simple call? → **Facade**

## How Structural patterns differ from Behavioral patterns (the category-level distinction)
- **Behavioral** patterns (Strategy, Observer, State, Command) are about **runtime decision-making
  and reacting to events** — the held reference is often swapped (State) or broadcast to
  (Observer), and the whole point is dynamic behavior change based on choice or events.
- **Structural** patterns (Adapter, Decorator, Facade) are about **the static shape you build by
  composing objects** — the held reference is usually fixed at construction time, forming a
  permanent structural relationship (an adapter always wraps the same adaptee; a decorator chain,
  once built, doesn't restructure itself; a facade's subsystems are fixed fields).
