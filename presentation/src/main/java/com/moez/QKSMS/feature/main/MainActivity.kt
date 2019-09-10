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
package com.moez.QKSMS.feature.main

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.appodeal.ads.Appodeal
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.androidxcompat.drawerOpen
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.dialog.FiveStarRateDialog
import com.moez.QKSMS.common.util.Navigations
import com.moez.QKSMS.common.util.Permissions
import com.moez.QKSMS.common.util.Preferences
import com.moez.QKSMS.common.util.SmsAnalytics
import com.moez.QKSMS.common.util.extensions.*
import com.moez.QKSMS.feature.changelog.ChangelogDialog
import com.moez.QKSMS.feature.conversations.ConversationItemTouchCallback
import com.moez.QKSMS.feature.conversations.ConversationsAdapter
import com.moez.QKSMS.feature.guide.SettingLauncherPadActivity
import com.moez.QKSMS.feature.guide.UsageUtils
import com.moez.QKSMS.repository.SyncRepository
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import io.presage.finder.model.App
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_permission_hint.*
import kotlinx.android.synthetic.main.main_syncing.*
import javax.inject.Inject

class MainActivity : QkThemedActivity(), MainView {

    @Inject
    lateinit var disposables: CompositeDisposable
    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var conversationsAdapter: ConversationsAdapter
    @Inject
    lateinit var drawerBadgesExperiment: DrawerBadgesExperiment
    @Inject
    lateinit var searchAdapter: SearchAdapter
    @Inject
    lateinit var itemTouchCallback: ConversationItemTouchCallback
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

//    lateinit var mAdView: AdView

    override val activityResumedIntent: Subject<Unit> = PublishSubject.create()
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val composeIntent by lazy { compose.clicks() }
    override val drawerOpenIntent: Observable<Boolean> by lazy {
        drawerLayout
                .drawerOpen(Gravity.START)
                .doOnNext { dismissKeyboard() }
    }
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val drawerItemIntent: Observable<DrawerItem> by lazy {
        Observable.merge(listOf(
                inbox.clicks().map { DrawerItem.INBOX },
                archived.clicks().map { DrawerItem.ARCHIVED },
                backup.clicks().map { DrawerItem.BACKUP },
                scheduled.clicks().map { DrawerItem.SCHEDULED },
                blocking.clicks().map { DrawerItem.BLOCKING },
                settings.clicks().map { DrawerItem.SETTINGS },
                plus.clicks().map { DrawerItem.PLUS },
                help.clicks().map { DrawerItem.HELP },
                invite.clicks().map { DrawerItem.INVITE }))
    }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val plusBannerIntent by lazy { plusBanner.clicks() }
    override val dismissRatingIntent by lazy { rateDismiss.clicks() }
    override val rateIntent by lazy { rateOkay.clicks() }
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    override val swipeConversationIntent by lazy { itemTouchCallback.swipes }
    override val changelogMoreIntent by lazy { changelog.moreClicks }
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java] }
    private val toggle by lazy { ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.main_drawer_open_cd, 0) }
    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }
    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingProgress, "progress", 0, 0) }
    private val changelog by lazy { ChangelogDialog(this) }
    private val archiveSnackbar by lazy {
        Snackbar.make(drawerLayout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
        }
    }
    private val snackbar by lazy { findViewById<View>(R.id.snackbar) }
    private val syncing by lazy { findViewById<View>(R.id.syncing) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel.bindView(this)

        SmsAnalytics.logEvent("Main_Create")
        Appodeal.initialize(this, "69b85ac97d3b099a7f6ddb1cf6cd374cbcd5abe0b7441eee",
                Appodeal.BANNER or Appodeal.INTERSTITIAL or Appodeal.NATIVE, true)

        (snackbar as? ViewStub)?.setOnInflateListener { _, _ ->
            snackbarButton.clicks().subscribe(snackbarButtonIntent)
        }

        (syncing as? ViewStub)?.setOnInflateListener { _, _ ->
            syncingProgress?.progressTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
            syncingProgress?.indeterminateTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
        }

        toggle.syncState()
        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }

