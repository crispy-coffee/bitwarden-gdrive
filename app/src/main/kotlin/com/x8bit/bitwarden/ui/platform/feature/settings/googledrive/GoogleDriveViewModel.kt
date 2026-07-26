package com.x8bit.bitwarden.ui.platform.feature.settings.googledrive

import androidx.lifecycle.viewModelScope
import com.bitwarden.core.data.manager.toast.ToastManager
import com.bitwarden.ui.platform.base.BaseViewModel
import com.bitwarden.ui.util.Text
import com.bitwarden.ui.util.asText
import com.x8bit.bitwarden.data.platform.manager.GoogleDriveAccountInfo
import com.x8bit.bitwarden.data.platform.manager.GoogleDriveManager
import com.x8bit.bitwarden.data.platform.manager.network.NetworkConnectionManager
import com.x8bit.bitwarden.data.platform.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

/**
 * View model for the google drive settings screen.
 */
@HiltViewModel
class GoogleDriveViewModel @Inject constructor(
    private val googleDriveManager: GoogleDriveManager,
    private val settingsRepository: SettingsRepository,
    private val networkConnectionManager: NetworkConnectionManager,
    private val toastManager: ToastManager
) : BaseViewModel<GoogleDriveState, GoogleDriveEvent, GoogleDriveAction>(
    initialState = GoogleDriveState(
        isSignedId = googleDriveManager.isSignedId(),
        accountInfo = googleDriveManager.getAccountInfo(),
        lastSyncTime = settingsRepository.googleDriveLastSync
    )
) {
    init {
        refresh(silent = true)
    }

    override fun handleAction(action: GoogleDriveAction) {
        when (action) {
            GoogleDriveAction.BackClick -> sendEvent(GoogleDriveEvent.NavigateBack)
            GoogleDriveAction.SignInClick -> {
                Timber.d("Sign In Clicked")
                mutableStateFlow.update { it.copy(isLoading = true, error = null) }
                sendEvent(GoogleDriveEvent.LaunchSignIn)
            }
            GoogleDriveAction.SignOutClick -> {
                Timber.d("Sign Out Clicked")
                handleSignOut()
            }
            GoogleDriveAction.RefreshClick -> {
                Timber.d("Refresh Clicked")
                refresh(isPullToRefresh = true)
            }
            GoogleDriveAction.LifecycleResumed -> {
                Timber.d("Lifecycle Resumed - refreshing connection")
                refresh(silent = true)
            }
        }
    }

    private fun handleSignOut() {
        viewModelScope.launch {
            googleDriveManager.signOut()
            settingsRepository.googleDriveLastSync = null
            settingsRepository.googleDriveAccountEmail = null
            mutableStateFlow.update {
                it.copy(
                    isSignedId = false,
                    accountInfo = null,
                    attachmentCount = 0,
                    lastSyncTime = null
                )
            }
        }
    }

    fun refresh(silent: Boolean = false, isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                if (isPullToRefresh) {
                    mutableStateFlow.update { it.copy(isRefreshing = true, error = null) }
                } else {
                    mutableStateFlow.update { it.copy(isLoading = true, error = null) }
                }
            }

            if (!networkConnectionManager.isNetworkConnected) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = if (!silent) "Offline. Please check your internet connection.".asText() else null
                    )
                }
                return@launch
            }

            val isSignedId = googleDriveManager.isSignedId()
            if (!isSignedId) {
                mutableStateFlow.update {
                    it.copy(
                        isSignedId = false,
                        accountInfo = null,
                        isLoading = false,
                        isRefreshing = false,
                        attachmentCount = 0
                    )
                }
                return@launch
            }

            // Verify connection
            val verified = googleDriveManager.verifyConnection()
            if (!verified) {
                Timber.d("Connection could not be verified")
                mutableStateFlow.update {
                    it.copy(
                        isSignedId = false,
                        isLoading = false,
                        isRefreshing = false,
                        error = if (!silent) "Connection failed. Please sign in again.".asText() else null
                    )
                }
                return@launch
            }

            val accountInfo = googleDriveManager.getAccountInfo()

            // Detect account switch
            val lastEmail = settingsRepository.googleDriveAccountEmail
            if (lastEmail != null && accountInfo?.email != null && lastEmail != accountInfo.email) {
                Timber.d("Account switch detected from %s to %s", lastEmail, accountInfo.email)
                settingsRepository.googleDriveLastSync = null
            }
            settingsRepository.googleDriveAccountEmail = accountInfo?.email

            val files = googleDriveManager.listFiles()
            val syncTime = Instant.now()
            settingsRepository.googleDriveLastSync = syncTime

            mutableStateFlow.update {
                it.copy(
                    isSignedId = true,
                    accountInfo = accountInfo,
                    attachmentCount = files.size,
                    lastSyncTime = syncTime,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            }
        }
    }

    fun onSignInResult(success: Boolean) {
        if (success) {
            refresh()
        } else {
            toastManager.show("Google Sign-In failed or was cancelled.")
            mutableStateFlow.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    fun onSignInError(statusCode: Int) {
        Timber.e("Google Sign-In error: %d", statusCode)
        toastManager.show("Google Sign-In failed with status code: $statusCode")
        mutableStateFlow.update { it.copy(isLoading = false, isRefreshing = false) }
    }
}

data class GoogleDriveState(
    val isSignedId: Boolean,
    val accountInfo: GoogleDriveAccountInfo? = null,
    val attachmentCount: Int = 0,
    val lastSyncTime: Instant? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: Text? = null
)

sealed class GoogleDriveEvent {
    data object NavigateBack : GoogleDriveEvent()
    data object LaunchSignIn : GoogleDriveEvent()
}

sealed class GoogleDriveAction {
    data object BackClick : GoogleDriveAction()
    data object SignInClick : GoogleDriveAction()
    data object SignOutClick : GoogleDriveAction()
    data object RefreshClick : GoogleDriveAction()
    data object LifecycleResumed : GoogleDriveAction()
}
