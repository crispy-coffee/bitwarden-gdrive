package com.x8bit.bitwarden.data.platform.manager

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA))
        .build()

    private fun getDriveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: run {
            Timber.d("No Google account signed in")
            return null
        }

        if (!GoogleSignIn.hasPermissions(account, com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA))) {
            Timber.d("Missing Drive AppData permissions")
            return null
        }

        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Bitwarden")
            .build()
    }

    fun isSignedId(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && GoogleSignIn.hasPermissions(
            account,
            com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA)
        )
    }

    fun getAccountInfo(): GoogleDriveAccountInfo? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return GoogleDriveAccountInfo(
            displayName = account.displayName,
            email = account.email,
            photoUrl = account.photoUrl?.toString()
        )
    }

    suspend fun verifyConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext false
            // Fetch user info to verify token
            service.about().get().setFields("user").execute()
            Timber.d("Google Drive connection verified")
            true
        } catch (e: Exception) {
            Timber.e(e, "Google Drive connection verification failed")
            false
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        Timber.d("Signing out from Google Drive")
        GoogleSignIn.getClient(context, gso).signOut()
    }

    suspend fun listFiles(): List<com.google.api.services.drive.model.File> = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext emptyList()
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name, size, modifiedTime)")
                .execute()
            val files = result.files ?: emptyList()
            Timber.d("Listed %d files from Google Drive AppData", files.size)
            files
        } catch (e: Exception) {
            Timber.e(e, "Google Drive list files failed")
            emptyList()
        }
    }

    suspend fun uploadFile(file: File, fileName: String): String? = withContext(Dispatchers.IO) {
        Timber.d("Uploading file to Google Drive: %s", fileName)
        try {
            val service = getDriveService() ?: return@withContext null

            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = fileName
                parents = Collections.singletonList("appDataFolder")
            }
            val mediaContent = FileContent("application/octet-stream", file)

            val driveFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            Timber.d("Upload successful. Drive File ID: %s", driveFile.id)
            driveFile.id
        } catch (e: Exception) {
            Timber.e(e, "Google Drive upload failed")
            null
        }
    }

    suspend fun downloadFile(fileId: String, targetFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        Timber.d("Downloading file from Google Drive: %s", fileId)
        try {
            val service = getDriveService() ?: return@withContext Result.failure(IllegalStateException("Google Drive service not available"))
            targetFile.outputStream().use { outputStream ->
                service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }
            Timber.d("Download successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Google Drive download failed for fileId: $fileId")
            Result.failure(e)
        }
    }

    suspend fun deleteFile(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        Timber.d("Deleting file from Google Drive: %s", fileId)
        try {
            val service = getDriveService() ?: return@withContext Result.failure(IllegalStateException("Google Drive service not available"))
            service.files().delete(fileId).execute()
            Timber.d("Delete successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Google Drive delete failed for fileId: $fileId")
            Result.failure(e)
        }
    }
}

data class GoogleDriveAccountInfo(
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)
