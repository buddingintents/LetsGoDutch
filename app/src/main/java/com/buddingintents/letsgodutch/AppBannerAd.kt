package com.buddingintents.letsgodutch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun LetsGoDutchBannerAd(
    productionAdUnitId: String,
    modifier: Modifier = Modifier,
) {
    val adWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtLeast(320)

    key(adWidthDp) {
        AndroidView(
            modifier = modifier,
            factory = { viewContext ->
                AdView(viewContext).apply {
                    adUnitId = productionAdUnitId
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            viewContext,
                            adWidthDp,
                        ),
                    )
                    loadAd(AdRequest.Builder().build())
                }
            },
        )
    }
}
