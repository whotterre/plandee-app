package com.example.plandee.data.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.plandee.data.security.SessionManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager private constructor(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private val sessionManager = SessionManager.getInstance(context)

    companion object {
        private const val TAG = "RewardedAdManager"
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

        @Volatile
        private var INSTANCE: RewardedAdManager? = null

        fun getInstance(context: Context): RewardedAdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RewardedAdManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        loadAd()
    }

    fun loadAd() {
        if (isLoading || rewardedAd != null) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "AdMob Rewarded Ad successfully loaded.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.e(TAG, "AdMob Rewarded Ad failed to load: ${adError.message}")
                }
            }
        )
    }

    fun showAd(activity: Activity, onRewardEarned: (rewardAmount: Int) -> Unit) {
        val currentAd = rewardedAd
        if (currentAd != null) {
            currentAd.show(activity) { rewardItem ->
                val reward = rewardItem.amount.coerceAtLeast(1)
                sessionManager.addTokens(reward)
                Toast.makeText(context, "Ad Completed: +$reward Recommendation Token! 🪙", Toast.LENGTH_SHORT).show()
                onRewardEarned(reward)
                rewardedAd = null
                loadAd()
            }
        } else {
            Log.d(TAG, "Rewarded Ad is loading or unavailable. Triggering fallback reward.")
            val reward = 1
            sessionManager.addTokens(reward)
            Toast.makeText(context, "Ad Completed: +1 Recommendation Token Awarded! 🪙", Toast.LENGTH_SHORT).show()
            onRewardEarned(reward)
            loadAd()
        }
    }
}
