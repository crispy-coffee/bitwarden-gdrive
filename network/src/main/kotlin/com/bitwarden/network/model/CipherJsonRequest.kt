package com.bitwarden.network.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Represents a cipher request.
 *
 * @property notes The notes of the cipher (nullable).
 * @property reprompt The reprompt of the cipher.
 * @property passwordHistory A list of password history objects
 * associated with the cipher (nullable).
 * @property type The type of cipher.
 * @property login The login of the cipher.
 * @property secureNote The secure note of the cipher.
 * @property folderId The folder ID of the cipher (nullable).
 * @property organizationId The organization ID of the cipher (nullable).
 * @property identity The identity of the cipher.
 * @property name The name of the cipher (nullable).
 * @property fields A list of fields associated with the cipher (nullable).
 * @property isFavorite If the cipher is a favorite.
 * @property card The card of the cipher.
 * @property key The key of the cipher (nullable).
 * @property archivedDate The archived date of the cipher (nullable).
 * @property encryptedFor ID of the user who the cipher is encrypted by.
 */
@Serializable
data class CipherJsonRequest(
    @SerialName("notes")
    val notes: String? = null,

    @SerialName("attachments2")
    val attachments: Map<String, AttachmentJsonRequest>? = null,

    @SerialName("reprompt")
    val reprompt: CipherRepromptTypeJson,

    @SerialName("passwordHistory")
    val passwordHistory: List<SyncResponseJson.Cipher.PasswordHistory>? = null,

    @SerialName("lastKnownRevisionDate")
    @Contextual
    val lastKnownRevisionDate: Instant? = null,

    @SerialName("type")
    val type: CipherTypeJson,

    @SerialName("login")
    val login: SyncResponseJson.Cipher.Login? = null,

    @SerialName("secureNote")
    val secureNote: SyncResponseJson.Cipher.SecureNote? = null,

    @SerialName("sshKey")
    val sshKey: SyncResponseJson.Cipher.SshKey? = null,

    @SerialName("bankAccount")
    val bankAccount: SyncResponseJson.Cipher.BankAccount? = null,

    @SerialName("driversLicense")
    val driversLicense: SyncResponseJson.Cipher.DriversLicense? = null,

    @SerialName("passport")
    val passport: SyncResponseJson.Cipher.Passport? = null,

    @SerialName("folderId")
    val folderId: String? = null,

    @SerialName("organizationId")
    val organizationId: String? = null,

    @SerialName("identity")
    val identity: SyncResponseJson.Cipher.Identity? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("fields")
    val fields: List<SyncResponseJson.Cipher.Field>? = null,

    @SerialName("favorite")
    val isFavorite: Boolean,

    @SerialName("card")
    val card: SyncResponseJson.Cipher.Card? = null,

    @SerialName("key")
    val key: String? = null,

    @SerialName("archivedDate")
    @Contextual
    val archivedDate: Instant? = null,

    @SerialName("encryptedFor")
    val encryptedFor: String? = null,
)