//        supportActionBar?.setHomeButtonEnabled(true)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.ic_navigation_drawer))

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Don't allow clicks to pass through the drawer layout
        drawer.clicks().subscribe()

        // Set the theme color tint to the recyclerView, progressbar, and FAB
        theme
                .doOnNext { recyclerView.scrapViews() }
                .autoDisposable(scope())
                .subscribe { theme ->
                    // Set the color for the drawer icons
                    val states = arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf(-android.R.attr.state_activated))
                    resolveThemeColor(android.R.attr.textColorSecondary)
                            .let { textSecondary -> ColorStateList(states, intArrayOf(theme.theme, textSecondary)) }
                            .let { tintList ->
                                inboxIcon.imageTintList = tintList
                                archivedIcon.imageTintList = tintList
                            }

                    // Miscellaneous views
                    listOf(plusBadge1, plusBadge2).forEach { badge ->
                        badge.setBackgroundTint(theme.theme)
                        badge.setTextColor(theme.textPrimary)
                    }
                    syncingProgress?.progressTintList = ColorStateList.valueOf(theme.theme)
                    syncingProgress?.indeterminateTintList = ColorStateList.valueOf(theme.theme)
                    plusIcon.setTint(theme.theme)
                    rateIcon.setTint(theme.theme)
                    compose.setBackgroundTint(theme.theme)

                    toolbar.setBackgroundColor(theme.theme)

                    // Set the FAB compose icon color
                    compose.setTint(theme.textPrimary)
                }

        itemTouchCallback.adapter = conversationsAdapter
//        conversationsAdapter.autoScrollToStart(recyclerView)

//        mAdView = AdView(this)
//        mAdView.adSize = AdSize.BANNER
//        mAdView.adUnitId = FirebaseRemoteConfig.getInstance().getString("Ad_Homepage_Banner_Admob_ID");
//        adViewContainer.addView(mAdView)
//        val adRequest = AdRequest.Builder().addTestDevice("023989BFBFB4293AF9F7F96F1DDBFC65").build()
//        mAdView.loadAd(adRequest)
        SmsAnalytics.logEvent("Main_Banner_Chance")
