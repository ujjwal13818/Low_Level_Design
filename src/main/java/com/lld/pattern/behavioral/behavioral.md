# Behavioral Design Patterns

## Why are they called "Behavioral"?

Design patterns are grouped into three categories based on **what problem they solve**:

| Category | What varies / what it solves |
|---|---|
| **Creational** | *How objects are created* (Factory, Builder, Singleton) |
| **Structural** | *How objects/classes are composed together* (Adapter, Decorator, Facade) |
| **Behavioral** | *How objects communicate, interact, and change behavior at runtime* |

**Behavioral patterns** are named for exactly that: they govern **behavior** — the runtime interaction,
communication, and responsibility-sharing between objects. They don't care how an object was built
(that's Creational) or how it's structurally wired together (that's Structural). They care about:
- What an object *does* when something happens
- How responsibility for a decision/action is delegated
- How multiple objects stay in sync or react to change

**Common thread across all four patterns below:** they all rely on **polymorphism through an
interface** — a "client" class holds a reference to an interface type, and calls a method on it
without knowing (or caring) which concrete implementation is actually running. This is the same
mechanic underlying DIP (Dependency Inversion) — behavioral patterns are essentially **DIP + OCP
applied to specific, recurring runtime-behavior problems.**

---

## 1. Strategy Pattern

### What varies
The **algorithm/behavior** used to accomplish a task — chosen once (usually by the client),
and doesn't change on its own mid-lifecycle.

### Why it's used
You have multiple interchangeable ways of doing the same conceptual thing (e.g., payment methods,
discount calculations, progressive overload algorithms), and you don't want a big `if/else` or
`switch` choosing between them scattered across the codebase.

### How it's used
1. Define an interface for the varying behavior (e.g., `PaymentStrategy` with `pay(double amount)`).
2. Each variant implements that interface as its own class (`CreditCard`, `Bitcoin`).
3. The client class (`PaymentProcessor`) takes the strategy via constructor injection and delegates
   to it — never contains the `if/else` itself.

### If NOT used — problems
- Giant `if/else`/`switch` blocks in one method deciding behavior → violates **OCP** (adding a new
  variant means editing existing, tested code).
- Hard to unit test each variant in isolation (they're all tangled in one method).
- Business logic class becomes bloated and knows too much about every variant's implementation
  detail — violates **SRP** and **DIP**.

### Benefits
- New variant = new class, zero edits to existing code (OCP-compliant).
- Each strategy testable in isolation.
- Swappable at runtime — same client, different behavior, just by injecting a different object.

### Real example (from your own code)
```java
public interface PaymentStrategy {
    void pay(double amount);
}
public class CreditCard implements PaymentStrategy { ... }
public class Bitcoin implements PaymentStrategy { ... }

public class PaymentProcessor {
    private final PaymentStrategy paymentStrategy;
    public PaymentProcessor(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    public void pay(double amount) { paymentStrategy.pay(amount); }
}
```

### Interview weightage: ⭐⭐⭐⭐⭐ (Very High)
Almost always the first pattern interviewers expect you to reach for — extremely common in real
systems (payment gateways, sorting/pricing rules, discount engines) and frequently the "obvious"
right answer in LLD interview questions like *"design a payment system"* or *"design a discount
engine."* Know this cold.

---

## 2. Observer Pattern

### What varies
**How many objects need to react** when one object's state changes — without that object needing
to know who's listening or how many observers exist.

### Why it's used
One event needs to trigger multiple, independent reactions (e.g., workout completed → AI
recommendation engine reacts, analytics logger reacts, notification service reacts) — and the
"subject" (the thing that changed) shouldn't be hardwired to know about every possible reactor.

### How it's used
1. Define an observer interface (e.g., `PriceObserver` with `onPriceChange(double newPrice)`).
2. Each reactor implements it (`MobileAppDisplay`, `EmailNotification`).
3. The subject (`Stock`) keeps a `List<PriceObserver>`, exposes `addObserver()`, and loops through
   the list calling the interface method whenever its own state changes (`setPrice()`).

### If NOT used — problems
- Subject directly creates and calls every interested party → tight coupling, violates **DIP**
  (subject now depends on concrete classes, not abstractions) and **OCP** (adding a new reactor
  means editing the subject's code).
- Subject becomes a "god object" that knows about unrelated concerns (AI logic, analytics logic,
  notification logic) it has no business knowing about.

### Benefits
- Subject stays completely decoupled from its reactors — it only knows the interface.
- New reactor = new class + one line to register it. Subject is never touched (OCP-compliant).
- This is the **in-process version of what Kafka/pub-sub does at the distributed-systems level** —
  same core idea (publish an event, let subscribers react), different scale.

### Real example (from your own code)
```java
public interface PriceObserver {
    void onPriceChange(Double newPrice);
}
public class MobileAppDisplay implements PriceObserver { ... }
public class EmailNotification implements PriceObserver {
    // only fires past a threshold — proves observers can have independent, differing logic
    public void onPriceChange(Double newPrice) {
        if (newPrice > 100.0) System.out.println("Email Notification: price updated to " + newPrice);
    }
}
public class Stock {
    List<PriceObserver> observers = new ArrayList<>();
    public void addObserver(PriceObserver observer) { observers.add(observer); }
    public void setPrice(Double price) {
        for (PriceObserver observer : observers) observer.onPriceChange(price);
    }
}
```

### Interview weightage: ⭐⭐⭐⭐⭐ (Very High)
Extremely common — comes up in "design a stock ticker," "design a notification system," "design a
pub-sub system," or any question involving "multiple things need to know when X happens." Also the
conceptual foundation for understanding event-driven systems (Kafka, WebSockets), which is directly
relevant to your Wells Fargo/NextSet Kafka work.

---

## 3. State Pattern

### What varies
An object's **behavior changes based on its own internal state**, and the object **transitions
between states over time**, often automatically as a side effect of its own method calls.

### Structurally similar to Strategy — but different intent
| | Strategy | State |
|---|---|---|
| Who picks the implementation? | Client explicitly chooses it | The object switches itself automatically |
| Does it change over the object's lifetime? | Usually no, picked once | Yes — that's the whole point |
| Example | Client picks `CreditCard` once for a payment | `WorkoutSession` moves itself from `InProgress` → `Paused` → `Completed` |

### Why it's used
An object has a lifecycle with valid/invalid transitions (e.g., a `WorkoutSession` can't be paused
before it starts, can't be completed twice), and you don't want that logic as scattered
if/else-with-string-comparisons across every method.

### How it's used
1. Define a state interface (e.g., `SessionState` with `start()`, `pause()`, `complete()`, each
   taking the context object as a parameter).
2. Each state implements it, deciding what's valid *from that state* and calling
   `session.setState(new NextState())` to transition.
3. The context class (`WorkoutSession`) holds a `currentState` field and **delegates every call**
   to it — it contains no decision logic of its own, just one-line forwards:
   ```java
   public void start() { currentState.start(this); }
   ```

### If NOT used — problems
- Every method needs manual guard checks against a string/enum field:
  ```java
  if (state.equals("IN_PROGRESS")) { ... } else { print error }
  ```
  This duplicates validity checks across every method, and adding a new state means editing
  **every** method — a severe **OCP** violation.
- Easy to introduce inconsistent/buggy transitions because the rules aren't localized — they're
  smeared across the whole class.

### Benefits
- Each state's valid transitions live in exactly one place (that state's class) — easy to reason
  about, easy to extend.
- Adding a new state (e.g., `CancelledState`) = one new class, no existing state class touched.
- The context class becomes a thin dispatcher — simple, hard to get wrong.

### Real example (from your own code)
```java
public interface SessionState {
    void start(WorkoutSession session);
    void pause(WorkoutSession session);
    void complete(WorkoutSession session);
}
public class NotStartedState implements SessionState {
    public void start(WorkoutSession session) {
        System.out.println("Workout started");
        session.setState(new InProgressState());
    }
    // pause/complete → invalid, print guard message
}
public class InProgressState implements SessionState { /* pause valid, complete valid */ }
public class PausedState implements SessionState { /* start = resume, complete valid */ }
public class CompletedState implements SessionState { /* everything invalid — terminal state */ }

public class WorkoutSession {
    private SessionState currentState = new NotStartedState();
    public void setState(SessionState state) { this.currentState = state; }
    public void start() { currentState.start(this); }
    public void pause() { currentState.pause(this); }
    public void complete() { currentState.complete(this); }
}
```

**Debugging lesson learned:** when tracing State pattern output, always track **which state
`currentState` currently points to at each line** — a call like `session.pause()` runs whichever
state's `pause()` is active *right now*, not necessarily the one you expect if a prior call already
transitioned it. Output that looks "wrong" is often just a misunderstanding of which state is
currently active, not a bug.

### Interview weightage: ⭐⭐⭐⭐ (High)
Very common in LLD problems with clear lifecycles — "design a vending machine," "design a traffic
light," "design an elevator," "design an order/booking system" (Pending → Confirmed → Shipped →
Delivered → Cancelled). Slightly less universally applicable than Strategy/Observer, but a near-
guaranteed pattern for any problem involving a state machine.

---

## 4. Command Pattern

### What varies
**A request/action itself becomes an object**, instead of an immediate inline method call — so it
can be queued, logged, passed around as data, executed later, or **undone**.

### Why it's used
You can't "undo a method call" directly in a language like Java. But if the request is wrapped as
an object that knows how to both `execute()` and `undo()` itself, undo/redo, action queues, and
action history all become possible.

### How it's used
1. Define a `Command` interface with `execute()` and `undo()`.
2. Each concrete command wraps a specific action plus the data needed to reverse it
   (`LogSetCommand`, `AppendCommand`).
3. An `Invoker` class holds a history (typically a stack/`Deque`) of executed commands — it calls
   `execute()` and pushes to history; `undoLast()` pops and calls `undo()`.
4. The invoker never knows *what* a command actually does — same polymorphism principle as every
   pattern above.

### If NOT used — problems
- No way to implement undo/redo without hacky, ad-hoc "remember the last thing that happened" state
  variables that don't scale past one undo step.
- No way to queue actions for later execution (e.g., offline actions to sync later) — the action is
  "used up" the instant it's called, it can't be stored as data.
- No natural way to log a history of "what actions were taken" as first-class objects (useful for
  audit trails, debugging, replay).

### Benefits
- Undo/redo becomes trivial — just pop the history stack and call `undo()`.
- Actions can be queued, logged, serialized, or replayed since they're real objects, not just
  fleeting method calls.
- Decouples "who requests an action" from "who performs it" and "when it's performed."

### Real example (from your own code — text editor)
```java
public interface Command {
    void execute();
    void undo();
}
public class AppendCommand implements Command {
    private final TextEditor editor;
    private final String text;
    public AppendCommand(TextEditor editor, String text) { this.editor = editor; this.text = text; }
    public void execute() { editor.appendText(text); }
    public void undo() { editor.deleteLastChars(text.length()); }
}
public class CommandInvoker {
    private final Deque<Command> history = new ArrayDeque<>();
    public void executeCommand(Command command) { command.execute(); history.push(command); }
    public void undoLast() { if (!history.isEmpty()) history.pop().undo(); }
}
```

### Interview weightage: ⭐⭐⭐ (Medium)
Less universally asked than Strategy/Observer/State, but comes up specifically for "design a text
editor with undo/redo," "design a remote control," "design a task queue/job scheduler." Worth
knowing solidly, but if you're short on prep time, prioritize the other three first — this one is
scenario-specific rather than a default go-to.

---

## Quick recall table (for fast revision)

| Pattern | What varies | Client picks it? | Classic interview Q | Weightage |
|---|---|---|---|---|
| **Strategy** | Algorithm/behavior | Yes, explicitly | Payment system, discount engine | ⭐⭐⭐⭐⭐ |
| **Observer** | Who reacts to a change | No — auto-notified | Stock ticker, notification/pub-sub system | ⭐⭐⭐⭐⭐ |
| **State** | Behavior by internal state | No — object switches itself | Vending machine, traffic light, order lifecycle | ⭐⭐⭐⭐ |
| **Command** | Action-as-object (undo/queue) | N/A — action is wrapped | Text editor undo/redo, remote control, job queue | ⭐⭐⭐ |

## "What varies" — the fastest way to pick the right pattern in an interview
Ask yourself, in this exact order:
1. Does an **action need to be undoable, queued, or logged as data**? → **Command**
2. Does an object's behavior depend on **its own internal lifecycle/state**, and does it transition
   automatically? → **State**
3. Do **multiple, independent things need to react** when something changes, without tight coupling? → **Observer**
4. Is there just **one swappable algorithm/behavior**, chosen once by the client? → **Strategy**

If none of these fit, the answer is probably Structural or Creational, not Behavioral.