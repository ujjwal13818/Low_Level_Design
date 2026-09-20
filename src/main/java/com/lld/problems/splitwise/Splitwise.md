# LLD Problem: Splitwise (Expense Splitting System)

## Requirements (finalized scope)
- Users can form a **group**, add/remove members.
- Log an **expense**: total amount, who **paid**, and the list of **participants** the expense
  should be split among.
- Split type supported: **exact amounts** per participant (explicitly specified, must sum to the
  total). Design should be extensible to **equal** and **percentage** splits too (Strategy pattern).
- Edit or remove an expense — and this must correctly **adjust** the balances that were already
  affected by it.
- **Settlement**: one user pays another to reduce/zero-out what they owe — updates balances.
- Track **net balance** between users (who owes whom, how much).
- Group deletion.
- Notifications — **out of scope**.
- Validation: sum of split amounts must equal the total expense amount, or the operation should be
  rejected.
- Every expense's participants (and the payer) must actually belong to the group the expense is
  logged against.

---

## Step 1: Entity Identification

| Entity | Why it's needed | Key attributes |
|---|---|---|
| `User` | A person in the system | `userId`, `userName`, `age` |
| `Group` | Collection of users sharing expenses | `groupId`, `name`, `admin`, `members`, `expenses` |
| `Expense` | One logged spending event | `expenseId`, `totalAmount`, `paidBy`, `participants`, `splitDetails` |
| `SplitStrategy` | How an expense's amount is divided | interface, swappable implementations |
| `BalanceSheet` | Tracks who owes whom, updated on every expense change | `Map<User, Map<User, Double>>` |
| `ExpenseManager` | Orchestrates: validates, computes split, updates Group + BalanceSheet together | no stored state beyond a `BalanceSheet` reference |

### Why does `BalanceSheet` need to be a separate entity from `Group`?
If `Group` directly stored and updated balances itself, `Group` would be mixing two unrelated
responsibilities: membership management, and financial calculation logic (SRP violation — same
reasoning as pulling `FeeStrategy` out of `ParkingLot`). If balance-calculation rules evolve later
(currency conversion, debt-simplification), you'd be editing `Group` for reasons that have nothing
to do with "who's a member." Keeping `BalanceSheet` separate isolates that complexity.

### Why does `ExpenseManager` exist, separate from `Expense` and `BalanceSheet`?
`Expense` was deliberately kept as a **simple, immutable data object** — "this expense happened,
here's its final details" — not something that computes or validates itself. Something needs to
**orchestrate**: call the right `SplitStrategy`, validate group membership, build the `Expense`
object, and update both `Group.expenses` and `BalanceSheet` **together, atomically**, so they never
drift out of sync. That orchestration role is `ExpenseManager`'s job — same pattern as `ParkingLot`
coordinating across `Floor`s that couldn't coordinate themselves.

---

## Q&A — every point of confusion from this build, explained in detail

### Q1: Why use `Map<User, Double>` for an expense's split, instead of an array/list where position = user?
Traced concretely: if `splitArray = [100, 150, 50]` means "index 0 = whoever is first in the
group's member list," this breaks the moment the group's membership changes — a new member joining,
someone leaving and rejoining, or any reordering of the member list would silently make
`splitArray[2]` refer to a **different person** than originally intended, corrupting historical
expense data with no warning. **A `Map<User, Double>` ties each amount directly and permanently to
a specific `User` object/key — completely independent of any list's current ordering.** This is the
same underlying principle as `Map<SpotType, List<ParkingSpot>>` in Parking Lot: whenever a value
must be tied to a specific entity, and that entity's position isn't guaranteed stable, use a `Map`
keyed by the entity — never rely on array/list index to carry meaning.

### Q2: Why does `BalanceSheet` use `Map<User, Map<User, Double>>` instead of computing balances on the fly from a list of expenses?
A nested map lets you answer "how much does X owe Y" with a **single, instant lookup**
(`balances.get(Y).get(X)`), updated incrementally every time an expense is added or removed.
Computing this from scratch by scanning every expense every time a balance is requested would be
far slower and more complex, especially as the number of expenses grows — same reasoning as
choosing `Map<SpotType, List<ParkingSpot>>` over recomputing "available spots" by scanning
everything each time.

