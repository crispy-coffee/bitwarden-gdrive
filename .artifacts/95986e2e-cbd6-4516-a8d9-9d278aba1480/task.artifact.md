# Task List - Google Drive Attachment Polish

## 1. Remove Broken TOTP Home Card
- [x] Identify and delete unused Verification Code files:
    - [x] `VerificationCodeViewModel.kt` & tests
    - [x] `VerificationCodeScreen.kt` & tests
    - [x] `VerificationCodeNavigation.kt`
    - [x] `VerificationCodeItem.kt`
    - [x] `VerificationCodeHandlers.kt`
    - [x] `VerificationCodeDataUtil.kt` (test util)
- [x] Remove TOTP home section from `VaultContent.kt`.
- [x] Clean up `VaultViewModel.kt`:
    - [x] Remove `totpItemsCount` and related logic.
    - [x] Remove `VerificationCodesClick` action and `NavigateToVerificationCodeScreen` event.
- [x] Update `VaultGraphNavigation.kt` to remove the verification code destination.
- [x] Update `VaultScreen.kt` to remove the navigation callback.

## 2. Redesign Google Drive Settings Screen
- [x] Enhance `GoogleDriveManager.kt`:
    - [x] Add `getAccountInfo()` to fetch profile name, email, and photo URL.
    - [x] Add `listFiles()` to fetch the list of attachments from `appDataFolder`.
    - [x] Add `verifyConnection()` to check token validity.
- [x] Update `GoogleDriveViewModel.kt`:
    - [x] Expand `GoogleDriveState` with account details, sync time, and attachment count.
    - [x] Implement auto-refresh logic on `init` and `resume`.
    - [x] Add `RefreshClick` action.
- [x] Update `GoogleDriveScreen.kt`:
    - [x] Implement the new professional layout with profile picture, account details, and sync status.
    - [x] Add pull-to-refresh.
    - [x] Implement better loading and empty states.

## 3. Smart Account Detection & Handling
- [x] Implement account verification logic in `GoogleDriveManager.kt`.
- [x] Add account switching detection in `GoogleDriveViewModel.kt` (comparing current email with last used email).
- [x] Ensure state is cleared immediately when account change is detected.

## 4. Google Drive as Single Source of Truth
- [x] Update `AttachmentsViewModel.kt`:
    - [x] Fetch the current list of files from Google Drive when loading attachments.
    - [x] Cross-reference the cipher's attachment metadata with the Drive file list.
    - [x] Filter out any attachments that don't exist in Drive.
- [x] Implement automatic refresh in `AttachmentsViewModel.kt` after upload/delete actions.

## 5. Better UX (States & Error Handling)
- [x] Implement user-friendly error messages for Drive API failures (OAuth expiration, permission revoked).
- [x] Add timeouts and retry logic for loading states.
- [x] Add empty states for "No account connected", "No attachments", and "Offline".

## 6. Debug Logging & Cleanup
- [x] Add structured Timber logs across Google Drive flows.
- [x] Remove any remaining dead code or obsolete logic.

## 7. Verification
- [x] Verify TOTP home card is gone.
- [x] Verify individual item TOTP works.
- [x] Verify Google Drive account info and sync status.
- [x] Verify attachment filtering (missing in Drive -> hidden in app).
- [x] Verify auto-refresh after actions.
