# Feature Specification: Custom Exercises

This document details the functional specifications and verification test cases for adding and removing custom exercise types.

---

## 🏗️ 1. Technical Reference
*   **Key Composables:**
    *   `AddCustomWorkoutDialog` in [HomeScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/home/HomeScreen.kt) (Add category dialog)
    *   `FilterChip` layout inside category rows in [HomeScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/home/HomeScreen.kt)
*   **Logical Components:**
    *   `addCustomWorkoutType` and `deleteCustomWorkoutType` inside [HomeViewModel.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/home/HomeViewModel.kt)
    *   `CustomWorkoutTypeEntity` in [CustomWorkoutTypeEntity.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/data/local/entity/CustomWorkoutTypeEntity.kt) (Database persistence)

---

## 📋 2. Functional Requirements
1.  **Creation Chip:** Users can add a custom exercise category by tapping the `+` chip located at the end of the Gym or Cardio lists.
2.  **Input Rules:**
    *   Empty inputs are blocked, displaying: `"Name cannot be empty"`.
    *   Duplicate exercise names within categories are blocked, displaying: `"This workout type already exists"`.
3.  **Visual Lifecycle:** Custom categories display a clear/delete `x` icon overlay on the chip. Tapping the `x` opens a delete confirmation dialog.
4.  **Data Isolation:** Deleting a custom exercise type deletes the category chip from the selection board, but **does not** delete or alter workouts already logged in history using that category.

---

## 🧪 3. Verification Test Suite

### Test Case 3.1: Add Custom Exercise
1.  On the Home screen, find the Gym categories list.
2.  Scroll to the end and tap the **`+`** chip.
3.  Type `"Calisthenics"` and tap **Save**.
4.  *Verify:* A custom chip named `"Calisthenics"` with a delete `"x"` icon appears in the selection list.
5.  *Verify:* A Snackbar showing `"Custom workout added!"` appears.

### Test Case 3.2: Duplicate Validation
1.  Open the add custom dialog again (Test Case 3.1).
2.  Type `"Calisthenics"` (exact duplicate) and tap **Save**.
3.  *Verify:* The dialog remains open and displays the error message: `"This workout type already exists"`.
4.  Type `"   "` (blank space) and tap **Save**.
5.  *Verify:* The dialog displays: `"Name cannot be empty"`.

### Test Case 3.3: Delete Custom Exercise Chip
1.  Tap the **`x`** close button on the custom `"Calisthenics"` chip.
2.  A deletion confirmation dialog appears. Tap **Delete**.
3.  *Verify:* The `"Calisthenics"` chip disappears from the selection list.
4.  *Verify:* A Snackbar showing `"Custom workout deleted"` is displayed.

---

## 🤖 4. Executing Automated Tests
AI agents can run unit tests verifying custom type validations and DB records:
```bash
./gradlew testDebugUnitTest --tests "com.ayogeshwaran.workoutlogger.presentation.home.HomeViewModelTest"
```
