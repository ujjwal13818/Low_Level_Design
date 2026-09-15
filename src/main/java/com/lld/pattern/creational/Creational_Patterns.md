# Creational Design Patterns

## Why are they called "Creational"?

Recall the three pattern categories:

| Category | What varies / what it solves |
|---|---|
| **Creational** | *How objects are created* |
| **Structural** | *How objects/classes are composed together to form a larger structure* |
| **Behavioral** | *How objects communicate, interact, and change behavior at runtime* |

**Creational patterns** are named for exactly that: they govern **the process of object creation**
itself — deciding *which* class to instantiate, *how* to assemble a complex object piece by piece,
or *how many* instances of a class are allowed to exist. They don't care about an object's
behavior once it exists (Behavioral) or how it's structurally wired to other objects (Structural) —
they care only about the **moment an object comes into being**, and making that moment safe,
flexible, and free of duplicated/scattered `new SomeClass(...)` logic across the codebase.

**Why this matters as its own category:** scattering object-creation logic (`if/else` chains
deciding which class to `new`, giant multi-parameter constructors, uncontrolled instantiation of
things that should only exist once) causes the exact same kind of OCP/DIP violations you've already
seen elsewhere — just specifically at the point of **construction**, not at the point of behavior.

---

## 1. Factory Pattern

### Why called "Factory"
Like a physical factory — you don't care about the internal machinery that builds the product, you
just ask the factory for "a car" or "a chair" and it hands you a finished, ready-to-use object. In
code: you ask `ExerciseFactory.createExercise("strength")` and get back a ready `Exercise` object —
you never see or write the `new StrengthExercise()` yourself.

### What problem it solves
Object-creation logic — deciding **which concrete class** to instantiate, often based on some input
(a string, an enum, a config value) — needs a single, centralized home. Without it, that decision
gets duplicated as `if/else`/`switch` blocks scattered everywhere objects get created.

### If NOT used — problems
- The same "which class do I create" `if/else` gets copy-pasted in every place that needs to create
  one of these objects.
- Adding a new type (e.g., a new `PUSH` notification type) means finding and editing **every**
  duplicate of that branching logic — a direct **OCP** violation, specifically about construction.
- Client code ends up knowing about every concrete class directly (`new EmailNotification()`,
  `new SmsNotification()`) — a **DIP** violation, since client code should depend on the abstraction
  (`Notification`), not concrete implementations.

### Benefits
- The "which class to build" decision lives in exactly **one place** — the factory.
- Client code only ever talks to the abstraction (`Exercise`, `Notification`) and the factory's
  single creation method — it never writes `new ConcreteClass()` directly.
- Adding a new type means adding one `case` to the factory and one new class — nothing else in the
  codebase changes.

### Real example (from your own code)
```java
public interface Exercise {
    void describe();
}
public class StrengthExercise implements Exercise {
    public void describe() { System.out.println("This is a strength exercise."); }
}
public class CardioExercise implements Exercise {
    public void describe() { System.out.println("This is a cardio exercise."); }
}
public class FlexibilityExercise implements Exercise {
    public void describe() { System.out.println("This is a flexibility exercise."); }
}

public class ExerciseFactory {
    public static Exercise createExercise(String name) {
        switch (name) {
            case "strength": return new StrengthExercise();
            case "cardio": return new CardioExercise();
            case "flexibility": return new FlexibilityExercise();
            default: throw new IllegalArgumentException("Unknown exercise type: " + name);
        }
    }
}

// Client code never calls `new` directly on a concrete Exercise type
ExerciseFactory.createExercise("strength").describe();
```

### Factory vs. Strategy — the distinction that matters (asked and answered in this conversation)
Both look like "interface + branching logic" on the surface, but the lifecycle is completely
different:

| | Strategy | Factory |
|---|---|---|
| What's being decided? | Which **behavior** to run, repeatedly | Which **object** to construct, once |
| Is the chosen thing held and reused? | Yes — stored as a field, invoked many times over the object's life | No — created once; the factory's job ends the instant it returns the object |
| When does the decision happen? | Usually once, but the resulting object is *used* many times after | Only at the single moment of creation |
| What does the chosen thing do afterward? | Actively does work every time it's called | Nothing further — it's just a normal object now; factory has zero further involvement |

**One-line gut check:** Strategy answers *"how should this behave?"* (a standing decision, exercised
repeatedly). Factory answers *"what should I build?"* (a one-shot decision that produces an object
and is immediately done).

