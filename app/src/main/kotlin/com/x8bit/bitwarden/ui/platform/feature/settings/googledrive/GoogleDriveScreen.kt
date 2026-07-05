package com.x8bit.bitwarden.ui.platform.feature.settings.googledrive

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitwarden.ui.platform.base.util.EventsEffect
import com.bitwarden.ui.platform.base.util.standardHorizontalMargin
import com.bitwarden.ui.platform.components.appbar.BitwardenTopAppBar
import com.bitwarden.ui.platform.components.appbar.NavigationIcon
import com.bitwarden.ui.platform.components.button.BitwardenFilledButton
import com.bitwarden.ui.platform.components.button.BitwardenOutlinedButton
import com.bitwarden.ui.platform.components.scaffold.BitwardenScaffold
import com.bitwarden.ui.platform.components.util.rememberVectorPainter
import com.bitwarden.ui.platform.resource.BitwardenDrawable
import com.bitwarden.ui.platform.resource.BitwardenString
import com.bitwarden.ui.platform.theme.BitwardenTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.services.drive.DriveScopes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoogleDriveViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA))
        .build()
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            viewModel.onSignInResult(true)
        } catch (e: com.google.android.gms.common.api.ApiException) {
            viewModel.onSignInError(e.statusCode)
        } catch (e: Exception) {
            viewModel.onSignInResult(false)
        }
    }

    EventsEffect(viewModel = viewModel) { event ->
        when (event) {
            GoogleDriveEvent.NavigateBack -> onNavigateBack()
            GoogleDriveEvent.LaunchSignIn -> {
                startForResult.launch(googleSignInClient.signInIntent)
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    BitwardenScaffold(
        topBar = {
            BitwardenTopAppBar(
                title = stringResource(id = BitwardenString.google_drive),
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = rememberVectorPainter(id = BitwardenDrawable.ic_back),
                    navigationIconContentDescription = stringResource(id = BitwardenString.back),
                    onNavigationIconClick = { viewModel.trySendAction(GoogleDriveAction.BackClick) }
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = rememberVectorPainter(id = BitwardenDrawable.ic_globe),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = BitwardenTheme.colorScheme.icon.secondary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Google Drive Sync",
                style = BitwardenTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BitwardenTheme.colorScheme.text.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sync your vault attachments with your personal Google Drive app data folder.",
                style = BitwardenTheme.typography.bodyMedium,
                color = BitwardenTheme.colorScheme.text.secondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(
                color = BitwardenTheme.colorScheme.stroke.divider
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (state.isSignedId) {
                Text(
                    text = "Connected",
                    style = BitwardenTheme.typography.bodyLarge,
                    color = BitwardenTheme.colorScheme.status.good
                )
                Spacer(modifier = Modifier.height(16.dp))
                BitwardenOutlinedButton(
                    label = "Sign Out",
                    onClick = { viewModel.trySendAction(GoogleDriveAction.SignOutClick) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Not Connected",
                    style = BitwardenTheme.typography.bodyLarge,
                    color = BitwardenTheme.colorScheme.text.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                BitwardenFilledButton(
                    label = "Sign In with Google",
                    onClick = { viewModel.trySendAction(GoogleDriveAction.SignInClick) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