**Balances direction — the actual definition, locked after some back-and-forth:**
`balances.get(X)` = a map of people who **owe X**, and how much. So after Alice pays ₹300 split
equally among Alice, Bob, Carol: `balances.get(Alice) = {Bob: 100, Carol: 100}` (Bob and Carol each
owe Alice 100). This direction is easy to get backwards — worth restating out loud whenever
revisiting this code, and the `getBalance(debtor, creditor)` parameter names were chosen
specifically to make the lookup direction less error-prone to call correctly.

**Why `getBalance` needs to check BOTH directions and net them out, not just one lookup:**
Debts between the same two people can accumulate in **both directions** across different expenses
(e.g., Alice pays for dinner — Bob owes Alice; later Bob pays for a movie — Alice owes Bob). Since
each expense's debt gets recorded under whichever user **paid** that specific expense, the "true"
net balance between two people can end up split across **two different rows** in the nested map.
`getBalance` must read both `balances.get(creditor).get(debtor)` and
`balances.get(debtor).get(creditor)` and subtract them to get the actual net amount — a single
lookup in only one direction gives an incomplete (and potentially very wrong) answer if debts exist
in both directions.

### Q3: Why does `EqualSplitStrategy` need a separate `participants` parameter, if `inputValues` is a `Map<User, Double>` — aren't the map's keys already the participant list?
For `ExactSplitStrategy`/`PercentageSplitStrategy`, yes — `inputValues.keySet()` genuinely IS the
participant list, making a separate `participants` parameter redundant for those two. But
`EqualSplitStrategy` has **no per-user values at all** — nothing to divide, just a total and a list
of who to divide it among. If `inputValues` is `null`/empty for `EqualSplitStrategy` (as designed),
there'd be **no way to derive participants from an empty map**. Since one shared `SplitStrategy`
interface must satisfy the *most demanding* implementation, `participants` stays as an explicit
parameter — a normal trade-off with Strategy pattern: the interface must be a superset of what every
implementation needs, even if some implementations end up not needing every parameter.

