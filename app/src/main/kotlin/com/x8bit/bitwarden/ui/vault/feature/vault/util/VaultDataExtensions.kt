package com.x8bit.bitwarden.ui.vault.feature.vault.util

import androidx.core.net.toUri
import com.bitwarden.collections.CollectionView
import com.bitwarden.ui.platform.components.icon.model.IconData
import com.bitwarden.ui.platform.resource.BitwardenDrawable
import com.bitwarden.ui.platform.resource.BitwardenString
import com.bitwarden.ui.util.asText
import com.bitwarden.vault.CipherListView
import com.bitwarden.vault.CipherListViewType
import com.bitwarden.vault.CipherRepromptType
import com.bitwarden.vault.CipherType
import com.bitwarden.vault.FolderView
import com.bitwarden.vault.LoginUriView
import com.x8bit.bitwarden.data.autofill.util.card
import com.x8bit.bitwarden.data.autofill.util.login
import com.x8bit.bitwarden.data.platform.util.isActive
import com.x8bit.bitwarden.data.vault.repository.model.VaultData
import com.x8bit.bitwarden.data.vault.repository.util.toFailureCipherListView
import com.x8bit.bitwarden.ui.vault.feature.util.getFilteredCollections
import com.x8bit.bitwarden.ui.vault.feature.util.getFilteredFolders
import com.x8bit.bitwarden.ui.vault.feature.util.toLabelIcons
import com.x8bit.bitwarden.ui.vault.feature.util.toOverflowActions
import com.x8bit.bitwarden.ui.vault.feature.vault.VaultState
import com.x8bit.bitwarden.ui.vault.feature.vault.model.VaultFilterType
import com.x8bit.bitwarden.ui.vault.model.VaultItemCipherType
import com.x8bit.bitwarden.ui.vault.model.findVaultCardBrandWithNameOrNull
import com.x8bit.bitwarden.ui.vault.util.toSdkCipherType
import com.x8bit.bitwarden.ui.vault.util.toVaultItemCipherType
import kotlinx.collections.immutable.persistentListOf
import timber.log.Timber

private const val ANDROID_URI = "androidapp://"
private const val IOS_URI = "iosapp://"

/**
 * The maximum number of no folder items that can be displayed before the UI creates a
 * no folder "folder".
 */
private const val NO_FOLDER_ITEM_THRESHOLD: Int = 100

/**
 * Transforms [VaultData] into [VaultState.ViewState] using the given [vaultFilterType].
 */
