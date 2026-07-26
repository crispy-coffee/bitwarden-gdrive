package com.x8bit.bitwarden.ui.vault.feature.attachments.util

import com.bitwarden.vault.CipherView
import com.x8bit.bitwarden.ui.vault.feature.attachments.AttachmentsState
import kotlinx.collections.immutable.toImmutableList

/**
 * Converts the [CipherView] into a [AttachmentsState.ViewState.Content].
 *
 * @param driveFileIds If provided, only Google Drive attachments with IDs in this set will be included.
 */
fun CipherView.toViewState(driveFileIds: Set<String>? = null): AttachmentsState.ViewState.Content {
    val gdriveAttachmentsFromFields = fields
        .orEmpty()
        .filter { it.name?.startsWith("__gdrive_attach_") == true }
        .mapNotNull { field ->
            val id = field.name?.removePrefix("__gdrive_attach_") ?: return@mapNotNull null

            if (driveFileIds != null) {
                val driveId = id.removePrefix("gdrive_")
                if (!driveFileIds.contains(driveId)) return@mapNotNull null
            }

            val value = field.value ?: ""
            val parts = if (value.startsWith("v1:")) {
                value.removePrefix("v1:").split(":")
            } else {
                value.split("|")
            }
            if (parts.size < 3) return@mapNotNull null
            val fileName = parts[0]
            val sizeName = parts[2]
            AttachmentsState.AttachmentItem(
                id = id,
                title = fileName,
                displaySize = sizeName,
                isLargeFile = try {
                    (parts[1].toLongOrNull() ?: 0L) >= TEN_MB_IN_BYTES
                } catch (_: Exception) {
                    false
                },
            )
        }

    val regularAttachments = this.attachments
        .orEmpty()
        .mapNotNull {
            val id = it.id ?: return@mapNotNull null

            if (id.startsWith("gdrive_") && driveFileIds != null) {
                val driveId = id.removePrefix("gdrive_")
                if (!driveFileIds.contains(driveId)) return@mapNotNull null
            }

            AttachmentsState.AttachmentItem(
                id = id,
                title = it.fileName.orEmpty(),
                displaySize = it.sizeName.orEmpty(),
                isLargeFile = it.isLargeFile(),
            )
        }

    return AttachmentsState.ViewState.Content(
        originalCipher = this,
        attachments = (regularAttachments + gdriveAttachmentsFromFields)
            .distinctBy { it.id }
            .toImmutableList(),
        newAttachment = null,
    )
}