**Alternative considered and rejected:** using placeholder values (e.g., all `0.0`) in `inputValues`
so `EqualSplitStrategy` could derive participants from `keySet()` too, eliminating the separate
parameter. This technically works, but risks confusing future readers ("does `0.0` mean something?
Is this person's share actually zero?") — the explicit, separate `participants` parameter with a
`null` `inputValues` for Equal is clearer about intent, even though it's a few extra plain fields.

### Q4: How do you make a method parameter "optional" in Java?
Java has **no built-in default/optional parameter syntax** (unlike Python/Kotlin). Real options:
1. **Allow `null`** — simplest, but relies on documentation/convention; risks `NullPointerException`
   if a strategy that shouldn't need the value accidentally reads it.
2. **Method overloading** — multiple signatures, same method name. Rejected here because it
   reintroduces per-type branching logic in the *caller*, defeating Strategy pattern's purpose.
3. **`Optional<T>`** — wraps the parameter's type, forcing callers/implementers to consciously
   handle "this might be absent" via `.isPresent()`/`.orElseThrow()`, rather than silently allowing
   `null`. **This is the option used in the final design** —
   `Optional<Map<User, Double>> inputValues` — since each `SplitStrategy` implementation already
   knows in advance whether it needs input values, `Optional` makes the "might be absent" case
   explicit and safer than a bare nullable parameter.

### Q5: Why must floating-point sums be compared with a tolerance (`EPSILON`), not `==`?
`double` values can't represent all decimal numbers exactly in binary — e.g., `0.1 + 0.2` doesn't
equal exactly `0.3` in floating-point math (it's actually `0.30000000000000004`). Directly comparing
two `double`s with `==`/`!=` is unreliable for this reason. **Fix:** check if the absolute difference
between the two values is smaller than a small tolerance (`EPSILON`, e.g., `0.01`) — if so, treat
them as equal for practical purposes: `Math.abs(sum - totalAmount) > EPSILON`. This same tolerance
check is used in `ExactSplitStrategy` (validating amounts sum to total), `PercentageSplitStrategy`
(validating percentages sum to 100), and `BalanceSheet.removeExpense` (checking if a balance has
been fully zeroed out after reversal).

### Q6: `PercentageSplitStrategy` — why is "give leftover to the first participant" the WRONG way to handle a percentage mismatch (unlike `EqualSplitStrategy`'s remainder handling)?
These look similar on the surface but are fundamentally different situations:
- **`EqualSplitStrategy`'s remainder** (e.g., ₹100 / 3 = ₹33.33...) is a **rounding artifact of a
  mathematically correct operation** — the calculation itself is always valid, just needs help with
  floating-point precision. Giving the last participant `total - sum of others` is a legitimate fix.
- **Percentages summing to something other than 100** (e.g., 40% + 40% + 30% = 110%) is a symptom of
  **invalid input data**, not a rounding artifact. Silently absorbing the "leftover" into one
  participant's share would **hide a real data-entry error** instead of rejecting it — very
  different from the equal-split case. **Fix: validate percentages sum to 100 (within `EPSILON`)
  FIRST, throwing an exception if not, before doing any per-user calculation at all.**

### Q7: Why does `ExpenseManager.addExpense` validate group membership BEFORE calling `strategy.calculateSplit(...)`?
"Fail fast" — reject invalid input before doing any real work (computing splits, creating objects),
rather than performing the computation and only discovering the problem when trying to record it.
Cheaper to fail early, and avoids partially-completed side effects.

### Q8: Why do `User` (and `Group`) need `equals()`/`hashCode()` overrides, given that the code never explicitly calls `.equals()` or `.hashCode()` anywhere?
This was the biggest point of confusion in this build, worth restating clearly:

**You never call `.equals()`/`.hashCode()` yourself — but every built-in Java collection method that
compares objects calls them internally, automatically, on your behalf.** `List.contains(x)`
internally loops through the list and calls `.equals()` on each element. `HashMap.get(key)`
internally calls `key.hashCode()` to find the right bucket, then `.equals()` to confirm a match
within that bucket. You never see these calls in your own code, but methods like
`members.contains(paidBy)`, `balances.get(user)`, `balances.merge(user, ...)`,
`balances.computeIfAbsent(user, ...)` all trigger this machinery under the hood — same idea as
`@Autowired` invisibly wiring dependencies for you.

**The actual problem this solves:** by default, Java's `Object.equals()` uses **reference
equality** — two objects are only "equal" if they're the literal same object in memory, even if
every field matches. `new User("u1", "Alice", 23).equals(new User("u1", "Alice", 23))` is `false`
by default, even though both clearly represent the same person. This becomes a real bug the moment
an object gets **reconstructed** rather than reused — most commonly, loading data from a database.
Every time a backend loads "user u1" from storage, it typically constructs a **brand-new** `User`
object — a different instance every time, even for the same real person. Without a custom
`equals()`/`hashCode()` based on `userId`, `HashMap`/`List.contains()` would silently treat these as
different entities, splitting one person's data across multiple map entries or failing membership
checks that should have passed.

**Overriding both, based on the unique ID (`userId`, `groupId`):**
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    User user = (User) o;
    return userId.equals(user.userId);
}

@Override
public int hashCode() {
    return Objects.hash(userId);
}
```
**Why both together, always:** Java's contract requires that if two objects are `.equals()`, they
must also share the same `.hashCode()`. `HashMap` uses `hashCode()` to pick a bucket and `equals()`
to confirm a match within it — overriding one without the other breaks this contract and causes
inconsistent, hard-to-debug behavior (two "equal" objects landing in different buckets, so a lookup
never finds a match even though `.equals()` would say true).

**Does auto-generating IDs (e.g., via `UUID`) remove the need for this?** No — this was a separate
question worth untangling. Auto-generation (`UUID.randomUUID()`, not `Random`, for a
practically-guaranteed-unique ID) solves "how do I create a unique ID without manual effort." It has
nothing to do with "how does Java recognize that two separately-created objects represent the same
entity" — that's `equals()`/`hashCode()`'s job regardless of how the ID was produced. If anything,
auto-generation makes the reconstruction scenario (same ID, different object instances) MORE likely
in a real system, not less.

**Does a field changing mid-lifecycle break this?** If `userId` were mutable and changed after the
object was already stored as a `HashMap` key, the object's hash bucket would become stale —
`HashMap` could lose track of it entirely. This is a real, well-known Java gotcha: never mutate a
field that's part of `equals()`/`hashCode()` while the object is live as a map key. This is an
additional, independent reason `userId` is kept `final`.

### Q9: Should `Group.addExpense()`/`removeExpense()` be `public`?
No — if `public`, any code could call them directly, bypassing `ExpenseManager` entirely, and
leaving `BalanceSheet` permanently out of sync with `Group.expenses` (a real correctness bug: the
group's expense list changes, but balances silently don't reflect it). **Fix: make them
package-private** (no access modifier) — only classes in the same package (like `ExpenseManager`)
can call them, while `Group` still owns and exposes the list for reading
(`getExpenses()` stays `public`). This mirrors the earlier `ParkingSpot.occupy()`/`vacate()` design
— mutation happens only through the trusted orchestrating class, never directly by arbitrary callers.

---

## Bugs encountered and fixed during this build

1. **`EqualSplitStrategy`'s remainder handling was correct from the start** — giving the last
   participant `total - sum of others` avoids floating-point drift (`100/3` scenario). Good
   instinct here, no fix needed.
2. **`ExactSplitStrategy` — missing validation entirely (first draft).** Copied `inputValues`
   straight into the result with no check that the values summed to `totalAmount` — silently
   allowed invalid splits through. Fixed by summing all values and comparing against `totalAmount`
   with `EPSILON` tolerance, throwing `IllegalArgumentException` on mismatch.
3. **`PercentageSplitStrategy` — missing `return` statement** (compile error — no path returned a
   value), and used a "leftover to first participant" patch instead of validating percentages sum
   to 100 upfront (see Q6 above for why these are different problems).
4. **`BalanceSheet.addExpense` — direction confusion, resolved through explicit back-and-forth**
   (see Q2) — the outer map key represents "who is owed," so updates must happen on the **payer's**
   row (`balances.get(paidBy).merge(participant, share, ...)`), not the participant's row.
5. **`BalanceSheet` — `NullPointerException` risk from manual `containsKey`/`get`/`put` chains** —
   fixed using `computeIfAbsent(key, k -> new HashMap<>())` (ensure the inner map exists) chained
   with `.merge(key, value, Double::sum)` (safely accumulate a value, whether or not the key
   already existed) — replacing verbose, bug-prone manual null-checking with these two idioms.
6. **`BalanceSheet.getBalance` — only checked one direction initially**, missing that debts between
   the same two people can accumulate on both sides across different expenses. Fixed by checking
   and netting both directions.
7. **`BalanceSheet.removeExpense` — used `computeIfAbsent` where it shouldn't have.** Removing an
   expense should only ever apply to a previously-added expense, so the payer's row should already
   exist; using `computeIfAbsent` here would silently create a fresh (and incorrectly negative)
   entry if something had gone wrong elsewhere, masking a real bug instead of surfacing it.
8. **`User` — multiple issues in the first draft**: package-private (non-`private`) fields, no
   `final`, `age` typed as `String` instead of `int`, no getters, no `equals()`/`hashCode()`. All
   fixed to match the established conventions from Parking Lot (`private final` fields, proper
   getters, correct types).
9. **`Main` — invalid `Map.of({key, value}, ...)` syntax** (not valid Java — `Map.of` takes flat
   alternating key-value arguments, no braces), and **`new ChronoLocalDate()`** — `ChronoLocalDate`
   is an interface, cannot be instantiated with `new`; corrected to `LocalDate.now()`.
10. **`Main` — bypassed `ExpenseManager` entirely**, constructing `Expense` directly with a
    hand-built split map — this skips `SplitStrategy`, membership validation, and `BalanceSheet`
    updates altogether. Corrected to go through `ExpenseManager.addExpense(...)`, the intended
    single entry point.

---

## Final entity list and code

```
User
  - userId, userName, age (private final, with getters)
  - equals()/hashCode() based on userId

Group
  - groupId, groupName (final), admin (mutable), members (final List), expenses (final List)
  - addMember() / removeMember() — public
  - addExpense() / removeExpense() — package-private (only ExpenseManager calls these)
  - equals()/hashCode() based on groupId

Expense
  - expenseId, description, totalAmount, paidBy, participants, splitDetails (Map<User,Double>), date
  - simple, immutable data object — no computation logic inside

SplitStrategy (interface)
  - Map<User, Double> calculateSplit(double totalAmount, List<User> participants, Optional<Map<User, Double>> inputValues)
  Implementations: EqualSplitStrategy, ExactSplitStrategy, PercentageSplitStrategy

BalanceSheet
  - Map<User, Map<User, Double>> balances   (outer key = who is OWED)
  - addExpense(Expense) / removeExpense(Expense) — update balances
  - getBalance(debtor, creditor) — nets both directions

ExpenseManager
  - holds a BalanceSheet reference
  - addExpense(...) — validates membership, calls strategy, builds Expense, updates Group + BalanceSheet together
  - removeExpense(...) — reverses both Group and BalanceSheet together
```

```java
public class User {
    private final String userId;
    private final String userName;
    private final int age;

    public User(String userId, String userName, int age) {
        this.userId = userId;
        this.userName = userName;
        this.age = age;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getAge() { return age; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId.equals(user.userId);
    }

    @Override
    public int hashCode() { return Objects.hash(userId); }
}

public class Group {
    private final String groupId;
    private final String groupName;
    private User admin;
    private final List<User> members;
    private final List<Expense> expenses;

    public Group(String groupId, String groupName, User admin) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.admin = admin;
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
        members.add(admin);
    }

    public void addMember(User user) { members.add(user); }
    public void removeMember(User user) { members.remove(user); }
    void addExpense(Expense expense) { expenses.add(expense); }
    void removeExpense(Expense expense) { expenses.remove(expense); }

    public String getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }
    public List<User> getMembers() { return members; }
    public List<Expense> getExpenses() { return expenses; }
}