@Suppress("LongMethod", "LongParameterList")
fun VaultData.toViewState(
    isPremium: Boolean,
    hasMasterPassword: Boolean,
    isIconLoadingDisabled: Boolean,
    baseIconUrl: String,
    vaultFilterType: VaultFilterType,
    restrictItemTypesPolicyOrgIds: List<String>,
    isNewItemTypesEnabled: Boolean,
    hiddenVaultItemTypes: Set<String> = emptySet(),
    hiddenVaultHomeSections: Set<String> = emptySet(),
): VaultState.ViewState {
    val allCipherViews =
        decryptCipherListResult
            .successes
            .plus(
                elements = decryptCipherListResult
                    .failures
                    .map { cipher ->
                        cipher.toFailureCipherListView()
                    },
            )
            .filter {
                it.type !is CipherListViewType.BankAccount &&
                    it.type !is CipherListViewType.Passport &&
                    it.type !is CipherListViewType.DriversLicense
            }
            .applyFilters(
                vaultFilterType = vaultFilterType,
                restrictItemTypesPolicyOrgIds = restrictItemTypesPolicyOrgIds,
                excludeArchived = false,
                excludeDeleted = false,
            )

    val activeCipherViews = allCipherViews
        .filter { it.isActive }
        .filter { it.type.toSdkCipherType().toVaultItemCipherType().name !in hiddenVaultItemTypes }

    val activeDecryptedCipherViews = decryptCipherListResult
        .successes
        .filter {
            it.type !is CipherListViewType.BankAccount &&
                it.type !is CipherListViewType.Passport &&
                it.type !is CipherListViewType.DriversLicense
        }
        .applyFilters(
            vaultFilterType = vaultFilterType,
            restrictItemTypesPolicyOrgIds = restrictItemTypesPolicyOrgIds,
            excludeArchived = true,
            excludeDeleted = true,
        )
        .filter { it.type.toSdkCipherType().toVaultItemCipherType().name !in hiddenVaultItemTypes }

    val activeUndecryptableCipherViews = decryptCipherListResult
        .failures
        .map { cipher ->
            cipher.toFailureCipherListView()
        }
        .filter {
            it.type !is CipherListViewType.BankAccount &&
                it.type !is CipherListViewType.Passport &&
                it.type !is CipherListViewType.DriversLicense
        }
        .applyFilters(
            vaultFilterType = vaultFilterType,
            restrictItemTypesPolicyOrgIds = restrictItemTypesPolicyOrgIds,
            excludeArchived = true,
            excludeDeleted = true,
        )
        .filter { it.type.toSdkCipherType().toVaultItemCipherType().name !in hiddenVaultItemTypes }

    val filteredFolderViewList = folderViewList
        .toFilteredList(
            cipherList = activeCipherViews,
            vaultFilterType = vaultFilterType,
        )
        .getFilteredFolders()

    val filteredCollectionViewList = collectionViewList
        .toFilteredList(vaultFilterType)
        .getFilteredCollections()

    return if (allCipherViews.isEmpty()) {
        VaultState.ViewState.NoItems
    } else {
        val cardCount = activeCipherViews.count { it.type is CipherListViewType.Card }
        val archiveCount = allCipherViews.count {
            it.archivedDate != null && it.deletedDate == null
        }

        val vaultTypeItems = VaultItemCipherType.entries.mapNotNull { type ->
            if (type.name in hiddenVaultItemTypes) return@mapNotNull null
            if (type == VaultItemCipherType.BANK_ACCOUNT ||
                type == VaultItemCipherType.DRIVERS_LICENSE ||
                type == VaultItemCipherType.PASSPORT
            ) return@mapNotNull null

            val count = activeCipherViews.count { it.type.toSdkCipherType().toVaultItemCipherType() == type }
            val label = when (type) {
                VaultItemCipherType.LOGIN -> BitwardenString.type_login
                VaultItemCipherType.CARD -> BitwardenString.type_card
                VaultItemCipherType.IDENTITY -> BitwardenString.type_identity
                VaultItemCipherType.SECURE_NOTE -> BitwardenString.type_secure_note
                VaultItemCipherType.SSH_KEY -> BitwardenString.type_ssh_key
                else -> BitwardenString.vault
            }.asText()

            val iconRes = when (type) {
                VaultItemCipherType.LOGIN -> BitwardenDrawable.ic_globe
                VaultItemCipherType.CARD -> BitwardenDrawable.ic_payment_card
                VaultItemCipherType.IDENTITY -> BitwardenDrawable.ic_id_card
                VaultItemCipherType.SECURE_NOTE -> BitwardenDrawable.ic_note
                VaultItemCipherType.SSH_KEY -> BitwardenDrawable.ic_ssh_key
                else -> BitwardenDrawable.ic_vault
            }

            val testTag = when (type) {
                VaultItemCipherType.LOGIN -> "LoginFilter"
                VaultItemCipherType.CARD -> "CardFilter"
                VaultItemCipherType.IDENTITY -> "IdentityFilter"
                VaultItemCipherType.SECURE_NOTE -> "SecureNoteFilter"
                VaultItemCipherType.SSH_KEY -> "SshKeyFilter"
                else -> ""
            }

            // For cards, we also check the policy
            if (type == VaultItemCipherType.CARD && cardCount == 0 && restrictItemTypesPolicyOrgIds.isNotEmpty()) {
                return@mapNotNull null
            }

            VaultState.ViewState.VaultTypeItem(
                type = type,
                label = label,
                iconRes = iconRes,
                count = count,
                testTag = testTag
            )
        }

        VaultState.ViewState.Content(
            itemTypesCount = vaultTypeItems.size,
            vaultTypeItems = vaultTypeItems,
            favoriteItems = activeDecryptedCipherViews
                .filter { it.favorite }
                .mapNotNull {
                    it.toVaultItemOrNull(
                        hasMasterPassword = hasMasterPassword,
                        isIconLoadingDisabled = isIconLoadingDisabled,
                        baseIconUrl = baseIconUrl,
                        isPremiumUser = isPremium,
                        hasDecryptionError = false,
                    )
                }
                .plus(
                    elements = activeUndecryptableCipherViews
                        .filter { it.favorite }
                        .mapNotNull {
                            it.toVaultItemOrNull(
                                hasMasterPassword = hasMasterPassword,
                                isIconLoadingDisabled = isIconLoadingDisabled,
                                baseIconUrl = baseIconUrl,
                                isPremiumUser = isPremium,
                                hasDecryptionError = true,
                            )
                        },
                ),
            folderItems = filteredFolderViewList
                .map { folderView ->
                    VaultState.ViewState.FolderItem(
                        id = folderView.id,
                        name = folderView.name.asText(),
                        itemCount = activeCipherViews
                            .count {
                                !it.id.isNullOrBlank() &&
                                    folderView.id == it.folderId
                            },
                    )
                },
            noFolderItems = emptyList(), // Removed from Home screen
            collectionItems = filteredCollectionViewList
                .filter { it.id != null }
                .map { collectionView ->
                    VaultState.ViewState.CollectionItem(
                        id = requireNotNull(collectionView.id),
                        name = collectionView.name,
                        itemCount = activeCipherViews
                            .count {
                                !it.id.isNullOrBlank() &&
                                    collectionView.id in it.collectionIds
                            },
                    )
                },
            trashItemsCount = allCipherViews.count { it.deletedDate != null },
            archivedItemsCount = archiveCount.takeIf { isPremium || archiveCount > 0 },
            archiveEndIcon = BitwardenDrawable.ic_locked.takeIf { !isPremium && archiveCount == 0 },
            archiveSubText = BitwardenString
                .premium_subscription_required
                .asText()
                .takeIf { !isPremium && archiveCount == 0 },
        )
    }
}

