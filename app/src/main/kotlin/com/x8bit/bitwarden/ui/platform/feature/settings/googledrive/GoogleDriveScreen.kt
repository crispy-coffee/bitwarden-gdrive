package com.x8bit.bitwarden.ui.platform.feature.settings.googledrive

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitwarden.core.data.util.toFormattedDateTimeStyle
import com.bitwarden.ui.platform.base.util.EventsEffect
import com.bitwarden.ui.platform.base.util.LifecycleEventEffect
import com.bitwarden.ui.platform.base.util.standardHorizontalMargin
import com.bitwarden.ui.platform.components.appbar.BitwardenTopAppBar
import com.bitwarden.ui.platform.components.button.BitwardenFilledButton
import com.bitwarden.ui.platform.components.button.BitwardenOutlinedButton
import com.bitwarden.ui.platform.components.content.BitwardenLoadingContent
import com.bitwarden.ui.platform.components.model.CardStyle
import com.bitwarden.ui.platform.components.row.BitwardenTextRow
import com.bitwarden.ui.platform.components.scaffold.BitwardenScaffold
import com.bitwarden.ui.platform.components.scaffold.model.rememberBitwardenPullToRefreshState
import com.bitwarden.ui.platform.components.util.rememberVectorPainter
import com.bitwarden.ui.platform.resource.BitwardenDrawable
import com.bitwarden.ui.platform.resource.BitwardenString
import com.bitwarden.ui.platform.theme.BitwardenTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.services.drive.DriveScopes
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun GoogleDriveScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoogleDriveViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
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

    LifecycleEventEffect { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            viewModel.trySendAction(GoogleDriveAction.LifecycleResumed)
        }
    }

    val pullToRefreshState = rememberBitwardenPullToRefreshState(
        isEnabled = state.isSignedId,
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refresh(isPullToRefresh = true) },
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    BitwardenScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BitwardenTopAppBar(
                title = stringResource(id = BitwardenString.google_drive),
                scrollBehavior = scrollBehavior,
                navigationIcon = rememberVectorPainter(id = BitwardenDrawable.ic_back),
                navigationIconContentDescription = stringResource(id = BitwardenString.back),
                onNavigationIconClick = { viewModel.trySendAction(GoogleDriveAction.BackClick) }
            )
        },
        pullToRefreshState = pullToRefreshState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading && !state.isSignedId) {
                BitwardenLoadingContent(modifier = Modifier.fillMaxSize())
            } else if (state.isSignedId) {
                ConnectedSection(state, viewModel)
            } else {
                NotConnectedSection(viewModel)
            }

            state.error?.let { error ->
                Text(
                    text = error(),
                    style = BitwardenTheme.typography.bodyMedium,
                    color = BitwardenTheme.colorScheme.status.error,
                    modifier = Modifier
                        .padding(16.dp)
                        .standardHorizontalMargin()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ConnectedSection(
    state: GoogleDriveState,
    viewModel: GoogleDriveViewModel
) {
    Column {
        // Connection Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .standardHorizontalMargin()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = rememberVectorPainter(id = BitwardenDrawable.ic_check_mark),
                contentDescription = null,
                tint = BitwardenTheme.colorScheme.status.good,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Connected",
                style = BitwardenTheme.typography.titleMedium,
                color = BitwardenTheme.colorScheme.status.good
            )
        }

        // Account Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .standardHorizontalMargin()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                model = state.accountInfo?.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                loading = placeholder(BitwardenDrawable.logo_bitwarden_icon),
                failure = placeholder(BitwardenDrawable.logo_bitwarden_icon)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = state.accountInfo?.displayName ?: "Google User",
                    style = BitwardenTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BitwardenTheme.colorScheme.text.primary
                )
                Text(
                    text = state.accountInfo?.email ?: "",
                    style = BitwardenTheme.typography.bodyMedium,
                    color = BitwardenTheme.colorScheme.text.secondary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 16.dp),
            color = BitwardenTheme.colorScheme.stroke.divider
        )

        // Sync Info
        BitwardenTextRow(
            text = "Stored Attachments",
            onClick = {},
            cardStyle = CardStyle.Top(),
            modifier = Modifier
                .standardHorizontalMargin()
                .fillMaxWidth()
        ) {
            Text(
                text = state.attachmentCount.toString(),
                style = BitwardenTheme.typography.bodyMedium,
                color = BitwardenTheme.colorScheme.text.secondary
            )
        }

        BitwardenTextRow(
            text = "Last Sync",
            onClick = {},
            cardStyle = CardStyle.Middle(),
            modifier = Modifier
                .standardHorizontalMargin()
                .fillMaxWidth()
        ) {
            Text(
                text = state.lastSyncTime?.toFormattedDateTimeStyle(FormatStyle.MEDIUM, FormatStyle.SHORT) ?: "Never",
                style = BitwardenTheme.typography.bodyMedium,
                color = BitwardenTheme.colorScheme.text.secondary
            )
        }

        BitwardenTextRow(
            text = "Storage",
            onClick = {},
            cardStyle = CardStyle.Bottom,
            modifier = Modifier
                .standardHorizontalMargin()
                .fillMaxWidth()
        ) {
            Text(
                text = "Google Drive AppData",
                style = BitwardenTheme.typography.bodyMedium,
                color = BitwardenTheme.colorScheme.text.secondary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        BitwardenOutlinedButton(
            label = "Sign Out",
            onClick = { viewModel.trySendAction(GoogleDriveAction.SignOutClick) },
            modifier = Modifier
                .fillMaxWidth()
                .standardHorizontalMargin()
        )
    }
}

@Composable
private fun NotConnectedSection(
    viewModel: GoogleDriveViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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

        BitwardenFilledButton(
            label = "Sign In with Google",
            onClick = { viewModel.trySendAction(GoogleDriveAction.SignInClick) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
