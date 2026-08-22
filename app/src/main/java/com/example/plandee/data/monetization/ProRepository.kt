package com.example.plandee.data.monetization

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.plandee.data.security.SessionManager
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.restorePurchasesWith
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.purchaseWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProRepository private constructor(private val context: Context) {

    private val sessionManager = SessionManager.getInstance(context)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isProState = MutableStateFlow(false)
    val isProState: StateFlow<Boolean> = _isProState.asStateFlow()

    private val _offeringsState = MutableStateFlow<Offerings?>(null)
    val offeringsState: StateFlow<Offerings?> = _offeringsState.asStateFlow()

    companion object {
        private const val TAG = "PlanDee_RevenueCat"
        const val ENTITLEMENT_PLAN_DEE_PRO = "PlanDee Pro"
        const val ENTITLEMENT_DEE_PRO = "dee_pro"

        // How long to wait before retrying init if Purchases.configure()
        // hasn't run yet, and how many times to retry before giving up.
        private const val INIT_RETRY_DELAY_MS = 1000L
        private const val INIT_MAX_RETRIES = 5

        @Volatile
        private var INSTANCE: ProRepository? = null

        fun getInstance(context: Context): ProRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        attemptInitialLoad(retryCount = 0)
    }

    /**
     * Guards against the race where ProRepository.getInstance() is called
     * (e.g. from a ViewModel or Composable) before Purchases.configure()
     * has run in Application.onCreate(). Instead of silently giving up,
     * this retries on a short delay until the SDK reports configured,
     * or until INIT_MAX_RETRIES is exhausted.
     */
    private fun attemptInitialLoad(retryCount: Int) {
        if (Purchases.isConfigured) {
            Log.i(TAG, "✅ Purchases is configured, loading entitlements + offerings.")
            checkRevenueCatEntitlements()
            fetchOfferings()
            return
        }

        if (retryCount >= INIT_MAX_RETRIES) {
            Log.e(
                TAG,
                "🚨 Purchases still not configured after $INIT_MAX_RETRIES retries. " +
                        "Check that Purchases.configure() runs in Application.onCreate() " +
                        "before ProRepository.getInstance() is first called."
            )
            return
        }

        Log.w(
            TAG,
            "⚠️ Purchases not yet configured (attempt ${retryCount + 1}/$INIT_MAX_RETRIES). " +
                    "Retrying in ${INIT_RETRY_DELAY_MS}ms..."
        )
        mainHandler.postDelayed({
            attemptInitialLoad(retryCount + 1)
        }, INIT_RETRY_DELAY_MS)
    }

    fun loginUserToRevenueCat(userId: String = sessionManager.getUserId()) {
        try {
            Purchases.sharedInstance.logInWith(
                userId,
                onError = { error -> Log.e(TAG, "RevenueCat logIn error: ${error.message}") },
                onSuccess = { customerInfo, _ ->
                    Log.i(TAG, "User logged in to RevenueCat: $userId")
                    updateProEntitlementStatus(customerInfo)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "RevenueCat not configured yet on login attempt", e)
        }
    }

    fun fetchOfferings() {
        try {
            Log.i(TAG, "🔄 Initiating RevenueCat fetchOfferings()...")

            Purchases.sharedInstance.getOfferingsWith(
                onError = { error ->
                    Log.e(TAG, "Failed to fetch offerings: [Code: ${error.code}] - ${error.message}")
                    Log.e(TAG, "Underlying error details: ${error.underlyingErrorMessage}")
                },
                onSuccess = { offerings ->
                    _offeringsState.value = offerings

                    val current = offerings.current
                    if (current == null) {
                        Log.w(TAG, "⚠️ Offerings loaded successfully, but NO 'current' default offering is set in RevenueCat Dashboard!")
                        return@getOfferingsWith
                    }

                    val packages = current.availablePackages
                    Log.i(TAG, "✅ Current Offering Loaded: '${current.identifier}' with ${packages.size} packages.")

                    packages.forEachIndexed { index, pkg ->
                        Log.i(
                            TAG,
                            "📦 Package #$index | Identifier: '${pkg.identifier}' | Type: ${pkg.packageType} | " +
                                    "Product ID: '${pkg.product.id}' | Price: ${pkg.product.price.formatted}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "🚨 Unexpected runtime exception during getOfferings", e)
        }
    }

    fun checkRevenueCatEntitlements() {
        try {
            Purchases.sharedInstance.getCustomerInfoWith(
                onError = { error -> Log.e(TAG, "❌ RevenueCat customer info error: ${error.message}") },
                onSuccess = { customerInfo ->
                    updateProEntitlementStatus(customerInfo)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "🚨 RevenueCat not configured yet on entitlement check", e)
            _isProState.value = false
        }
    }

    fun restorePurchases(onSuccess: (String) -> Unit = {}) {
        try {
            Purchases.sharedInstance.restorePurchasesWith(
                onError = { error ->
                    showToast("Restore failed: ${error.message}")
                },
                onSuccess = { customerInfo ->
                    updateProEntitlementStatus(customerInfo)
                    if (isProState.value) {
                        val appUserId = try { Purchases.sharedInstance.appUserID } catch (e: Exception) { customerInfo.originalAppUserId }
                        onSuccess(appUserId)
                        showToast("PlanDee Pro restored successfully!")
                    } else {
                        showToast("No active PlanDee Pro subscription found.")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "🚨 Restore purchases failed", e)
            showToast("RevenueCat SDK Error: ${e.localizedMessage}")
        }
    }

    fun purchasePackage(activity: Activity, rcPackage: Package, onPurchaseSuccess: (String) -> Unit = {}) {
        try {
            Purchases.sharedInstance.purchaseWith(
                PurchaseParams.Builder(activity, rcPackage).build(),
                onError = { error, userCancelled ->
                    if (!userCancelled) {
                        showToast("Purchase failed: ${error.message}")
                    }
                },
                onSuccess = { _, customerInfo ->
                    updateProEntitlementStatus(customerInfo)
                    if (isProState.value) {
                        val appUserId = try { Purchases.sharedInstance.appUserID } catch (e: Exception) { customerInfo.originalAppUserId }
                        onPurchaseSuccess(appUserId)
                        showToast("Welcome to PlanDee Pro!")
                    } else {
                        showToast("Transaction completed, but Pro entitlement is inactive.")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "🚨 RevenueCat purchase error", e)
            showToast("RevenueCat SDK Error: ${e.localizedMessage}")
        }
    }

    fun purchaseProDefault(activity: Activity, onPurchaseSuccess: (String) -> Unit = {}) {
        val currentOfferings = _offeringsState.value

        // If offerings haven't loaded yet (e.g. init retry is still in
        // progress), try one synchronous fetch instead of immediately
        // failing, then proceed with whatever comes back.
        if (currentOfferings == null) {
            Log.w(TAG, "⚠️ purchaseProDefault called before offerings loaded — fetching now.")
            fetchOfferings()
        }

        val pkg = _offeringsState.value?.current?.monthly
            ?: _offeringsState.value?.current?.annual
            ?: _offeringsState.value?.current?.lifetime

        if (pkg != null) {
            purchasePackage(activity, pkg, onPurchaseSuccess)
        } else {
            showToast("RevenueCat Error: No active offerings configured.")
        }
    }

    private fun updateProEntitlementStatus(customerInfo: CustomerInfo) {
        val planDeeProActive = customerInfo.entitlements[ENTITLEMENT_PLAN_DEE_PRO]?.isActive == true
        val deeProActive = customerInfo.entitlements[ENTITLEMENT_DEE_PRO]?.isActive == true

        val isPro = planDeeProActive || deeProActive
        Log.i(TAG, "🔒 Entitlement Status Updated -> Is Pro: $isPro (PlanDee Pro: $planDeeProActive, dee_pro: $deeProActive)")
        _isProState.value = isPro
    }

    fun setProStatus(isPro: Boolean) {
        _isProState.value = isPro
    }

    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}