/**
 * Method to build the icon data for login item icons.
 */
fun List<LoginUriView>?.toLoginIconData(
    isIconLoadingDisabled: Boolean,
    baseIconUrl: String,
    usePasskeyDefaultIcon: Boolean,
): IconData {
    val defaultIconRes = if (usePasskeyDefaultIcon) {
        BitwardenDrawable.ic_bw_passkey
    } else {
        BitwardenDrawable.ic_globe
    }

    var uri = this
        ?.map { it.uri }
        ?.firstOrNull { uri -> uri?.contains(".") == true }
        ?: return IconData.Local(defaultIconRes)

    if (uri.startsWith(ANDROID_URI)) {
        return IconData.Local(BitwardenDrawable.ic_android)
    }

    if (uri.startsWith(IOS_URI)) {
        return IconData.Local(BitwardenDrawable.ic_ios)
    }

    if (isIconLoadingDisabled) {
        return IconData.Local(defaultIconRes)
    }

    if (!uri.contains("://")) {
        uri = "http://$uri"
    }

    val iconUri = uri.toUri()
    val hostname = iconUri.host

    val url = "$baseIconUrl/$hostname/icon.png"

    return IconData.Network(
        uri = url,
        fallbackIconRes = defaultIconRes,
    )
}

/**
 * Transforms a [CipherListView] into a [VaultState.ViewState.VaultItem].
 */
@Suppress("MagicNumber", "LongMethod", "CyclomaticComplexMethod")
private fun CipherListView.toVaultItemOrNull(
    hasMasterPassword: Boolean,
    isIconLoadingDisabled: Boolean,
    baseIconUrl: String,
    isPremiumUser: Boolean,
    hasDecryptionError: Boolean,
): VaultState.ViewState.VaultItem? {
    val id = this.id ?: return null
    return when (type) {
        is CipherListViewType.Login -> VaultState.ViewState.VaultItem.Login(
            id = id,
            name = if (hasDecryptionError) {
                BitwardenString.error_cannot_decrypt.asText()
            } else {
                name.asText()
            },
            username = login?.username?.asText(),
            startIcon = login?.uris.toLoginIconData(
                isIconLoadingDisabled = isIconLoadingDisabled,
                baseIconUrl = baseIconUrl,
                usePasskeyDefaultIcon = false,
            ),
            overflowOptions = if (hasDecryptionError) {
                persistentListOf()
            } else {
                toOverflowActions(
                    hasMasterPassword = hasMasterPassword,
                    isPremiumUser = isPremiumUser,
                )
            },
            extraIconList = toLabelIcons(),
            shouldShowMasterPasswordReprompt = hasMasterPassword &&
                reprompt == CipherRepromptType.PASSWORD,
            hasDecryptionError = hasDecryptionError,
        )

        CipherListViewType.SecureNote -> VaultState.ViewState.VaultItem.SecureNote(
            id = id,
            name = name.asText(),
            overflowOptions = toOverflowActions(
                hasMasterPassword = hasMasterPassword,
                isPremiumUser = isPremiumUser,
            ),
            extraIconList = toLabelIcons(),
            shouldShowMasterPasswordReprompt = hasMasterPassword &&
                reprompt == CipherRepromptType.PASSWORD,
            hasDecryptionError = hasDecryptionError,
        )

        is CipherListViewType.Card -> VaultState.ViewState.VaultItem.Card(
            id = id,
            name = name.asText(),
            brand = card?.brand?.findVaultCardBrandWithNameOrNull(),
            lastFourDigits = subtitle.asText(),
            overflowOptions = toOverflowActions(
                hasMasterPassword = hasMasterPassword,
                isPremiumUser = isPremiumUser,
            ),
            extraIconList = toLabelIcons(),
            shouldShowMasterPasswordReprompt = hasMasterPassword &&
                reprompt == CipherRepromptType.PASSWORD,
            hasDecryptionError = hasDecryptionError,
        )

        CipherListViewType.Identity -> VaultState.ViewState.VaultItem.Identity(
            id = id,
            name = name.asText(),
            fullName = subtitle.asText(),
            overflowOptions = toOverflowActions(
                hasMasterPassword = hasMasterPassword,
                isPremiumUser = isPremiumUser,
            ),
            extraIconList = toLabelIcons(),
            shouldShowMasterPasswordReprompt = hasMasterPassword &&
                reprompt == CipherRepromptType.PASSWORD,
            hasDecryptionError = hasDecryptionError,
        )

        CipherListViewType.SshKey -> VaultState.ViewState.VaultItem.SshKey(
            id = id,
            name = name.asText(),
            extraIconList = toLabelIcons(),
            overflowOptions = toOverflowActions(
                hasMasterPassword = hasMasterPassword,
                isPremiumUser = isPremiumUser,
            ),
            shouldShowMasterPasswordReprompt = hasMasterPassword &&
                reprompt == CipherRepromptType.PASSWORD,
            hasDecryptionError = hasDecryptionError,
        )

        else -> null
    }
}

