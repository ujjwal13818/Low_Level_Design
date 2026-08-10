# Dependency Inversion Principle (DIP)

## Definition
High-level modules (business logic) should not depend on low-level modules (implementation details).
Both should depend on **abstractions** (interfaces).
Abstractions should not depend on details — details should depend on abstractions.

## Why it matters
This principle explains *why* dependency injection (Spring's `@Autowired`) works, and why interfaces
matter even when there's only one implementation today. It's about protecting stable business logic
from volatile, swappable infrastructure.

---

## Bad Example

```java
public class EmailNotificationService {
    public void send(String message) {
        System.out.println("Sending email: " + message);
    }
}

public class OrderService {
    private EmailNotificationService emailService = new EmailNotificationService();

    public void placeOrder() {
        // order logic
        emailService.send("Order placed!");
    }
}
```

**Problem:** `OrderService` (high-level business logic) directly creates and depends on
`EmailNotificationService` (a low-level, concrete detail). To add SMS notifications tomorrow,
you'd have to edit `OrderService` itself — exactly what OCP tells you to avoid.

---

## Good Example (DIP applied)

```java
public interface NotificationService {
    void send(String message);
}

public class EmailNotificationService implements NotificationService {
    public void send(String message) { System.out.println("Email: " + message); }
}

public class OrderService {
    private final NotificationService notificationService;

    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService; // injected, not created
    }

    public void placeOrder() {
        notificationService.send("Order placed!");
    }
}
```

Now `OrderService` depends only on the `NotificationService` interface. Swap in
`SmsNotificationService`, `SlackNotificationService`, whatever — `OrderService` never changes.

---

## Connection to Spring `@Autowired`

```java
@Service
public class OrderService {
    private final NotificationService notificationService;

    @Autowired
    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}

@Service
public class EmailNotificationService implements NotificationService {
    public void send(String message) { ... }
}
```

- `OrderService` asks for the **interface** `NotificationService` in its constructor — it never
  writes `new EmailNotificationService()` itself.
- At startup, Spring scans the codebase, finds a concrete class implementing `NotificationService`,
  creates it, and **injects it automatically** — that's what `@Autowired` mechanically does:
  constructor injection performed by the framework instead of by hand.
- **DIP is the precondition that makes `@Autowired` possible at all.** If `OrderService` had
  hardcoded `new EmailNotificationService()`, there'd be nothing for Spring to inject — the
  dependency wasn't requested via an abstraction.

---

## Q&A

### Q1: What if there are two implementations of the same interface — e.g. `EmailNotificationService` and `SmsNotificationService` — which one does `@Autowired` pick?

Spring finds **two** beans matching `NotificationService` and can't decide — it fails loudly at
startup with `NoUniqueBeanDefinitionException`. It won't guess silently.

**Three ways to resolve it:**

1. **`@Qualifier`** — be explicit about which bean to use:
   ```java
   @Autowired
   public OrderService(@Qualifier("emailNotificationService") NotificationService notificationService) { ... }
   ```
   Default bean name = class name with lowercase first letter, unless overridden via `@Service("customName")`.

2. **`@Primary`** — mark one implementation as the default:
   ```java
   @Service
   @Primary
   public class EmailNotificationService implements NotificationService { ... }
   ```
   Anything without an explicit `@Qualifier` gets this one automatically.

3. **Field/parameter name matching** — Spring can resolve by matching the parameter name to a bean
   name (e.g. parameter named `emailNotificationService`). Fragile, not something to rely on deliberately.

**If you actually want both to run** (e.g. notify via email AND SMS):
```java
@Autowired
private List<NotificationService> notificationServices; // Spring injects ALL matching beans
```
Then loop through and call `.send()` on each.

---

### Q2: What if the autowired type is a concrete class that has subclasses, not an interface?

Same mechanism, same problem. Spring matches any bean **assignable to** the requested type — so if
`NotificationService` is a concrete class and `EmailNotificationService extends NotificationService`
is also a bean, autowiring the base type again produces `NoUniqueBeanDefinitionException` (same fixes:
`@Qualifier`, `@Primary`, or `List<NotificationService>`).

**More important point:** autowiring a *concrete class* (instead of an interface) is itself a mild
DIP smell. It means the "high-level" consumer is depending on a real implementation, not an
abstraction — even if Spring resolves it fine. This causes real problems later:
- Harder to swap implementations without touching the inheritance hierarchy
- Messy interactions with Spring AOP/proxies when subclassing managed beans

**Convention:** autowire interfaces, not concrete classes with subclasses. If you find yourself
wanting to autowire a class that has subclasses, that's usually a sign it should have been an
interface (or abstract class) from the start, with the subclasses as concrete implementations.

---

### Q3: If only one implementation should exist, why not just hardcode/couple them directly instead of using an interface + `@Autowired`? Isn't defining an interface just to avoid an exception pointless if there's only one `@Service` anyway?

This is the key question DIP actually answers — and it's **not** about avoiding the
`NoUniqueBeanDefinitionException`. That exception only matters once you have *multiple*
implementations, which is a separate, later concern. The real reason to use an interface is about
**what happens when requirements change**, not today's bean count.

**Timeline without DIP (hardcoded):**

- **Day 1:** Only `EmailNotificationService` exists.
  ```java
  private EmailNotificationService notificationService = new EmailNotificationService();
  ```
  Works fine today — no ambiguity, no exception, nothing wrong yet.

- **Month 3:** Product asks for SMS notifications too. Now you must:
  1. Open `OrderService` — code that was already working, tested, deployed
  2. Change the field type
  3. Change how it's constructed
  4. Re-test `OrderService` even though its actual business logic (placing orders) never changed

That cost — reopening and re-risking stable business logic — is paid **later**, when it's most
disruptive. Hardcoding isn't wrong because it breaks today; it's wrong because it welds business
logic to a decision that's likely to change.

**Timeline with DIP (interface from day 1, even with one implementation):**

```java
@Autowired
public OrderService(NotificationService notificationService) { ... }
```

- **Month 3:** Add `SmsNotificationService implements NotificationService`, add one `@Qualifier` or
  `@Primary` to resolve which bean to use.
- `OrderService` is **never opened, never edited, never re-tested.**

**Bottom line:** you pay a small upfront cost (define an interface, even for a single
implementation) to avoid a much larger, riskier cost later (editing and re-testing tested business
logic every time infrastructure changes). This is a bet that **infrastructure changes far more
often than business rules do** — usually a safe bet in any real, evolving codebase (e.g. Wells
Fargo's distribution finance platform), even if it feels like overhead on a small project like
NextSet at its current size.

---

## How DIP differs from OCP (they overlap but aren't the same)

- **OCP** = can I *add* new behavior without editing existing, tested code? (extension without modification)
- **DIP** = *who depends on whom*? Should business logic depend on low-level details, or should both depend on an abstraction?

If `WorkoutService` does `new KafkaEventPublisher()` directly inside itself, that's **both**:
- an OCP violation (swapping providers means editing `WorkoutService`), **and**
- a DIP violation (core business logic is tightly bound to a low-level detail — if Kafka's client
  API changes, business logic has to change too)

Introducing an `EventPublisher` interface fixes both at once — which is why they can feel like the
same principle. The distinction: **OCP cares about not re-editing working code. DIP cares about who's
in charge** — high-level policy should define the interface it needs; low-level details must conform
to it, never the reverse.
