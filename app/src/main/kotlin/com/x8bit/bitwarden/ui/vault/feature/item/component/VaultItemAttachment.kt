package com.x8bit.bitwarden.ui.vault.feature.item.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bitwarden.ui.platform.base.util.annotatedStringResource
import com.bitwarden.ui.platform.base.util.cardStyle
import com.bitwarden.ui.platform.base.util.spanStyleOf
import com.bitwarden.ui.platform.components.button.BitwardenStandardIconButton
import com.bitwarden.ui.platform.components.dialog.BitwardenTwoButtonDialog
import com.bitwarden.ui.platform.components.model.CardStyle
import com.bitwarden.ui.platform.components.util.rememberVectorPainter
import com.bitwarden.ui.platform.resource.BitwardenDrawable
import com.bitwarden.ui.platform.resource.BitwardenString
import com.bitwarden.ui.platform.theme.BitwardenTheme
import com.x8bit.bitwarden.ui.vault.feature.item.VaultItemState

/**
 * Attachment UI common for all item types.
 */
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun VaultItemAttachment(
    attachmentItem: VaultItemState.ViewState.Content.Common.AttachmentItem,
    onAttachmentDownloadClick: (VaultItemState.ViewState.Content.Common.AttachmentItem) -> Unit,
    onAttachmentPreviewClick: (VaultItemState.ViewState.Content.Common.AttachmentItem) -> Unit,
    onAttachmentShareClick: (VaultItemState.ViewState.Content.Common.AttachmentItem) -> Unit,
    onUpgradeToPremiumClick: () -> Unit,
    cardStyle: CardStyle,
    modifier: Modifier = Modifier,
) {
    var shouldShowPremiumWarningDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowSizeWarningDialog by rememberSaveable { mutableStateOf(false) }

    val iconRes = remember(attachmentItem.title) {
        val extension = attachmentItem.title.substringAfterLast(".", "").lowercase()
        when (extension) {
            "pdf" -> BitwardenDrawable.ic_file_text
            "jpg", "jpeg", "png", "gif", "webp" -> BitwardenDrawable.ic_camera
            "zip", "rar", "7z", "tar", "gz" -> BitwardenDrawable.ic_archive
            else -> BitwardenDrawable.ic_file
        }
    }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .defaultMinSize(minHeight = 72.dp)
            .cardStyle(
                cardStyle = cardStyle,
                padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                onClick = {
                    if (!attachmentItem.isDownloadAllowed) {
                        shouldShowPremiumWarningDialog = true
                        return@cardStyle
                    }
                    onAttachmentPreviewClick(attachmentItem)
                },
            )
            .testTag("CipherAttachment"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = rememberVectorPainter(id = iconRes),
                contentDescription = null,
                tint = BitwardenTheme.colorScheme.icon.secondary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachmentItem.title,
                    color = BitwardenTheme.colorScheme.text.primary,
                    style = BitwardenTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("AttachmentNameLabel"),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = attachmentItem.displaySize,
                        color = BitwardenTheme.colorScheme.text.secondary,
                        style = BitwardenTheme.typography.bodySmall,
                        modifier = Modifier.testTag("AttachmentSizeLabel"),
                    )

                    attachmentItem.modifiedDate?.let {
                        Text(
                            text = " • ",
                            color = BitwardenTheme.colorScheme.text.secondary,
                            style = BitwardenTheme.typography.bodySmall,
                        )
                        Text(
                            text = it.substringBefore("T"), // Simple date display
                            color = BitwardenTheme.colorScheme.text.secondary,
                            style = BitwardenTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BitwardenStandardIconButton(
                    vectorIconRes = BitwardenDrawable.ic_share_small,
                    contentDescription = stringResource(id = BitwardenString.share),
                    onClick = {
                        if (!attachmentItem.isDownloadAllowed) {
                            shouldShowPremiumWarningDialog = true
                            return@BitwardenStandardIconButton
                        }

                        onAttachmentShareClick(attachmentItem)
                    },
                    modifier = Modifier.testTag("AttachmentShareButton"),
                )

                BitwardenStandardIconButton(
                    vectorIconRes = BitwardenDrawable.ic_download,
                    contentDescription = stringResource(id = BitwardenString.download),
                    isExternalLink = true,
                    onClick = {
                        if (!attachmentItem.isDownloadAllowed) {
                            shouldShowPremiumWarningDialog = true
                            return@BitwardenStandardIconButton
                        }

                        if (attachmentItem.isLargeFile) {
                            shouldShowSizeWarningDialog = true
                            return@BitwardenStandardIconButton
                        }

                        onAttachmentDownloadClick(attachmentItem)
                    },
                    modifier = Modifier.testTag("AttachmentDownloadButton"),
                )
            }
        }
    }

    if (shouldShowPremiumWarningDialog) {
        BitwardenTwoButtonDialog(
            title = stringResource(id = BitwardenString.premium_subscription_required),
            message = stringResource(id = BitwardenString.attachments_are_a_premium_feature),
            confirmButtonText = stringResource(id = BitwardenString.upgrade_to_premium),
            dismissButtonText = stringResource(id = BitwardenString.cancel),
            onConfirmClick = {
                shouldShowPremiumWarningDialog = false
                onUpgradeToPremiumClick()
            },
            onDismissClick = { shouldShowPremiumWarningDialog = false },
            onDismissRequest = { shouldShowPremiumWarningDialog = false },
        )
    }

    if (shouldShowSizeWarningDialog) {
        BitwardenTwoButtonDialog(
            title = stringResource(id = BitwardenString.download_attachment),
            message = annotatedStringResource(
                id = BitwardenString.attachment_large_warning,
                args = arrayOf(attachmentItem.displaySize),
                style = spanStyleOf(
                    color = BitwardenTheme.colorScheme.text.primary,
                    textStyle = BitwardenTheme.typography.bodyMedium,
                ),
            ),
            confirmButtonText = stringResource(BitwardenString.yes),
            dismissButtonText = stringResource(BitwardenString.no),
            onConfirmClick = {
                shouldShowSizeWarningDialog = false
                onAttachmentDownloadClick(attachmentItem)
            },
            onDismissClick = { shouldShowSizeWarningDialog = false },
            onDismissRequest = { shouldShowSizeWarningDialog = false },
        )
    }
}
