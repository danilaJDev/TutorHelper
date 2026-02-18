package by.dreb.tutorhelper.data.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class BillingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        const val MONTHLY_SUBSCRIPTION_ID = "tutorhelper_monthly_099"
    }

    private val _subscriptionActive = MutableStateFlow(false)
    val subscriptionActive: StateFlow<Boolean> = _subscriptionActive.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val billingScope = CoroutineScope(Dispatchers.IO)

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .setListener(this)
        .build()

    init {
        billingScope.launch {
            connectIfNeeded()
            refreshSubscriptionStatus()
            loadProductDetails()
        }
    }

    suspend fun refreshSubscriptionStatus() {
        _isLoading.value = true
        val isReady = connectIfNeeded()
        if (!isReady) {
            _isLoading.value = false
            _subscriptionActive.value = false
            return
        }

        val queryResult = querySubscriptions()
        _subscriptionActive.value = queryResult.any { purchase -> purchase.isValidSubscription() }
        _isLoading.value = false
    }

    suspend fun loadProductDetails() {
        val isReady = connectIfNeeded()
        if (!isReady) return

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(MONTHLY_SUBSCRIPTION_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val result = suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, detailsList ->
                continuation.resume(billingResult to detailsList)
            }
        }

        if (result.first.responseCode == BillingResponseCode.OK) {
            _productDetails.value = result.second.firstOrNull()
            _errorMessage.value = null
        } else {
            _errorMessage.value = result.first.debugMessage
        }
    }

    fun launchSubscriptionPurchase(activity: Activity): Boolean {
        val details = _productDetails.value ?: return false
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return false

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        val launchResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        return launchResult.responseCode == BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                purchases.orEmpty().forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                _errorMessage.value = null
            }
            else -> {
                _errorMessage.value = billingResult.debugMessage
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (!purchase.isValidSubscription()) return

        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { acknowledgeResult ->
                if (acknowledgeResult.responseCode == BillingResponseCode.OK) {
                    _subscriptionActive.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = acknowledgeResult.debugMessage
                }
            }
        } else {
            _subscriptionActive.value = true
            _errorMessage.value = null
        }
    }

    private fun Purchase.isValidSubscription(): Boolean {
        val validProduct = products.contains(MONTHLY_SUBSCRIPTION_ID)
        return validProduct && purchaseState == Purchase.PurchaseState.PURCHASED
    }

    private suspend fun querySubscriptions(): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val result = suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    continuation.resume(purchasesList)
                } else {
                    _errorMessage.value = billingResult.debugMessage
                    continuation.resume(emptyList())
                }
            }
        }

        result.forEach { purchase ->
            if (!purchase.isAcknowledged) {
                handlePurchase(purchase)
            }
        }

        return result
    }

    private suspend fun connectIfNeeded(): Boolean {
        if (billingClient.isReady) return true

        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        continuation.resume(true)
                    } else {
                        _errorMessage.value = billingResult.debugMessage
                        continuation.resume(false)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _errorMessage.value = "Billing service disconnected"
                }
            })
        }
    }
}
