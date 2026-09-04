# Implementation Plan - Remove Bank Account, Passport, and Driver License Support

This plan details the steps to completely remove UI support for Bank Account, Passport, and Driver License vault item types, making the application behave as it did before these types were enabled.

## Proposed Changes

### [app]

#### [MODIFY] [VaultItemCipherType.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/model/VaultItemCipherType.kt)
- Keep the enum values but they will no longer be used in the UI mapping.

#### [MODIFY] [VaultDataExtensions.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultDataExtensions.kt)
- Update `toViewState` to strictly filter out `BANK_ACCOUNT`, `PASSPORT`, and `DRIVERS_LICENSE` from all cipher lists (`allCipherViews`, `activeCipherViews`, etc.).
- Update `vaultTypeItems` mapping to exclude these three types.
- Remove `when` branches for these types in `toVaultItemOrNull`.

#### [MODIFY] [VaultItemListingDataExtensions.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/itemlisting/util/VaultItemListingDataExtensions.kt)
- Remove branches for `BankAccount`, `License`, and `Passport` in `determineListingPredicate`, `toViewState`, and `updateWithAdditionalDataIfNecessary`.

#### [MODIFY] [VaultAddItemStateExtensions.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultAddItemStateExtensions.kt)
- Remove branches for these types in `toCipherView`, `toDriversLicense`, `toPassport`, and `toBankAccountView`.

#### [MODIFY] [VaultAddEditViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/addedit/VaultAddEditViewModel.kt)
- Remove `BANK_ACCOUNT`, `LICENSE`, and `PASSPORT` from `ItemTypeOption` enum.
- Remove handling in `handleAddItemClick`.

#### [MODIFY] [CreateVaultItemType.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/components/model/CreateVaultItemType.kt)
- Remove `BANK_ACCOUNT`, `LICENSE`, and `PASSPORT` from the enum.

#### [MODIFY] [VaultViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultViewModel.kt)
- Update `handleVaultTypeGroupClick` to remove navigation for these types.

#### [MODIFY] [VaultHomeCustomizationScreen.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/settings/vault/VaultHomeCustomizationScreen.kt)
- Remove branches for these types from the `Vault Types` list rendering.

#### [MODIFY] [SearchViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/search/SearchViewModel.kt)
- Ensure search results filter out these types.

---

## Verification Plan

### Automated Tests
- Build the project: `./gradlew :app:compileStandardDebugKotlin`

### Manual Verification
- **Home Screen**: Verify only Login, Card, Identity, Secure Note, and SSH Key are visible in the "Types" section.
- **Add Item**: Open the "Add Item" dialog and verify Bank Account, Passport, and Driver License are missing.
- **My Vault**: Verify these types do not appear in the vault listing.
- **Search**: Search for a known Bank Account item and verify it does not appear in the results.
- **Customization**: Verify these types are gone from the Customization settings.
- **Google Drive**: Verify that adding an attachment to a Login still works perfectly.