public interface SplitStrategy {
    Map<User, Double> calculateSplit(double totalAmount, List<User> participants, Optional<Map<User, Double>> inputValues);
}

public class EqualSplitStrategy implements SplitStrategy {
    public Map<User, Double> calculateSplit(double totalAmount, List<User> participants, Optional<Map<User, Double>> inputValues) {
        int n = participants.size();
        double eachShare = totalAmount / n;
        double lastShare = totalAmount - (eachShare * (n - 1));
        Map<User, Double> result = new HashMap<>();
        for (int i = 0; i < n - 1; i++) result.put(participants.get(i), eachShare);
        result.put(participants.get(n - 1), lastShare);
        return result;
    }
}

public class ExactSplitStrategy implements SplitStrategy {
    private static final double EPSILON = 0.01;
    public Map<User, Double> calculateSplit(double totalAmount, List<User> participants, Optional<Map<User, Double>> inputValues) {
        Map<User, Double> splitDetails = inputValues.orElseThrow(() ->
                new IllegalArgumentException("Exact split requires input values"));
        double sum = 0.0;
        for (double amount : splitDetails.values()) sum += amount;
        if (Math.abs(sum - totalAmount) > EPSILON) {
            throw new IllegalArgumentException("Split amounts (" + sum + ") do not sum to total amount (" + totalAmount + ")");
        }
        return splitDetails;
    }
}

