package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.admob.AdMobManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Bottom Rotating Banner that cycles across the user's provided AdMob Ad Unit IDs
 * while the player is in any screen or actively playing.
 */
@Composable
fun RotatingBottomAdBanner(
    modifier: Modifier = Modifier,
    rotationIntervalSeconds: Int = 30
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Text("AdMob Banner [Preview]", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    var currentUnitIndex by remember { mutableIntStateOf(0) }
    var useFallbackTest by remember { mutableStateOf(false) }
    var adLoadKey by remember { mutableIntStateOf(0) }
    var isReadyToLoad by remember { mutableStateOf(false) }

    // Automatic rotation loop: advances to next ad unit ID every rotationIntervalSeconds
    LaunchedEffect(Unit) {
        delay(600) // Allow main UI tree to render cleanly first
        isReadyToLoad = true
        while (isActive) {
            delay(rotationIntervalSeconds * 1000L)
            currentUnitIndex = (currentUnitIndex + 1) % AdMobManager.USER_AD_UNIT_IDS.size
            useFallbackTest = false
            adLoadKey++
        }
    }

    val currentAdUnitId = if (useFallbackTest) {
        AdMobManager.TEST_BANNER_ID
    } else {
        AdMobManager.USER_AD_UNIT_IDS[currentUnitIndex]
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .border(width = 0.5.dp, color = Color(0xFF334155))
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Compliance label indicating Advertisement
        Text(
            text = "PUBLICIDAD · ANUNCIO #${currentUnitIndex + 1}",
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(top = 2.dp, bottom = 1.dp)
        )

        if (!isReadyToLoad) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cargando anuncio...",
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.6f)
                )
            }
        } else {
            Crossfade(
                targetState = adLoadKey,
                label = "ad_banner_rotation"
            ) { _ ->
                AdViewContainer(
                    adUnitId = currentAdUnitId,
                    onFailedToLoad = {
                        if (!useFallbackTest) {
                            useFallbackTest = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AdViewContainer(
    adUnitId: String,
    onFailedToLoad: () -> Unit
) {
    val context = LocalContext.current
    var isLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("admob_bottom_banner"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isLoaded = true
                            Log.d("AdBannerView", "Banner ad loaded: $adUnitId")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isLoaded = false
                            Log.w("AdBannerView", "Banner ad failed to load ($adUnitId): ${error.message}")
                            onFailedToLoad()
                        }
                    }
                    val adRequest = AdRequest.Builder().build()
                    loadAd(adRequest)
                }
            },
            update = { adView ->
                // Ensure proper layout bounds
            },
            onRelease = { adView ->
                try {
                    adView.destroy()
                } catch (e: Exception) {
                    // Ignore
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (!isLoaded) {
            // Subtle placeholder while loading next rotation
            Text(
                text = "Cargando anuncio...",
                fontSize = 11.sp,
                color = Color.LightGray.copy(alpha = 0.6f)
            )
        }
    }
}
