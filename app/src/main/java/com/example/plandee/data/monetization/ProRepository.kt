package com.example.plandee.data.monetization

import android.app.Activity
import android.content.Context
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

    private val _isProState = MutableStateFlow(false)
    val isProState: StateFlow<Boolean> = _isProState.asStateFlow()

    private val _offeringsState = MutableStateFlow<Offerings?>(null)
    val offeringsState: StateFlow<Offerings?> = _offeringsState.asStateFlow()

    companion object {
        private const val TAG = "ProRepository"
        const val ENTITLEMENT_PLAN_DEE_PRO = "PlanDee Pro"
        const val ENTITLEMENT_DEE_PRO = "dee_pro"

        @Volatile
        private var INSTANCE: ProRepository? = null

        fun getInstance(context: Context): ProRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        checkRevenueCatEntitlements()
        fetchOfferings()
    }

    fun loginUserToRevenueCat(userId: String = sessionManager.getUserId()) {
        try {
            Purchases.sharedInstance.logInWith(
                userId,
                onError = { error -> Log.e(TAG, "RevenueCat logIn error: $error") },
                onSuccess = { customerInfo, _ ->
                    updateProEntitlementStatus(customerInfo)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "RevenueCat not configured yet", e)
        }
    }

    fun fetchOfferings() {
        try {
            Purchases.sharedInstance.getOfferingsWith(
                onError = { error -> Log.e(TAG, "RevenueCat getOfferings error: $error") },
                onSuccess = { offerings ->
                    _offeringsState.value = offerings
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "RevenueCat getOfferings error", e)
        }
    }

    fun checkRevenueCatEntitlements() {
        try {
            Purchases.sharedInstance.getCustomerInfoWith(
                onError = { error -> Log.e(TAG, "RevenueCat customer info error: $error") },
                onSuccess = { customerInfo ->
                    updateProEntitlementStatus(customerInfo)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "RevenueCat not configured yet", e)
        }
    }

    fun restorePurchases() {
        try {
            Purchases.sharedInstance.restorePurchasesWith(
                onError = { error ->
                    Toast.makeText(context, "Restore failed: ${error.message}", Toast.LENGTH_SHORT).show()
                },
                onSuccess = { customerInfo ->
                    updateProEntitlementStatus(customerInfo)
                    if (isProState.value) {
                        Toast.makeText(context, "PlanDee Pro restored successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No active PlanDee Pro subscription found.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Restore purchases failed", e)
        }
    }

    fun purchasePackage(activity: Activity, rcPackage: Package) {
        try {
            Purchases.sharedInstance.purchaseWith(
                PurchaseParams.Builder(activity, rcPackage).build(),
                onError = { error, userCancelled ->
                    if (!userCancelled) {
                        Toast.makeText(context, "Purchase failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                onSuccess = { _, customerInfo ->
                    updateProEntitlementStatus(customerInfo)
                    Toast.makeText(context, "Welcome to PlanDee Pro!", Toast.LENGTH_SHORT).show()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Purchase failed", e)
            Toast.makeText(context, "Billing initialization error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun purchaseProDefault(activity: Activity) {
        val currentOfferings = _offeringsState.value
        val pkg = currentOfferings?.current?.monthly
            ?: currentOfferings?.current?.annual
            ?: currentOfferings?.current?.lifetime

        if (pkg != null) {
            purchasePackage(activity, pkg)
        } else {
            // Fallback for local testing when Play Console product is not yet active
            setProStatus(true)
            Toast.makeText(context, "PlanDee Pro activated (Test Sandbox)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProEntitlementStatus(customerInfo: CustomerInfo) {
        val planDeeProActive = customerInfo.entitlements[ENTITLEMENT_PLAN_DEE_PRO]?.isActive == true
        val deeProActive = customerInfo.entitlements[ENTITLEMENT_DEE_PRO]?.isActive == true

        val isPro = planDeeProActive || deeProActive
        _isProState.value = isPro
    }

    fun setProStatus(isPro: Boolean) {
        _isProState.value = isPro
    }
}
