package by.dreb.tutorhelper.presentation.monetization

data class MonetizationUiState(
    val isLoading: Boolean = true,
    val isTrialActive: Boolean = true,
    val trialDaysLeft: Int = 14,
    val hasActiveSubscription: Boolean = false,
    val shouldShowPaywall: Boolean = false,
    val priceText: String = "$0.99 / month",
    val errorMessage: String? = null
)
