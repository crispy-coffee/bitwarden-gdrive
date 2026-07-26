package com.x8bit.bitwarden.ui.platform.feature.settings.vault

import androidx.lifecycle.viewModelScope
import com.bitwarden.ui.platform.base.BaseViewModel
import com.x8bit.bitwarden.data.platform.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CustomizationViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel<CustomizationState, CustomizationEvent, CustomizationAction>(
    initialState = CustomizationState(
        hiddenTypes = settingsRepository.hiddenVaultItemTypes,
        hiddenSections = settingsRepository.hiddenVaultHomeSections,
    ),
) {
    init {
        settingsRepository.hiddenVaultItemTypesFlow
            .onEach { types ->
                mutableStateFlow.update { it.copy(hiddenTypes = types) }
            }
            .launchIn(viewModelScope)

        settingsRepository.hiddenVaultHomeSectionsFlow
            .onEach { sections ->
                mutableStateFlow.update { it.copy(hiddenSections = sections) }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: CustomizationAction) {
        when (action) {
            CustomizationAction.BackClick -> sendEvent(CustomizationEvent.NavigateBack)
            is CustomizationAction.ToggleType -> {
                val current = settingsRepository.hiddenVaultItemTypes.toMutableSet()
                if (action.visible) {
                    current.remove(action.type)
                } else {
                    current.add(action.type)
                }
                settingsRepository.hiddenVaultItemTypes = current
            }
            is CustomizationAction.ToggleSection -> {
                val current = settingsRepository.hiddenVaultHomeSections.toMutableSet()
                if (action.section == "MY_VAULT") return // Safety check, should not happen

                if (action.visible) {
                    current.remove(action.section)
                } else {
                    current.add(action.section)
                }
                settingsRepository.hiddenVaultHomeSections = current
            }
        }
    }
}

data class CustomizationState(
    val hiddenTypes: Set<String> = emptySet(),
    val hiddenSections: Set<String> = emptySet(),
)

sealed class CustomizationEvent {
    data object NavigateBack : CustomizationEvent()
}

sealed class CustomizationAction {
    data object BackClick : CustomizationAction()
    data class ToggleType(val type: String, val visible: Boolean) : CustomizationAction()
    data class ToggleSection(val section: String, val visible: Boolean) : CustomizationAction()
}
