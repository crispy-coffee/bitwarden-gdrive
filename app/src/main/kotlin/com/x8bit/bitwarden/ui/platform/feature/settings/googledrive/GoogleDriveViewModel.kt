package com.x8bit.bitwarden.ui.platform.feature.settings.googledrive

import androidx.lifecycle.viewModelScope
import com.bitwarden.core.data.manager.toast.ToastManager
import com.bitwarden.ui.platform.base.BaseViewModel
import com.x8bit.bitwarden.data.platform.manager.GoogleDriveManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model for the google drive settings screen.
 */
@HiltViewModel
class GoogleDriveViewModel @Inject constructor(
    private val googleDriveManager: GoogleDriveManager,
    private val toastManager: ToastManager
) : BaseViewModel<GoogleDriveState, GoogleDriveEvent, GoogleDriveAction>(
    initialState = GoogleDriveState(
        isSignedId = googleDriveManager.isSignedId()
    )
) {
    override fun handleAction(action: GoogleDriveAction) {
        when (action) {
            GoogleDriveAction.BackClick -> sendEvent(GoogleDriveEvent.NavigateBack)
            GoogleDriveAction.SignInClick -> sendEvent(GoogleDriveEvent.LaunchSignIn)
            GoogleDriveAction.SignOutClick -> {
                viewModelScope.launch {
                    googleDriveManager.signOut()
                    mutableStateFlow.update { it.copy(isSignedId = false) }
                }
            }
        }
    }

    fun onSignInResult(success: Boolean) {
        val isSignedId = googleDriveManager.isSignedId()
        if (success && !isSignedId) {
            toastManager.show("Google Drive permission was not granted. Please try again and ensure the permission is checked.")
        }
        mutableStateFlow.update { it.copy(isSignedId = isSignedId) }
    }

    fun onSignInError(statusCode: Int) {
        toastManager.show("Google Sign-In failed with status code: $statusCode")
    }
}

data class GoogleDriveState(
    val isSignedId: Boolean
)

sealed class GoogleDriveEvent {
    data object NavigateBack : GoogleDriveEvent()
    data object LaunchSignIn : GoogleDriveEvent()
}

sealed class GoogleDriveAction {
    data object BackClick : GoogleDriveAction()
    data object SignInClick : GoogleDriveAction()
    data object SignOutClick : GoogleDriveAction()
}
