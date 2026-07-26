# Implementation Plan - Fix `totpItemsCount` build error

This plan addresses the build error `No parameter with name 'totpItemsCount' found` in `VaultDataExtensions.kt`. This error occurred because `totpItemsCount` was removed from the `VaultState.ViewState.Content` data class as part of a previous refactoring, but the call site was not updated.

## User Review Required

> [!IMPORTANT]
> I will be removing `totpItemsCount` and the `validTotpIds` parameter from the `toViewState` extension function. This aligns with the previous plan to remove the TOTP home section.

## Proposed Changes

### Vault Feature

#### [MODIFY] [VaultDataExtensions.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultDataExtensions.kt)
- Remove `validTotpIds` parameter from `toViewState`.
- Remove `totpItemsCount` assignment in the `ViewState.Content` constructor call.

#### [MODIFY] [VaultViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultViewModel.kt)
- Update all calls to `toViewState` to remove the `validTotpIds` argument.
- Remove `validTotpIds` from `VaultState` and associated logic in `VaultViewModel` (repository call, internal actions) as it is no longer used.

### Tests

#### [MODIFY] [VaultViewModelTest.kt](file:///C:/Project/Bitwarden/android/app/src/test/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultViewModelTest.kt)
- Remove `totpItemsCount` assertions.
- Update calls to `toViewState` and `VaultState` constructor.

#### [MODIFY] [VaultDataExtensionsTest.kt](file:///C:/Project/Bitwarden/android/app/src/test/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultDataExtensionsTest.kt)
- Remove tests verifying `totpItemsCount`.
- Update calls to `toViewState`.

#### [MODIFY] [VaultScreenTest.kt](file:///C:/Project/Bitwarden/android/app/src/test/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultScreenTest.kt) & [VaultStateExtensionsTest.kt](file:///C:/Project/Bitwarden/android/app/src/test/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultStateExtensionsTest.kt)
- Remove `totpItemsCount` assignments and assertions.

## Verification Plan

### Automated Tests
- Run `:app:compileStandardDebugKotlin` to verify the build error is fixed.
- Run unit tests for `VaultViewModel` and `VaultDataExtensions`:
  - `./gradlew :app:testStandardDebugUnitTest --tests "com.x8bit.bitwarden.ui.vault.feature.vault.*"`

### Manual Verification
- None required for this build fix, as it aligns with the already approved UI changes.
