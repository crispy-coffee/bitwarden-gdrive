# Implementation Plan - Google Drive Attachment Integration Polish

This plan covers removing the broken TOTP home section and improving the Google Drive integration in the Bitwarden Android app.

## User Review Required

> [!IMPORTANT]
> The broken TOTP/Verification Code card will be completely removed from the Vault home screen. This does NOT affect TOTP functionality within individual vault items.

> [!IMPORTANT]
> Google Drive integration will now cross-reference every attachment with the actual files in Drive. Attachments not found in Drive will be ignored/filtered out from the UI.

## Proposed Changes

### 1. Remove Broken TOTP Home Section

#### [DELETE] [VerificationCodeViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/verificationcode/VerificationCodeViewModel.kt)
#### [DELETE] [VerificationCodeScreen.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/verificationcode/VerificationCodeScreen.kt)
#### [DELETE] [VerificationCodeNavigation.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/verificationcode/VerificationCodeNavigation.kt)
#### [DELETE] [VerificationCodeItem.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/verificationcode/VerificationCodeItem.kt)
#### [DELETE] [VerificationCodeHandlers.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/verificationcode/handlers/VerificationCodeHandlers.kt)
#### [MODIFY] [VaultContent.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultContent.kt)
- Remove TOTP header and "Verification Codes" group item.
#### [MODIFY] [VaultViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultViewModel.kt)
- Remove `totpItemsCount` calculation and `VerificationCodesClick` action handler.
#### [MODIFY] [VaultGraphNavigation.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultGraphNavigation.kt)
- Remove `vaultVerificationCodeDestination`.

---

### 2. Google Drive Integration Improvements

#### [MODIFY] [GoogleDriveManager.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/data/platform/manager/GoogleDriveManager.kt)
- Add `getAccountInfo()` to fetch name, email, and profile photo.
- Add `listFiles()` to list files in `appDataFolder`.
- Improve error handling in existing methods.

#### [MODIFY] [GoogleDriveViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/settings/googledrive/GoogleDriveViewModel.kt)
- Update `GoogleDriveState` to include account info, attachment count, and sync time.
- Add logic to refresh account info and attachment count on `init` and `resume`.
- Handle account switching and clearing state.

#### [MODIFY] [GoogleDriveScreen.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/settings/googledrive/GoogleDriveScreen.kt)
- Redesign UI to show account details, connection status, and sync info.
- Implement better loading and empty states.

#### [MODIFY] [AttachmentsViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/attachments/AttachmentsViewModel.kt)
- Cross-reference attachments with Google Drive file list.
- Implement automatic refresh after upload/delete.
- Add better error handling for Drive failures.

#### [MODIFY] [SettingsRepository.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/data/platform/repository/SettingsRepository.kt) & [SettingsDiskSource.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/data/platform/datasource/disk/SettingsDiskSource.kt)
- Add storage for `googleDriveLastSyncTime` and `googleDriveAccountEmail` (to detect switching).

---

### 3. Verification Plan

#### Automated Tests
- Run existing tests to ensure no regression in vault functionality.
- Add unit tests for `GoogleDriveViewModel` to verify account detection and refresh logic.

#### Manual Verification
- Deploy to device/emulator.
- Verify TOTP card is gone from home screen.
- Connect Google Drive and verify account info is displayed correctly.
- Upload/Delete attachments and verify auto-refresh.
- Switch Google accounts on device and verify the app detects it.
- Verify that orphaned attachments (deleted in Drive but present in Bitwarden) are hidden.