/**
 * Filters out all [CipherListView]s that are not part of the given [VaultFilterType].
 */
@JvmName("toFilteredCipherList")
fun List<CipherListView>.toFilteredList(
    vaultFilterType: VaultFilterType,
): List<CipherListView> =
    this
        // Filter out any items with invalid IDs in the unlikely case they exist
        .filterNot { it.id.isNullOrBlank() }
        .filter {
            when (vaultFilterType) {
                VaultFilterType.AllVaults -> true
                VaultFilterType.MyVault -> it.organizationId == null
                is VaultFilterType.OrganizationVault -> {
                    it.organizationId == vaultFilterType.organizationId
                }
            }
        }

/**
 * Filters out all [FolderView]s that are not part of the given [VaultFilterType].
 */
@JvmName("toFilteredFolderList")
fun List<FolderView>.toFilteredList(
    cipherList: List<CipherListView>,
    vaultFilterType: VaultFilterType,
): List<FolderView> =
    this
        .filter { folder ->
            when (vaultFilterType) {
                VaultFilterType.AllVaults,
                VaultFilterType.MyVault,
                    -> true

                // Only include folders containing an item associated with this organization.
                is VaultFilterType.OrganizationVault -> {
                    cipherList.any { it.folderId == folder.id }
                }
            }
        }

/**
 * Filters out all [CollectionView]s that are not part of the given [VaultFilterType].
 */
@JvmName("toFilteredCollectionList")
fun List<CollectionView>.toFilteredList(
    vaultFilterType: VaultFilterType,
): List<CollectionView> =
    this
        .filter {
            when (vaultFilterType) {
                VaultFilterType.AllVaults -> true
                VaultFilterType.MyVault -> false
                is VaultFilterType.OrganizationVault -> {
                    it.organizationId == vaultFilterType.organizationId
                }
            }
        }

/**
 * Filters out [CipherType.CARD] [CipherListView]s that are in [restrictItemTypesPolicyOrgIds] list.
 * When [restrictItemTypesPolicyOrgIds] is not empty, individual vault items are also removed.
 */
fun List<CipherListView>.applyRestrictItemTypesPolicy(
    restrictItemTypesPolicyOrgIds: List<String>,
): List<CipherListView> =
    this
        .filterNot { cipherListView ->
            if (restrictItemTypesPolicyOrgIds.isEmpty()) {
                // No policy, so don't apply removal
                false
            } else if (cipherListView.type !is CipherListViewType.Card) {
                // Policy only for cards
                false
            } else {
                // If a policy is enable for a given organization then
                // also hide cards from individual vault
                cipherListView.organizationId.isNullOrEmpty() ||
                    restrictItemTypesPolicyOrgIds.contains(cipherListView.organizationId)
            }
        }

private fun List<CipherListView>.applyFilters(
    vaultFilterType: VaultFilterType,
    restrictItemTypesPolicyOrgIds: List<String>,
    excludeArchived: Boolean,
    excludeDeleted: Boolean,
): List<CipherListView> = this
    .filter {
        it.type !is CipherListViewType.BankAccount &&
            it.type !is CipherListViewType.Passport &&
            it.type !is CipherListViewType.DriversLicense
    }
    .let {
        if (excludeArchived) {
            it.filter { cipher -> cipher.archivedDate == null }
        } else {
            it
        }
    }
    .let {
        if (excludeDeleted) {
            it.filter { cipher -> cipher.deletedDate == null }
        } else {
            it
        }
    }
    .applyRestrictItemTypesPolicy(restrictItemTypesPolicyOrgIds)
    .toFilteredList(vaultFilterType)
