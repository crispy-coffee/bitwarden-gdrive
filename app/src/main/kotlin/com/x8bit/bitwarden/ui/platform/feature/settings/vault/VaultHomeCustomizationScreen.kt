package com.x8bit.bitwarden.ui.platform.feature.settings.vault

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitwarden.ui.platform.base.util.EventsEffect
import com.bitwarden.ui.platform.base.util.standardHorizontalMargin
import com.bitwarden.ui.platform.components.appbar.BitwardenTopAppBar
import com.bitwarden.ui.platform.components.header.BitwardenListHeaderText
import com.bitwarden.ui.platform.components.model.CardStyle
import com.bitwarden.ui.platform.components.scaffold.BitwardenScaffold
import com.bitwarden.ui.platform.components.toggle.BitwardenSwitch
import com.bitwarden.ui.platform.components.util.rememberVectorPainter
import com.bitwarden.ui.platform.resource.BitwardenDrawable
import com.bitwarden.ui.platform.resource.BitwardenString
import com.x8bit.bitwarden.ui.vault.model.VaultItemCipherType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(
    onNavigateBack: () -> Unit,
    viewModel: CustomizationViewModel = hiltViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel = viewModel) { event ->
        when (event) {
            CustomizationEvent.NavigateBack -> onNavigateBack()
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    BitwardenScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BitwardenTopAppBar(
                title = "Customization",
                scrollBehavior = scrollBehavior,
                navigationIcon = rememberVectorPainter(id = BitwardenDrawable.ic_back),
                navigationIconContentDescription = stringResource(id = BitwardenString.back),
                onNavigationIconClick = { viewModel.trySendAction(CustomizationAction.BackClick) },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            BitwardenListHeaderText(
                label = "Navigation",
                modifier = Modifier
                    .standardHorizontalMargin()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            BitwardenSwitch(
                label = "Show Generator",
                isChecked = !state.hiddenSections.contains("GENERATOR"),
                onCheckedChange = { viewModel.trySendAction(CustomizationAction.ToggleSection("GENERATOR", it)) },
                cardStyle = CardStyle.Top(dividerPadding = 0.dp),
                modifier = Modifier.standardHorizontalMargin()
            )
            BitwardenSwitch(
                label = "Show Send",
                isChecked = !state.hiddenSections.contains("SEND"),
                onCheckedChange = { viewModel.trySendAction(CustomizationAction.ToggleSection("SEND", it)) },
                cardStyle = CardStyle.Bottom,
                modifier = Modifier.standardHorizontalMargin()
            )

            Spacer(modifier = Modifier.height(24.dp))

            BitwardenListHeaderText(
                label = "Vault Types",
                modifier = Modifier
                    .standardHorizontalMargin()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val types = VaultItemCipherType.entries.filter {
                it != VaultItemCipherType.BANK_ACCOUNT &&
                    it != VaultItemCipherType.DRIVERS_LICENSE &&
                    it != VaultItemCipherType.PASSPORT
            }
            types.forEachIndexed { index, type ->
                val label = when(type) {
                    VaultItemCipherType.LOGIN -> stringResource(BitwardenString.type_login)
                    VaultItemCipherType.CARD -> stringResource(BitwardenString.type_card)
                    VaultItemCipherType.IDENTITY -> stringResource(BitwardenString.type_identity)
                    VaultItemCipherType.SECURE_NOTE -> stringResource(BitwardenString.type_secure_note)
                    VaultItemCipherType.SSH_KEY -> stringResource(BitwardenString.type_ssh_key)
                    else -> ""
                }
                val cardStyle = if (index == 0) CardStyle.Top(dividerPadding = 0.dp)
                                else if (index == types.lastIndex) CardStyle.Bottom
                                else CardStyle.Middle(dividerPadding = 0.dp)

                if (label.isNotEmpty()) {
                    BitwardenSwitch(
                        label = label,
                        isChecked = !state.hiddenTypes.contains(type.name),
                        onCheckedChange = { viewModel.trySendAction(CustomizationAction.ToggleType(type.name, it)) },
                        cardStyle = cardStyle,
                        modifier = Modifier.standardHorizontalMargin()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