public class PercentageSplitStrategy implements SplitStrategy {
    private static final double EPSILON = 0.01;
    public Map<User, Double> calculateSplit(double totalAmount, List<User> participants, Optional<Map<User, Double>> inputValues) {
        Map<User, Double> percentages = inputValues.orElseThrow(() ->
                new IllegalArgumentException("Percentage split requires input values"));
        double percentSum = 0.0;
        for (double percent : percentages.values()) percentSum += percent;
        if (Math.abs(percentSum - 100.0) > EPSILON) {
            throw new IllegalArgumentException("Percentages must sum to 100, got: " + percentSum);
        }
        Map<User, Double> result = new HashMap<>();
        for (Map.Entry<User, Double> entry : percentages.entrySet()) {
            result.put(entry.getKey(), totalAmount * entry.getValue() / 100.0);
        }
        return result;
    }
}

public class BalanceSheet {
    private static final double EPSILON = 0.01;
    private final Map<User, Map<User, Double>> balances = new HashMap<>();

    public void addExpense(Expense expense) {
        User paidBy = expense.getPaidBy();
        for (Map.Entry<User, Double> entry : expense.getSplitDetails().entrySet()) {
            User participant = entry.getKey();
            double share = entry.getValue();
            if (participant.equals(paidBy)) continue;
            balances.computeIfAbsent(paidBy, k -> new HashMap<>()).merge(participant, share, Double::sum);
        }
    }

