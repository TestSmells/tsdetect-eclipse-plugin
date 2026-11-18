# Approved Exceptions to the SRS Engine Constraint

The SRS places one technical constraint on this project:

> The existing tsDetect engine must remain unchanged; the plugins must integrate
> with the current implementation rather than replacing its detection algorithms.

Every file under `src/main/java/testsmell/` outside the `plugin/` package is
therefore off limits, and `.github/workflows/eclipse-ci.yml` enforces this
mechanically: it verifies all engine source files against
`tools/corpus/engine-checksums.txt` before compiling, and fails the build on any
difference.

This document records the exceptions the client has approved, so that a checksum
change can always be traced to a decision rather than an accident.

---

## Exception 1 — DependentTest detector

| | |
|---|---|
| **File** | `src/main/java/testsmell/smell/DependentTest.java` |
| **Requested** | 9 November 2026 |
| **Approved** | 16 November 2026 |
| **Approved by** | Mohamed Wiem Mkaouer (client) |
| **Scope** | This file only |

### Why it was requested

The Dependent Test detector could not report a smell under any input. Two
independent defects, either of which alone was sufficient:

**The membership test compared a `String` to a `Stream`.**

```java
x.getName().equals(testMethods.stream().map(z -> z.getMethodDeclaration().getNameAsString()))
```

A `String` is never equal to a `Stream`, so the condition was false for every
method in every file and the analyzer's element list was never populated.

**The smell flag was never set.** Even on a match, the element was added without
`setHasSmell(true)`, and `getHasSmell()` counts only elements whose flag is set.

A corrected implementation was present in the same file, commented out beneath
the broken code.

### Client decision

> Please proceed with the eight-line fix as an approved SRS exception. Since the
> Dependent Test detector cannot fire under any input, documenting it only as a
> limitation would leave a required feature unusable.

Reporting the defect upstream to the tsDetect maintainers was approved as a
secondary action, not a substitute for correcting the delivered version.

### What changed

`DependentTest.runAnalysis` only. Three corrections:

1. Membership is tested with `contains()` against a `Set` of the test method
   names declared in the file, rather than comparing a name to a `Stream`.
2. `setHasSmell` is called with the result for every test method examined,
   matching the convention the other analyzers follow.
3. A method calling itself is excluded — that is recursion, not a dependency
   between tests.

Two imports were added (`java.util.HashSet`, `java.util.Set`). The commented-out
original implementation was removed. No other engine file was touched, and no
detection logic outside this analyzer was altered.

### Validation

A regression fixture was added at `fixtures/testAccount.java` /
`fixtures/Account.java`, in which `testBalanceAfterDeposit()` calls
`testDeposit()`.

| Engine | Result on identical input |
|---|---|
| Unmodified | `Dependent Test = false` |
| Corrected | `Dependent Test = true` |

Full regression corpus re-run after the change:

- Fixture corpus coverage rises from **20 of 21** to **21 of 21** detectors.
- All other detector results are unchanged.
- The shared cross-IDE corpus is unaffected: 5 matched, 5 declared divergences,
  0 unexpected, 0 stale. No case in that corpus contains a test calling another
  test, so no expected result moves and no new divergence appears.

`tools/corpus/engine-checksums.txt` was regenerated to cover the corrected file.
This is the only reason the engine checksums differ from the inherited engine.

### Not changed

The three remaining engine defects recorded in
`documentation/regression-baseline.md` are untouched and remain out of scope:

- Windows-only path splitting in `TestFile.getTestFileName()` and `getTagName()`
- `ResourceOptimism` comparing a `Type` to a `String`, so its assigned-variable
  case never fires
- A stray `System.out.println` in `ResourceOptimism`

No exception has been requested or granted for any of these.
