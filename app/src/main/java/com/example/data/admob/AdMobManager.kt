package com.example.data.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

object AdMobManager {
    private const val TAG = "AdMobManager"

    // Real Ad Unit IDs provided by the user
    val USER_AD_UNIT_IDS = listOf(
        "ca-app-pub-1495262574338316/2803163788", // 1
        "ca-app-pub-1495262574338316/2903409205", // 2
        "ca-app-pub-1495262574338316/2452733430", // 3
        "ca-app-pub-1495262574338316/8970040322", // 4
        "ca-app-pub-1495262574338316/1182179137", // 5
        "ca-app-pub-1495262574338316/4962596335", // 6
        "ca-app-pub-1495262574338316/2714752655", // 7
        "ca-app-pub-1495262574338316/3503687777"  // 8
    )

    // Standard Google AdMob test IDs for safe fallback if ad units are newly registered or returning NO_FILL
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private val isInitialized = AtomicBoolean(false)
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isInterstitialLoading = false
    private var isRewardedLoading = false

    private var currentInterstitialUnitIndex = 0
    private var currentRewardedUnitIndex = 0

    // Observability for UI
    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded.asStateFlow()

    private val _isInterstitialAdLoaded = MutableStateFlow(false)
    val isInterstitialAdLoaded: StateFlow<Boolean> = _isInterstitialAdLoaded.asStateFlow()

    fun initialize(context: Context) {
        if (isInitialized.compareAndSet(false, true)) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val requestConfiguration = RequestConfiguration.Builder()
                        .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                        .build()
                    MobileAds.setRequestConfiguration(requestConfiguration)

                    MobileAds.initialize(context.applicationContext) { initStatus ->
                        Log.d(TAG, "AdMob SDK Initialized: $initStatus")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "AdMob initialization failed", e)
                }
            }
        }
    }

    /**
     * Preloads Interstitial Ad from user list with graceful test fallback.
     */
    fun loadInterstitial(context: Context, useFallbackTest: Boolean = false) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val unitId = if (useFallbackTest) {
            TEST_INTERSTITIAL_ID
        } else {
            USER_AD_UNIT_IDS[currentInterstitialUnitIndex % USER_AD_UNIT_IDS.size]
        }

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            unitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    _isInterstitialAdLoaded.value = true
                    Log.d(TAG, "Interstitial loaded successfully with unit: $unitId")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    _isInterstitialAdLoaded.value = false
                    Log.w(TAG, "Interstitial failed to load ($unitId): ${loadAdError.message}")

                    // Fallback directly to test ad if unapproved or no-fill, avoiding recursive codec hammering
                    if (!useFallbackTest) {
                        currentInterstitialUnitIndex = (currentInterstitialUnitIndex + 1) % USER_AD_UNIT_IDS.size
                        loadInterstitial(context, useFallbackTest = true)
                    }
                }
            }
        )
    }

    /**
     * Shows Interstitial Ad when player loses or between levels.
     */
    fun showInterstitial(
        activity: Activity?,
        onDismissed: () -> Unit = {}
    ) {
        val ad = interstitialAd
        if (activity != null && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    _isInterstitialAdLoaded.value = false
                    loadInterstitial(activity.applicationContext)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    _isInterstitialAdLoaded.value = false
                    loadInterstitial(activity.applicationContext)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed full screen")
                }
            }
            ad.show(activity)
        } else {
            // Not ready or no activity, proceed immediately
            activity?.let { loadInterstitial(it.applicationContext) }
            onDismissed()
        }
    }

    /**
     * Preloads Rewarded Ad for extra life / continue.
     */
    fun loadRewardedAd(context: Context, useFallbackTest: Boolean = false) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val unitId = if (useFallbackTest) {
            TEST_REWARDED_ID
        } else {
            USER_AD_UNIT_IDS[currentRewardedUnitIndex % USER_AD_UNIT_IDS.size]
        }

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            unitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    _isRewardedAdLoaded.value = true
                    Log.d(TAG, "Rewarded Ad loaded successfully: $unitId")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    _isRewardedAdLoaded.value = false
                    Log.w(TAG, "Rewarded Ad failed to load ($unitId): ${loadAdError.message}")

                    // Fallback directly to test ad if unapproved or no-fill, avoiding recursive codec hammering
                    if (!useFallbackTest) {
                        currentRewardedUnitIndex = (currentRewardedUnitIndex + 1) % USER_AD_UNIT_IDS.size
                        loadRewardedAd(context, useFallbackTest = true)
                    }
                }
            }
        )
    }

    /**
     * Shows Rewarded Ad for Extra Life ("vida extra por ver anuncio").
     */
    fun showRewardedAd(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit = {},
        onAdUnavailable: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (activity != null && ad != null) {
            var rewardGranted = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    loadRewardedAd(activity.applicationContext)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    loadRewardedAd(activity.applicationContext)
                    onAdUnavailable()
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                rewardGranted = true
                onRewardEarned()
            }
        } else {
            activity?.let { loadRewardedAd(it.applicationContext) }
            onAdUnavailable()
        }
    }
}