    public void removeExpense(Expense expense) {
        User paidBy = expense.getPaidBy();
        for (Map.Entry<User, Double> entry : expense.getSplitDetails().entrySet()) {
            User participant = entry.getKey();
            double share = entry.getValue();
            if (participant.equals(paidBy)) continue;
            balances.get(paidBy).merge(participant, -share, Double::sum);
            if (Math.abs(balances.get(paidBy).get(participant)) < EPSILON) {
                balances.get(paidBy).remove(participant);
            }
        }
    }

    public double getBalance(User debtor, User creditor) {
        double owesToCreditor = balances.getOrDefault(creditor, Map.of()).getOrDefault(debtor, 0.0);
        double owesFromCreditor = balances.getOrDefault(debtor, Map.of()).getOrDefault(creditor, 0.0);
        return owesToCreditor - owesFromCreditor;
    }
}

public class ExpenseManager {
    private final BalanceSheet balanceSheet;

    public ExpenseManager(BalanceSheet balanceSheet) { this.balanceSheet = balanceSheet; }

    public Expense addExpense(Group group, String expenseId, String description, double totalAmount,
                               User paidBy, List<User> participants, SplitStrategy strategy,
                               Optional<Map<User, Double>> inputValues) {
        validateMembership(group, paidBy, participants);
        Map<User, Double> splitDetails = strategy.calculateSplit(totalAmount, participants, inputValues);
        Expense expense = new Expense(expenseId, description, totalAmount, paidBy, participants, splitDetails, LocalDate.now());
        group.addExpense(expense);
        balanceSheet.addExpense(expense);
        return expense;
    }

    public void removeExpense(Group group, Expense expense) {
        group.removeExpense(expense);
        balanceSheet.removeExpense(expense);
    }

    private void validateMembership(Group group, User paidBy, List<User> participants) {
        List<User> members = group.getMembers();
        if (!members.contains(paidBy)) {
            throw new IllegalArgumentException("Payer " + paidBy.getUserName() + " is not a member of this group");
        }
        for (User participant : participants) {
            if (!members.contains(participant)) {
                throw new IllegalArgumentException("Participant " + participant.getUserName() + " is not a member of this group");
            }
        }
    }
}
```

## Patterns identified in this design
- **Strategy** — `SplitStrategy` interface with `EqualSplitStrategy`/`ExactSplitStrategy`/
  `PercentageSplitStrategy` — the clearest, most direct Strategy application of the whole curriculum
  so far, since the swappable "algorithm" (how to split money) is exactly what the pattern is built
  for.
- **No Factory used** — strategies are constructed directly by the caller (`new EqualSplitStrategy()`),
  since the caller always knows in advance which split type they want; a factory would add
  indirection without solving a real problem here.
- **Facade-adjacent** — `ExpenseManager` plays a role similar to a Facade: it hides the coordination
  between `SplitStrategy`, `Expense` construction, `Group`, and `BalanceSheet` behind two simple
  methods (`addExpense`/`removeExpense`), so callers never need to know about or manually coordinate
  all four pieces themselves.
