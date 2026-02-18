package by.dreb.tutorhelper.presentation.monetization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.dreb.tutorhelper.data.monetization.BillingRepository
import by.dreb.tutorhelper.data.monetization.TrialPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MonetizationViewModel @Inject constructor(
    private val trialPreferences: TrialPreferences,
    private val billingRepository: BillingRepository
) : ViewModel() {

    companion object {
        private const val TRIAL_DAYS = 14L
        private const val MILLIS_IN_DAY = 24L * 60L * 60L * 1000L
    }

    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())
    private val nowMillis: StateFlow<Long> = _nowMillis.asStateFlow()

    val uiState: StateFlow<MonetizationUiState> = combine(
        trialPreferences.firstLaunchTimeMillis,
        billingRepository.subscriptionActive,
        billingRepository.isLoading,
        billingRepository.errorMessage,
        billingRepository.productDetails,
        nowMillis
    ) { firstLaunchMillis, hasSubscription, isLoading, errorMessage, productDetails, now ->
        val startedAt = firstLaunchMillis ?: now
        val elapsedMillis = (now - startedAt).coerceAtLeast(0)
        val elapsedDays = elapsedMillis / MILLIS_IN_DAY
        val trialDaysLeft = (TRIAL_DAYS - elapsedDays).toInt().coerceAtLeast(0)
        val isTrialActive = elapsedDays < TRIAL_DAYS

        val priceText = productDetails?.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.formattedPrice
            ?.let { "$it / month" }
            ?: "$0.99 / month"

        MonetizationUiState(
            isLoading = isLoading,
            isTrialActive = isTrialActive,
            trialDaysLeft = trialDaysLeft,
            hasActiveSubscription = hasSubscription,
            shouldShowPaywall = !isTrialActive && !hasSubscription,
            priceText = priceText,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MonetizationUiState()
    )

    init {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            trialPreferences.setFirstLaunchTimeIfAbsent(now)
            _nowMillis.value = now
            billingRepository.refreshSubscriptionStatus()
            billingRepository.loadProductDetails()
        }
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _nowMillis.value = System.currentTimeMillis()
            billingRepository.refreshSubscriptionStatus()
        }
    }

    fun launchPurchase(activity: android.app.Activity): Boolean {
        return billingRepository.launchSubscriptionPurchase(activity)
    }

}