### Interview weightage: ⭐⭐⭐⭐⭐ (Very High)
One of the most fundamental, most-expected patterns — comes up constantly, both as a direct
question ("design a shape factory," "design a notification system with multiple channels") and as
a natural sub-component of larger LLD problems (e.g., a `VehicleFactory` inside "design a parking
lot").

---

## 2. Builder Pattern

### Why called "Builder"
Exactly what it sounds like — a separate object whose entire job is to **build up** a complex
object step by step, one piece at a time, before finally producing the finished result. Like
constructing a building floor by floor rather than trying to conjure the whole structure into
existence in one instant.

### What problem it solves
You need to construct an object with **many fields, several of them optional**, and:
- A single constructor taking every field is unreadable and error-prone (especially with several
  parameters of the same type — easy to swap two `boolean`s or `String`s by accident).
- Multiple overloaded constructors for every combination of optional fields explodes
  combinatorially (this is called "telescoping constructors").

### The core mechanic, explained step by step

**Step 1 — the real class has a private constructor that takes a `Builder`, not individual values:**
```java
public class Pizza {
    private String size;
    private boolean cheese;

    private Pizza(Builder builder) {
        this.size = builder.size;
        this.cheese = builder.cheese;
    }
}
```
The constructor just copies fields **out of** a `Builder` object into itself. `private` means
**no code outside `Pizza` can call `new Pizza(...)` at all** — the only legitimate way to get a
`Pizza` is through its `Builder`.

**Step 2 — the `Builder` is a separate "bucket" holding the same fields temporarily:**
```java
public static class Builder {
    private String size;
    private boolean cheese;

    public Builder setSize(String size) { this.size = size; return this; }
    public Builder setCheese(boolean cheese) { this.cheese = cheese; return this; }

    public Pizza build() { return new Pizza(this); }
}
```

**Step 3 — `return this;` is what enables method chaining.** Each setter returns the *same* Builder
instance it was called on, so you can write `.setSize(...).setCheese(...)` as one continuous
expression instead of separate statements:
```java
Pizza pizza = new Pizza.Builder()
        .setSize("Large")
        .setCheese(true)
        .build();
```
Trace: `.setSize("Large")` sets the field and returns the same builder object → `.setCheese(true)`
runs on that same returned object, sets cheese, returns itself again → `.build()` finally
constructs the real `Pizza` from all the values collected so far.

### Why `Builder` is `static`
A **non-static** nested class in Java requires an *existing instance of the outer class* before you
can create it — which would create an impossible chicken-and-egg problem here: you'd need a
`Pizza` already in hand just to get a `Builder` whose entire purpose is to create a `Pizza` in the
first place. Marking `Builder` `static` means it does **not** need any `Pizza` instance to exist
first — it's independent, just organizationally nested inside `Pizza` for readability. This is what
makes `new Pizza.Builder()` (no `Pizza` object involved at all) valid, instead of the broken
`somePizza.new Builder()`.

### Why not just use public getters/setters directly on `Pizza` instead of a whole separate Builder?
This was explored in detail in this conversation — the short version, with the long reasoning below:

1. **Prevents incomplete/half-built objects from leaking out and being used.** With public setters,
   nothing stops code like:
   ```java
   Pizza pizza = new Pizza();
   pizza.setSize("Large");
   pizza.printOrder();     // BUG: used before setCheese() was ever called — silently wrong, not a crash
   pizza.setCheese(true);  // too late
   ```
   With Builder, `Pizza` has no public no-arg constructor and no public setters — there is **no
   possible way** to get a half-built `Pizza`. Either `.build()` succeeds and gives you a fully
   formed object, or it doesn't run at all.

2. **Enforces required fields at one single checkpoint.** Plain setters can't force a field to be
   set. With Builder, `build()` can validate:
   ```java
   public Pizza build() {
       if (size == null) throw new IllegalStateException("Size must be set");
       return new Pizza(this);
   }
   ```
   Now forgetting a required field fails loudly and immediately, at the exact point of the mistake
   — not as a mysterious silent `null` bug discovered much later, somewhere unrelated.

3. **Readability at the call site.** `new Pizza("Large", true, false, true, false)` tells you
   nothing about what each value means. `.setSize("Large").setCheese(true).setExtraSauce(true)` is
   self-documenting — you can read exactly what's being configured without checking the
   constructor's parameter order.

4. **Immutability.** With setters, `pizza.setSize("Small")` could be called by *any* code, *any*
   time, even after the object's been handed off elsewhere (stored in a list, cached, sent to
   another service) — a silent mutation bug waiting to happen. With Builder + `final` fields in
   `Pizza`, once built, the object can never be changed again — guaranteed by the compiler, and
   automatically safer in multi-threaded code (same idea as the `final`/immutability discussion
   from the Decorator pattern notes).

