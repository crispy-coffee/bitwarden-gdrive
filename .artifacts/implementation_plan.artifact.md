# Implementation Plan - Fix `totpItemsCount` build error

This plan addresses the build error `No parameter with name 'totpItemsCount' found` in `VaultDataExtensions.kt`. It appears `totpItemsCount` was removed from the `VaultState.ViewState.Content` data class but not all usages were updated.

## Proposed Changes

### [app]

#### [MODIFY] [VaultDataExtensions.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultDataExtensions.kt)
- Remove `validTotpIds: Set<String>` parameter from `toViewState` function.
- Remove `totpItemsCount` calculation and assignment within `toViewState`.

#### [MODIFY] [VaultViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultViewModel.kt)
- Update all calls to `toViewState` to remove the `validTotpIds` argument.

#### [MODIFY] [VaultDataExtensionsTest.kt](file:///C:/Project/Bitwarden/android/app/src/test/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultDataExtensionsTest.kt)
- Update all calls to `toViewState` to remove the `validTotpIds` argument.
- Remove `totpItemsCount` from `ViewState.Content` expected values and assertions.

#### [MODIFY] [VaultScreenTest.kt](file:///C:/Project/Bitwarden/android/app/src/test/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultScreenTest.kt)
- Remove `totpItemsCount` from `ViewState.Content` initializations.

#### [MODIFY] [VaultViewModelTest.kt](file:///C:/Project/Bitwarden/android/app/src/test/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultViewModelTest.kt)
- Remove `totpItemsCount` from `ViewState.Content` initializations.
- Update calls to `toViewState`.

#### [MODIFY] [VaultStateExtensionsTest.kt](file:///C:/Project/Bitwarden/android/app/src/test/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultStateExtensionsTest.kt)
- Remove `totpItemsCount` from `ViewState.Content` initializations.

## Verification Plan

### Automated Tests
- Run `:app:compileStandardDebugKotlin` to verify the build error is fixed.
- Run tests in `VaultDataExtensionsTest.kt`, `VaultScreenTest.kt`, `VaultViewModelTest.kt`, and `VaultStateExtensionsTest.kt` to ensure no regressions.
