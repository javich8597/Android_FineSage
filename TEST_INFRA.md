# Test Infrastructure (TEST_INFRA)

This document outlines the testing methodology, feature inventory, test runner commands, and test structures for the Virtual Pet Feature in FinSage AI.

## 1. Testing Methodology
The test suite utilizes **opaque-box E2E and integration testing** to verify the behavior of the Virtual Pet Feature without relying on internal implementation details. 
- **Robolectric & Compose Test Rules**: Used to perform high-fidelity rendering, state validation, and interaction testing (e.g., clicking style select buttons, rendering progress indicators) in a sandboxed, JVM-based Android environment.
- **Unit Verification**: Used to check the underlying financial mathematics, state boundary thresholds, and view model transitions with high speed and zero flakiness.

---

## 2. Feature Inventory
The Virtual Pet Feature consists of three key capabilities that are fully covered by the test suite:

### Feature 1: Pet Selection UI
- **Description**: Allows users to select or switch the visual theme style of their pet.
- **Supported Styles**: Traditional (Cerdito/Gato/Perro), Zen (Tree), Futuristic (Cyber-Dragon).
- **Core Behavior**: Setting or changing styles updates the active view model state and dynamically alters the rendering container.

### Feature 2: Pet Mood & Growth States
- **Description**: Displays visual indicators (emojis, titles, descriptions) representing the pet's current mood and growth milestone.
- **Moods**: Happy (`HAPPY`), Idle (`IDLE`), Angry (`ANGRY`), and Petting (`PETTING`).
- **Growth/Progress**: Level indicators (e.g., `"LVL 3"`) and experience progress trackers.

### Feature 3: Financial Health Integration
- **Description**: Links user financial data directly to the pet's well-being and growth progression.
- **Heuristics**:
  - `Savings Rate = (Income + Expenses) / Income`
  - `Goal Progress = Average of (Saved Amount / Target Amount) across all goals`
  - `Combined Score = (Savings Rate + Goal Progress) / 2.0`
- **Transitions**:
  - `Combined Score > 0.40` or `Savings Rate > 0.30` -> `HAPPY`
  - `Combined Score > 0.10` or `Savings Rate > 0.00` -> `IDLE`
  - Otherwise -> `ANGRY`

---

## 3. Test Runner Commands
To run the test suite, execute the following command in the project root directory:

```powershell
.\gradlew test
```

This compiles all source code and executes both the existing tests and the virtual pet E2E test suite.

---

## 4. Test Structures & Tiers
The tests are organized into four progressive tiers to ensure full test depth and robustness:

| Tier | Category | Minimum Cases | Actual Cases | Description |
|---|---|---|---|---|
| **Tier 1** | Feature Coverage | 15 | 15 | 5 tests per feature validating happy-path flows. |
| **Tier 2** | Boundary & Edge Cases | 15 | 15 | Zero-value, NaN, Division by Zero, and threshold boundaries. |
| **Tier 3** | Cross-Feature Combinations | 3 | 3 | Complex interaction of style switches + mood consistency. |
| **Tier 4** | Real-World Scenarios | 5 | 5 | Multi-step user flows (salary day, splurge events, etc.). |
| **Total** | **All Tiers** | **38** | **38** | **Full E2E validation matrix.** |