None of these feel like a big deal for a tiny 2-3-field class — that's a fair observation. The
pattern earns its value on real objects with many fields, several required and several optional
(DTOs, request/config objects) — which is extremely common in real backend systems, and often
auto-generated in Java codebases via Lombok's `@Builder` annotation instead of being hand-written.

### Real example (from your own code)
```java
public class Pizza {
    private String size;
    private boolean cheese;
    private boolean pepperoni;
    private boolean extraSauce;

    private Pizza(Builder builder) {
        this.size = builder.size;
        this.cheese = builder.cheese;
        this.pepperoni = builder.pepperoni;
        this.extraSauce = builder.extraSauce;
    }

    public static class Builder {
        private String size;
        private boolean cheese;
        private boolean pepperoni;
        private boolean extraSauce;

        public Builder setSize(String size) { this.size = size; return this; }
        public Builder setCheese(boolean cheese) { this.cheese = cheese; return this; }
        public Builder setPepperoni(boolean pepperoni) { this.pepperoni = pepperoni; return this; }
        public Builder setExtraSauce(boolean extraSauce) { this.extraSauce = extraSauce; return this; }

        public Pizza build() {
            if (size == null) throw new IllegalStateException("Size must be set");
            return new Pizza(this);
        }
    }

    public void printPizza() {
        System.out.println(size + " " + cheese + " " + pepperoni + " " + extraSauce);
    }
}

// Client code
Pizza pizza1 = new Pizza.Builder()
        .setSize("Large").setCheese(true).setPepperoni(true).setExtraSauce(false)
        .build();

// Confirms required-field validation:
Pizza badPizza = new Pizza.Builder().setCheese(true).build();  // throws IllegalStateException
```

### Builder vs. Factory — the distinction that matters
- **Factory** answers *"which subclass should I instantiate?"* — one decision, one method call,
  done. Used when you're choosing **between different types**.
- **Builder** answers *"how do I assemble **one** complex object, piece by piece, with many
  optional parts?"* — not about choosing between types, about cleanly constructing a single type
  that has too many optional fields for a normal constructor.

### Interview weightage: ⭐⭐⭐⭐ (High)
Very commonly used for request/config objects, DTOs, and any LLD problem involving objects with
many optional attributes — "design a `HttpRequest` builder," "design a meal/order customization
system." Recognize it instantly in real code too: Java's own `StringBuilder`, and Lombok's
`@Builder` in your day-to-day Spring codebase, are both this exact pattern.

---

## 3. Singleton Pattern

### Why called "Singleton"
Directly from the math/set-theory term "singleton" — a set containing **exactly one element**. The
pattern guarantees a class has exactly one instance in the entire application, ever.

### What problem it solves
Some things genuinely should only ever have **one shared instance** across the whole application —
a config manager, a connection pool, a logger, a cache. Without control, nothing stops multiple
independent instances from being created, each with its own separate state — wasteful at best
(re-loading config repeatedly) and a source of subtle bugs at worst (different parts of the app
seeing different, inconsistent state).

### The core mechanic
```java
public class Logger {
    private static Logger logger;              // one shared field, holds THE instance

    private Logger() {                          // private — no external `new Logger()` possible
        System.out.println("Loading logger");
    }

    public static Logger getLogger() {           // the ONLY way to get an instance
        if (logger == null) {
            logger = new Logger();               // created once, the first time it's needed
        }
        return logger;                           // every later call returns the SAME object
    }

    public void log(String message) {
        System.out.println(message);
    }
}
```

Client code:
```java
Logger l1 = Logger.getLogger();
Logger l2 = Logger.getLogger();
// l1 == l2 is TRUE — same exact object in memory
// "Loading logger" prints only ONCE, no matter how many times getLogger() is called
```

