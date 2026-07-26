# Implementation Plan - Dynamic Vault Types (Final Fix)

This plan ensures all 8 supported vault item types are visible on the Home screen by removing a specific feature flag filter in the mapping layer.

## Proposed Changes

### [app]

#### [MODIFY] [VaultDataExtensions.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultDataExtensions.kt)
- **Change**: Remove the check for `isNewItemTypesEnabled` when generating the `vaultTypeItems` list inside `toViewState`.
- **Logic**:
    - Previously, `BANK_ACCOUNT`, `DRIVERS_LICENSE`, and `PASSPORT` were returning `null` if the flag was false.
    - Now, they will be included in the list like all other types, provided they are not hidden by the user in Customization settings.
- **Scope**: This change is **isolated** to the Home screen mapping. It does not affect the "Add Item" flow, the global feature flag system, or any other part of the application.

---

## Verification Plan

### Automated Tests
- Build the project: `./gradlew :app:compileStandardDebugKotlin`

### Manual Verification
- **Home Screen**: Open the app and verify all 8 types (Login, Card, Identity, Secure Note, SSH Key, Bank Account, Passport, Driver License) appear in the "Types" section.
- **Navigation**: Tap on "Bank Account" and verify it navigates to the correctly filtered listing.
- **Isolation**: Open the "Add Item" dialog (FAB) and verify that it **still respects** the feature flag (i.e., new types should not appear there if the flag is disabled), confirming the change was isolated to the Home screen.
