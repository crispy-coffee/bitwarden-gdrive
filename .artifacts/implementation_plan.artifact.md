# Implementation Plan - UX Fixes & Refinements (Turn 4)

This plan addresses the PDF preview crash, refines the "Customization" screen, and cleans up the Home screen UI.

## Proposed Changes

### 1. PDF Preview Fix (Crash Resolution)
- **File**: [PdfPreviewContent.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/attachments/preview/component/PdfPreviewContent.kt)
- **Change**: Implement a custom `Saver` for the `Offset` class and use it with `rememberSaveable`.
- **Reason**: The app currently crashes because `Offset` is not natively saveable in a `Bundle`. A custom `Saver` will allow the pan/zoom state to survive device rotation smoothly without triggering an `IllegalArgumentException`.

### 2. Customization Screen Refinement
- **File**: [SettingsViewModel.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/settings/SettingsViewModel.kt)
- **Change**:
    - Rename `VAULT_HOME_CUSTOMIZATION` to `CUSTOMIZATION`.
    - Update label to "Customization".
    - Change icon from `ic_paintbrush` to `ic_filter` (visually distinct from Appearance).
- **File**: [VaultHomeCustomizationScreen.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/platform/feature/settings/vault/VaultHomeCustomizationScreen.kt)
- **Change**:
    - Update title to "Customization".
    - Reorganize into "Navigation" and "Vault Types" sections.
    - Generate the list of types dynamically using `VaultItemCipherType.entries` instead of hardcoding.
- **Reason**: To provide a cleaner, professional, and future-proof customization experience.

### 3. Home Screen Cleanup
- **File**: [VaultDataExtensions.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/util/VaultDataExtensions.kt)
- **Change**:
    - Ensure `folderItems` strictly filters out the "No Folder" group (where `id == null`).
    - Keep `noFolderItems` as an empty list for the Home screen state.
- **File**: [VaultContent.kt](file:///C:/Project/Bitwarden/android/app/src/main/kotlin/com/x8bit/bitwarden/ui/vault/feature/vault/VaultContent.kt)
- **Change**: Remove the logic for rendering ungrouped items or the "No Folder" header.
- **Reason**: To simplify the Home screen while preserving the underlying folder logic in "My Vault" and search.

---

## Verification Plan

### Automated Tests
- Verify successful project build: `./gradlew :app:compileStandardDebugKotlin`.

### Manual Verification
- **PDF**: Open a PDF, zoom in, and rotate the device. Verify the zoom level and position are preserved and no crash occurs.
- **Customization**: Verify the entry in main Settings has the new icon and label. Verify all vault types are present in the customization list.
- **Filtering**: Disable "Secure Note", verify it disappears from the Home screen.
- **Home UI**: Verify "No Folder" section is absent even if ungrouped items exist.
