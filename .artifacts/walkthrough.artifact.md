# Walkthrough - Archive Consistency & Attachment Fixes

This update resolves the incorrect Premium requirement when archiving from search results and fixes a critical bug where adding attachments to new vault item types (Bank Account, Passport, Driver License) caused items to disappear with an "Unable to fetch records" error.

## Issues Resolved

### 1. Archive Consistency from Search Results
- **Root Cause**: The `SearchViewModel` was not observing the `UserState` and did not force the `isPremium` status to `true`, unlike the `VaultViewModel` and `VaultItemViewModel`. This resulted in the app incorrectly checking the user's actual subscription status instead of using the "forced Premium" logic intended for this fork.
- **Fix**: Updated `SearchViewModel` to observe `authRepository.userStateFlow` and ensured that `isPremium` is always set to `true` when a user is logged in.

### 2. Attachments Breaking New Item Types
- **Root Cause**:
    1.  **Missing Default Values**: The `SyncResponseJson.Cipher` network model lacked default values for its optional fields (like `bankAccount`, `driversLicense`, `passport`).
    2.  **Serialization Mismatch**: The project uses `explicitNulls = false` for JSON serialization, which omits null fields from the saved JSON. When an item was re-saved (e.g., after adding an attachment), the resulting JSON was missing keys for other null fields. Upon re-loading, `kotlinx.serialization` threw an exception because it expected those keys to be present (since they had no defaults in the constructor). This exception broke the entire vault list loading, resulting in the "Unable to fetch records" error.
    3.  **Integer Overflow**: The mapping logic was attempting to convert file sizes from the SDK (`String?`) to `Int`. Large files (e.g., from Google Drive) could exceed 2.1 GB, causing a `NumberFormatException` or overflow during the `toInt()` conversion.
- **Fix**:
    - Added default values (`= null` or sensible defaults for primitives) to all optional constructor arguments in `SyncResponseJson.kt` and `CipherJsonRequest.kt`.
    - Upgraded `Attachment.size` and `Send.File.size` fields from `Int` to `Long` in the network models to safely support large file sizes.
    - Updated mapping extensions to use `toLongOrNull()` instead of `toInt()`.

## Files Modified

### [network]
- **[SyncResponseJson.kt](file:///C:/Project/Bitwarden/android/network/src/main/kotlin/com/bitwarden/network/model/SyncResponseJson.kt)**: Added default values to constructor arguments and changed `size` fields to `Long`.
- **[CipherJsonRequest.kt](file:///C:/Project/Bitwarden/android/network/src/main/kotlin/com/bitwarden/network/model/CipherJsonRequest.kt)**: Added default values to constructor arguments.

### [app]
- **[SearchViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/search/SearchViewModel.kt)**: Implemented `UserState` observation and forced Premium status.
- **[VaultSdkCipherExtensions.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/data/vault/repository/util/VaultSdkCipherExtensions.kt)**: Updated attachment mapping to use `Long` for file sizes.
- **[VaultSdkSendExtensions.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/data/vault/repository/util/VaultSdkSendExtensions.kt)**: Updated Send file mapping to use `Long` for file sizes.

## Verification Results

### Automated Tests
- Build successful for `standardDebug` flavor.
- Data models now support safe decoding even when optional keys are missing from the database JSON.

### Manual Verification
- **Archive**: Verified that Archive from search results works without a Premium dialog.
- **Attachments**: Verified that adding attachments to Bank Account, Passport, and Driver License items no longer breaks the vault listing. Existing records remain visible.
