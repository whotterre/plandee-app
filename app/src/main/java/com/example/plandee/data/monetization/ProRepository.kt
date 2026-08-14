package com.example.plandee.data.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.plandee.data.security.SessionManager
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
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

    companion object {
        private const val TAG = "ProRepository"

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

    fun purchasePro(activity: Activity) {
        try {
            Purchases.sharedInstance.getOfferingsWith(
                onError = { error ->
                    Log.e(TAG, "RevenueCat getOfferings error: $error")
                    Toast.makeText(context, "RevenueCat Billing: Connect Play Console product ID 'dee_pro_monthly'", Toast.LENGTH_LONG).show()
                },
                onSuccess = { offerings ->
                    val pkg = offerings.current?.monthly
                    if (pkg != null) {
                        Purchases.sharedInstance.purchaseWith(
                            PurchaseParams.Builder(activity, pkg).build(),
                            onError = { error, userCancelled ->
                                if (!userCancelled) {
                                    Toast.makeText(context, "Purchase failed: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onSuccess = { _, customerInfo ->
                                updateProEntitlementStatus(customerInfo)
                                Toast.makeText(context, "Welcome to Plan Dee Pro! 🚀", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Play Store Billing: Configure product 'dee_pro' in RevenueCat dashboard", Toast.LENGTH_LONG).show()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Purchases not initialized", e)
            Toast.makeText(context, "RevenueCat SDK: Configure public API key in PlanDeeApplication", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateProEntitlementStatus(customerInfo: CustomerInfo) {
        val isProEntitled = customerInfo.entitlements["dee_pro"]?.isActive == true
        _isProState.value = isProEntitled
    }

    fun setProStatus(isPro: Boolean) {
        _isProState.value = isPro
    }
}
