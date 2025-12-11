package com.tagmypet.ui.components

import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView
import com.tagmypet.R

@Composable
fun AdMobNativePost(
    // ID de teste oficial do Google para Native Ads
    adUnitId: String = "ca-app-pub-3940256099942544/2247696110",
) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            val adView = LayoutInflater.from(context)
                .inflate(R.layout.ad_native_layout, null) as NativeAdView

            val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
            val bodyView = adView.findViewById<TextView>(R.id.ad_body)
            val callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
            val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
            val mediaView = adView.findViewById<MediaView>(R.id.ad_media)

            adView.headlineView = headlineView
            adView.bodyView = bodyView
            adView.callToActionView = callToActionView
            adView.iconView = iconView
            adView.mediaView = mediaView

            val adLoader = AdLoader.Builder(context, adUnitId)
                .forNativeAd { nativeAd ->
                    headlineView.text = nativeAd.headline
                    bodyView.text = nativeAd.body
                    callToActionView.text = nativeAd.callToAction

                    if (nativeAd.icon != null) {
                        iconView.setImageDrawable(nativeAd.icon?.drawable)
                        iconView.visibility = android.view.View.VISIBLE
                    } else {
                        iconView.visibility = android.view.View.GONE
                    }

                    mediaView.setMediaContent(nativeAd.mediaContent)
                    adView.setNativeAd(nativeAd)
                }
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
            adView
        }
    )
}