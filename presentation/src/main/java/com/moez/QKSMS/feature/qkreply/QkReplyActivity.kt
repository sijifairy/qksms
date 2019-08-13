/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.feature.qkreply

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.SmsAnalytics
import com.moez.QKSMS.common.util.extensions.autoScrollToStart
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.feature.compose.MessagesAdapter
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.qkreply_activity.*
import javax.inject.Inject

class QkReplyActivity : QkThemedActivity(), QkReplyView {

    @Inject
    lateinit var adapter: MessagesAdapter
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val menuItemIntent: Subject<Int> = PublishSubject.create()
    override val textChangedIntent by lazy { message.textChanges() }
    override val changeSimIntent by lazy { sim.clicks() }
    override val sendIntent by lazy { send.clicks() }

    lateinit var nativeAd: UnifiedNativeAd
    private fun isNativeAdInitialized() = ::nativeAd.isInitialized

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[QkReplyViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        setFinishOnTouchOutside(prefs.qkreplyTapDismiss.get())
        setContentView(R.layout.qkreply_activity)
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        viewModel.bindView(this)

        toolbar.clipToOutline = true

        messages.adapter = adapter
        messages.adapter?.autoScrollToStart(messages)
        messages.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = messages.scrollToPosition(adapter.itemCount - 1)
        })

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            toolbar.setBackgroundTint(resolveThemeColor(R.attr.colorPrimary))
            background.setBackgroundTint(resolveThemeColor(R.attr.composeBackground))
            messageBackground.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
            composeBackgroundGradient.setBackgroundTint(resolveThemeColor(R.attr.composeBackground))
            composeBackgroundSolid.setBackgroundTint(resolveThemeColor(R.attr.composeBackground))
        }

        val adLoader = AdLoader.Builder(this, "ca-app-pub-5061957740026229/7256693868")
                .forUnifiedNativeAd { unifiedNativeAd: UnifiedNativeAd ->
                    // Assumes that your ad layout is in a file call ad_unified.xml
                    // in the res/layout folder
                    nativeAd = unifiedNativeAd;
                    val adView = layoutInflater
                            .inflate(R.layout.ad_unified, null) as UnifiedNativeAdView
                    // This method sets the text, images and the native ad, etc into the ad
                    // view.
                    populateUnifiedNativeAdView(nativeAd, adView)
                    // Assumes you have a placeholder FrameLayout in your View layout
                    // (with id ad_frame) where the ad is to be placed.
                    ad_frame.removeAllViews()
                    ad_frame.addView(adView)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build()
        adLoader.loadAd(AdRequest.Builder().build())
        SmsAnalytics.logEvent("Reply_Ad_Chance")

        SmsAnalytics.logEvent("Reply_Create")
    }

    private fun populateUnifiedNativeAdView(ad: UnifiedNativeAd, adView: UnifiedNativeAdView) {
        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        adView.mediaView = mediaView

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)

        // The headline is guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).setText(ad.getHeadline())
        val night = prefs.night.get()
        if (night) {
            (adView.headlineView as TextView).setTextColor(0xffffffff.toInt())
        } else {
            (adView.headlineView as TextView).setTextColor(0xff333333.toInt())
        }

        if (ad.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).setText(ad.getBody())
        }

        if (ad.getCallToAction() == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).setText(ad.getCallToAction())
        }

        if (ad.getIcon() == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                    ad.getIcon().getDrawable())
            adView.iconView.visibility = View.VISIBLE
        }
        adView.setNativeAd(ad)

        SmsAnalytics.logEvent("Reply_Ad_Show")
    }

    override fun onResume() {
        super.onResume()

        SmsAnalytics.logEvent("Reply_Resume")
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isNativeAdInitialized() && nativeAd != null) {
            nativeAd.destroy()
        }
    }

    override fun render(state: QkReplyState) {
        if (state.hasError) {
            finish()
        }

        threadId.onNext(state.selectedConversation)

        title = state.title

//        toolbar.menu.findItem(R.id.expand)?.isVisible = !state.expanded
//        toolbar.menu.findItem(R.id.collapse)?.isVisible = state.expanded

        adapter.data = state.data

        counter.text = state.remaining
        counter.setVisible(counter.text.isNotBlank())

        sim.setVisible(state.subscription != null)
        sim.contentDescription = getString(R.string.compose_sim_cd, state.subscription?.displayName)
        simIndex.text = "${state.subscription?.simSlotIndex?.plus(1)}"

        send.isEnabled = state.canSend
        send.imageAlpha = if (state.canSend) 255 else 128
    }

    override fun setDraft(draft: String) {
        message.setText(draft)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.qkreply, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        menuItemIntent.onNext(item.itemId)
        return true
    }

    override fun getActivityThemeRes(night: Boolean, black: Boolean) = when {
        night && black -> R.style.AppThemeBlackDialog
        night && !black -> R.style.AppThemeDarkDialog
        else -> R.style.AppThemeLightDialog
    }

}