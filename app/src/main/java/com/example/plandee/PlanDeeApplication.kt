package com.example.plandee

import android.app.Application
import android.util.Log
import com.example.plandee.data.monetization.RewardedAdManager
import com.example.plandee.data.security.SessionManager
import com.google.android.gms.ads.MobileAds
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

class PlanDeeApplication : Application() {

    companion object {
        private const val TAG = "PlanDeeApplication"
        private const val REVENUECAT_PUBLIC_API_KEY = "goog_sample_public_sdk_key"
    }

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Encrypted Session Manager
        SessionManager.getInstance(this)

        // 2. Initialize AdMob Mobile Ads SDK & Preload Rewarded Ads
        try {
            MobileAds.initialize(this) {
                RewardedAdManager.getInstance(this).loadAd()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds", e)
        }

        // 3. Initialize RevenueCat Purchases SDK
        try {
            Purchases.configure(
                PurchasesConfiguration.Builder(this, REVENUECAT_PUBLIC_API_KEY).build()
            )
            Log.d(TAG, "RevenueCat Purchases SDK configured successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure RevenueCat Purchases SDK", e)
        }
    }
}
