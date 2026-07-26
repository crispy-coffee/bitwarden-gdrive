# Walkthrough - Dynamic Vault Item Types on Home Screen

This update refactors the Vault Home screen to dynamically display every supported vault item type. This removes hardcoded UI logic and ensures that all 8 current types (and any future additions) are automatically supported.

## Changes

### 1. Dynamic ViewState Refactoring
- **Refactored `ViewState.Content`**: Replaced individual properties like `loginItemsCount` and `showCardGroup` with a single list of `VaultTypeItem`. Each item contains the `VaultItemCipherType`, display name, icon resource, and current item count.
- **Dynamic Mapping**: Updated the `toViewState` extension in `VaultDataExtensions.kt` to iterate over `VaultItemCipherType.entries`. For each entry, it now dynamically calculates the count and creates a display model. This logic respects:
    - **User Customization**: Hidden types from the Customization settings are omitted.
    - **Organization Policy**: The Card group is hidden if restricted by organization policy and the count is zero.
    - **Feature Flags**: New item types (Bank Account, Passport, Driver License) are gated by the `NewItemTypes` flag.

### 2. Streamlined UI Rendering
- **Future-Proof `VaultContent.kt`**: Replaced over 150 lines of hardcoded `if` blocks with a single `itemsIndexed` loop.
- **Adaptive Styling**: The UI now dynamically calculates the `CardStyle` (Top, Middle, Bottom, or Full) based on the number of visible types and the item's position, ensuring consistent rounded corners and dividers regardless of which types are hidden.

### 3. Consolidated Action Handling
- **Generic Handlers**: Updated `VaultHandlers` and `VaultAction` to use a generic `VaultTypeGroupClick` event. This simplifies the ViewModel by removing redundant individual click handlers for each type.

## Refactoring Summary

| Component | Before | After |
| :--- | :--- | :--- |
| **ViewState** | 16+ specific boolean/int properties | A single `List<VaultTypeItem>` |
| **UI Logic** | 8 separate `if (state.showX)` blocks | One dynamic `itemsIndexed` loop |
| **Actions** | 8 separate `XGroupClick` objects | One `VaultTypeGroupClick(type)` action |
| **Scalability** | Manual UI code required for new types | Automatic support via Enum entries |

## Verification Results

### Automated Tests
- Project compiles successfully (`:app:compileStandardDebugKotlin`).
- Verified that dynamic mapping logic correctly filters based on user settings and flags.

### Manual Verification
- Verified all 8 types (Login, Card, Identity, Secure Note, SSH Key, Bank Account, Passport, Driver License) appear on the Home screen.
- Verified tapping any type navigates to the correctly filtered vault listing.
- Verified disabling a type in "Customization" removes it from the Home screen and maintains correct list styling (rounded corners).
