package com.x8bit.bitwarden.ui.vault.feature.attachments.util

import com.bitwarden.vault.CipherView
import com.x8bit.bitwarden.ui.vault.feature.attachments.AttachmentsState
import com.x8bit.bitwarden.ui.vault.feature.attachments.util.TEN_MB_IN_BYTES
import kotlinx.collections.immutable.toImmutableList

/**
 * Converts the [CipherView] into a [AttachmentsState.ViewState.Content].
 */
fun CipherView.toViewState(): AttachmentsState.ViewState.Content {
    val gdriveAttachmentsFromFields = fields
        .orEmpty()
        .filter { it.name?.startsWith("__gdrive_attach_") == true }
        .mapNotNull { field ->
            val id = field.name?.removePrefix("__gdrive_attach_") ?: return@mapNotNull null
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

    return AttachmentsState.ViewState.Content(
        originalCipher = this,
        attachments = (this
            .attachments
            .orEmpty()
            .mapNotNull {
                val id = it.id ?: return@mapNotNull null
                AttachmentsState.AttachmentItem(
                    id = id,
                    title = it.fileName.orEmpty(),
                    displaySize = it.sizeName.orEmpty(),
                    isLargeFile = it.isLargeFile(),
                )
            } + gdriveAttachmentsFromFields)
            .distinctBy { it.id }
            .toImmutableList(),
        newAttachment = null,
    )
}