**Key mechanics, piece by piece:**
- `private Logger()` — constructor is private, so `new Logger()` cannot be called from outside the
  class at all. This is the same "lock the constructor" idea used in Builder, just for a different
  reason (there, it was to force construction through a Builder; here, it's to force construction
  through `getLogger()` so there's only ever one).
- `private static Logger logger` — `static` here means this field belongs to the **class itself**,
  not to any individual object — there's exactly one `logger` field shared everywhere, which is
  exactly what you want since there's deliberately only ever one instance anyway.
- `getLogger()` is `public static` — callable without needing an instance already (`Logger.getLogger()`,
  not `someLogger.getLogger()`), and it's the sole gatekeeper: creates the instance lazily on first
  call, hands back the same one on every call after.

### If NOT used — problems
- Multiple independent instances of something meant to be shared get created — e.g., multiple
  separate `ConfigManager`s, each re-reading config from disk, potentially seeing different values
  if the file changes between reads, wasting I/O and memory.
- Inconsistent state across the app — one part of the code might mutate one instance's data while
  another part reads a completely different instance, causing bugs that are hard to trace back to
  "oh, these were never the same object."

### Benefits
- Guarantees exactly one instance — predictable, consistent shared state across the entire app.
- One global, well-known access point (`getLogger()`, `getInstance()`), which is easy to trace
  everywhere it's used.
- Avoids wasteful repeated initialization of expensive resources (DB connections, loaded config
  files, thread pools).

### Important real-world caveat — thread safety
The simple lazy-initialization version shown above:
```java
public static Logger getLogger() {
    if (logger == null) {
        logger = new Logger();
    }
    return logger;
}
```
has a genuine bug in **multi-threaded environments** (which describes almost any real backend,
including Spring apps): if two threads call `getLogger()` at the *exact same time* when `logger` is
still `null`, both could see `logger == null` simultaneously, and **both** could end up creating
separate `Logger` instances — silently violating the entire point of Singleton. This needs to be
revisited with either `synchronized`, double-checked locking, or (most simply, in real Spring code)
just letting Spring manage it as a `@Service`/`@Component` bean, which is a singleton by default
without you needing to write any of this manual logic yourself.

*(This caveat is a good thing to explore hands-on next, given its direct relevance to real backend
work — thread-safe Singleton variants are a very natural interview follow-up question after the
basic version.)*

### Singleton vs. Factory/Builder — the distinction that matters
- **Factory** controls *which class* gets instantiated.
- **Builder** controls *how a single complex object* gets assembled.
- **Singleton** controls *how many instances* of a class are allowed to exist — exactly one, ever,
  no matter how many times creation is requested.

### Interview weightage: ⭐⭐⭐⭐⭐ (Very High)
Extremely commonly asked, partly because the *naive* version (shown above) has real, well-known
follow-up bugs (thread safety) that interviewers love probing — expect "is this implementation
thread-safe?" as an almost-guaranteed follow-up question. Also directly relevant to your actual job:
Spring's default bean scope **is** Singleton, so understanding this pattern deeply explains a piece
of Spring behavior you rely on every day without necessarily having connected it explicitly before.

---

## Quick recall table (for fast revision)

| Pattern | Real-world analogy | Solves | Key mechanic | Weightage |
|---|---|---|---|---|
| **Factory** | A factory that hands you a finished product | Choosing **which class** to instantiate | Centralizes `if/else`/`switch` creation logic in one method | ⭐⭐⭐⭐⭐ |
| **Builder** | Constructing a building floor by floor | Assembling **one complex object** with many optional fields | Separate chainable object collects values, produces the real object only at `.build()` | ⭐⭐⭐⭐ |
| **Singleton** | "Singleton" = a set with exactly one element | Ensuring **only one instance** of a class ever exists | Private constructor + private static field + public static getter | ⭐⭐⭐⭐⭐ |

## "Which Creational pattern fits?" — fast decision test for interviews
Ask, in this order:
1. Should this class have **exactly one instance**, globally, ever? → **Singleton**
2. Do I need to **choose between several different classes** to instantiate, based on some input? → **Factory**
3. Am I constructing **one** object that has **many fields, several optional**, too many for a clean constructor? → **Builder**

## How Creational patterns differ from Structural and Behavioral (category-level distinction)
- **Creational** — concerned only with the **moment of creation**: which class, how assembled, how
  many instances. Once the object exists, these patterns are done, nothing further to do with them.
- **Structural** — concerned with **how existing objects are composed/wired together** to form a
  larger structure (wrapping, translating interfaces, simplifying access to subsystems).
- **Behavioral** — concerned with **runtime interaction and changing behavior** after objects
  already exist (swapping algorithms, reacting to events, transitioning between states, treating
  actions as objects).

A useful gut-check across all three categories: ask "is the problem about **making** an object
(Creational), **shaping/wiring** objects together (Structural), or **what happens at runtime**
between objects that already exist (Behavioral)?"