//        mAdView.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                // Code to be executed when an ad finishes loading.
//                SmsAnalytics.logEvent("Main_Banner_Show")
//            }
//
//            override fun onAdFailedToLoad(errorCode: Int) {
//                // Code to be executed when an ad request fails.
//            }
//
//            override fun onAdOpened() {
//                // Code to be executed when an ad opens an overlay that
//                // covers the screen.
//            }
//
//            override fun onAdClicked() {
//                // Code to be executed when the user clicks on an ad.
//                SmsAnalytics.logEvent("Main_Banner_Click")
//            }
//
//            override fun onAdLeftApplication() {
//                // Code to be executed when the user has left the app.
//            }
//
//            override fun onAdClosed() {
//                // Code to be executed when the user is about to return
//                // to the app after tapping on an ad.
//            }
//        }

        Appodeal.setBannerViewId(R.id.adViewContainer)
        Appodeal.show(this, Appodeal.BANNER_VIEW, "HomepageBanner")
    }

    override fun render(state: MainState) {
        if (state.hasError) {
            finish()
            return
        }

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }

        toolbarSearch.setVisible(false)
        toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)

        toolbar.menu.findItem(R.id.archive)?.isVisible = state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible = state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.pin)?.isVisible = markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0

        listOf(plusBadge1, plusBadge2).forEach { badge ->
            badge.isVisible = drawerBadgesExperiment.variant && !state.upgraded
        }
        plus.isVisible = false
        plusBanner.isVisible = false
        rateLayout.setVisible(false)

        compose.setVisible(state.page is Inbox || state.page is Archived)
        conversationsAdapter.emptyView = empty.takeIf { state.page is Inbox || state.page is Archived }

        when (state.page) {
            is Inbox -> {
                showBackButton(state.page.selected > 0)
                title = if (state.page.selected > 0) {
                    getString(R.string.main_title_selected, state.page.selected)
                } else {
                    "Messages"
                }
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                itemTouchHelper.attachToRecyclerView(recyclerView)
                empty.setText(R.string.inbox_empty_text)
            }

            is Searching -> {
                showBackButton(true)
                if (recyclerView.adapter !== searchAdapter) recyclerView.adapter = searchAdapter
                searchAdapter.data = state.page.data ?: listOf()
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.inbox_search_empty_text)
            }

            is Archived -> {
                showBackButton(state.page.selected > 0)
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.archived_empty_text)
            }
        }

        inbox.isActivated = state.page is Inbox
        archived.isActivated = state.page is Archived

        if (drawerLayout.isDrawerOpen(GravityCompat.START) && !state.drawerOpen) drawerLayout.closeDrawer(GravityCompat.START)
        else if (!drawerLayout.isDrawerVisible(GravityCompat.START) && state.drawerOpen) drawerLayout.openDrawer(GravityCompat.START)

        when (state.syncing) {
            is SyncRepository.SyncProgress.Idle -> {
                syncing.isVisible = false
                snackbar.isVisible = !state.defaultSms || !state.smsPermission || !state.contactPermission
            }

            is SyncRepository.SyncProgress.Running -> {
                syncing.isVisible = true
                syncingProgress.max = state.syncing.max
                progressAnimator.apply { setIntValues(syncingProgress.progress, state.syncing.progress) }.start()
                syncingProgress.isIndeterminate = state.syncing.indeterminate
                snackbar.isVisible = false
            }
        }

        when {
            !state.defaultSms -> {
                snackbarTitle?.setText(R.string.main_default_sms_title)
                snackbarMessage?.setText(R.string.main_default_sms_message)
                snackbarButton?.setText(R.string.main_default_sms_change)
            }

            !state.smsPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_sms)
                snackbarButton?.setText(R.string.main_permission_allow)
            }

            !state.contactPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_contacts)
                snackbarButton?.setText(R.string.main_permission_allow)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(Unit)

        Appodeal.onResume(this, Appodeal.BANNER_VIEW)
        SmsAnalytics.logEvent("Main_Resume", "nightmode", prefs.night.get().toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()

        Appodeal.destroy(Appodeal.BANNER_VIEW)
    }

    override fun showBackButton(show: Boolean) {
        toggle.onDrawerSlide(drawer, if (show) 1f else 0f)
        toggle.drawerArrowDrawable.color = when (show) {
            true -> resolveThemeColor(android.R.attr.textColorSecondary)
            false -> resolveThemeColor(android.R.attr.textColorPrimary)
        }
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS), 0)
    }

    override fun clearSearch() {
        dismissKeyboard()
        toolbarSearch.text = null
    }

    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(resources.getQuantityString(R.plurals.dialog_delete_message, count, count))
                .setPositiveButton(R.string.button_delete) { _, _ -> confirmDeleteIntent.onNext(conversations) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun showChangelog() {
        changelog.show()
    }

    override fun showArchivedSnackbar() {
        archiveSnackbar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onBackPressed() {
        if (FirebaseRemoteConfig.getInstance().getBoolean("Usage_Request_Enabled")
                && Preferences.getDefault().getInt("pref_key_usage_guide_times", 0) < 3
                && !Permissions.isUsageAccessGranted()) {
            showUsageDialog()
            Preferences.getDefault().incrementAndGetInt("pref_key_usage_guide_times")
            return
        }

        if (FiveStarRateDialog.showShowFiveStarRateDialogOnBackToDesktopIfNeed(this)) {
            return
        }
        backPressedIntent.onNext(Unit)
    }

    fun showUsageDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_usage_title)
        builder.setMessage(R.string.dialog_usage_message)
        builder.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { _, _ ->
            UsageUtils.requestUsageAccessPermission(this, {
                SettingLauncherPadActivity.closeSettingsActivity(this)
                Navigations.startActivity(this@MainActivity, MainActivity::class.java)
                SmsAnalytics.logEvent("Usage_Granted")
            }, false, false, false)
            SmsAnalytics.logEvent("Usage_Dialog_Ok_Click")
        })
        builder.setNegativeButton(R.string.button_cancel, DialogInterface.OnClickListener { dialog, which ->
            finish()
            SmsAnalytics.logEvent("Usage_Dialog_Cancel_Click")
        })
        val dialog = builder.create()
        dialog.setOnDismissListener { finish() }
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        SmsAnalytics.logEvent("Usage_Dialog_Show")
    }
}
