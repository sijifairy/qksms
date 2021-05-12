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
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.core.view.iterator
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.androidxcompat.drawerOpen
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.*
import com.moez.QKSMS.common.widget.QkTextView
import com.moez.QKSMS.databinding.MainActivityBinding
import com.moez.QKSMS.extensions.Optional
import com.moez.QKSMS.extensions.asObservable
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.feature.blocking.BlockingDialog
import com.moez.QKSMS.feature.conversations.ConversationItemTouchCallback
import com.moez.QKSMS.feature.conversations.ConversationsAdapter
import com.moez.QKSMS.manager.ChangelogManager
import com.moez.QKSMS.repository.SyncRepository
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.toolbar.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainHolder : DaggerFragment(), MainView {

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var blockingDialog: BlockingDialog

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

    @Inject
    lateinit var prefs: Preferences

    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val queryChangedIntent by lazy { binding.toolbarSearch.textChanges() }
    override val composeIntent by lazy { binding.compose.clicks() }
    override val drawerOpenIntent: Observable<Boolean> by lazy {
        binding.drawerLayout
                .drawerOpen(Gravity.START)
                .doOnNext {
                    activity!!.window.currentFocus?.let { focus ->
                        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(focus.windowToken, 0)

                        focus.clearFocus()
                    }
                }
    }
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val navigationIntent: Observable<NavItem> by lazy {
        Observable.merge(listOf(
                backPressedSubject,
                binding.drawer.inbox.clicks().map { NavItem.INBOX },
                binding.drawer.archived.clicks().map { NavItem.ARCHIVED },
                binding.drawer.backup.clicks().map { NavItem.BACKUP },
                binding.drawer.scheduled.clicks().map { NavItem.SCHEDULED },
                binding.drawer.blocking.clicks().map { NavItem.BLOCKING },
                binding.drawer.settings.clicks().map { NavItem.SETTINGS },
                binding.drawer.plus.clicks().map { NavItem.PLUS },
                binding.drawer.help.clicks().map { NavItem.HELP },
                binding.drawer.invite.clicks().map { NavItem.INVITE }))
    }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val plusBannerIntent by lazy { binding.drawer.plusBanner.clicks() }
    override val dismissRatingIntent by lazy { binding.drawer.rateDismiss.clicks() }
    override val rateIntent by lazy { binding.drawer.rateOkay.clicks() }
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    override val swipeConversationIntent by lazy { itemTouchCallback.swipes }

    //    override val changelogMoreIntent by lazy { changelogDialog.moreClicks }
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(activity!! as FragmentActivity, viewModelFactory)[MainViewModel::class.java] }
    private val toggle by lazy { ActionBarDrawerToggle(activity!!, binding.drawerLayout, binding.toolbar, R.string.main_drawer_open_cd, 0) }
    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }
    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingView.findViewById(R.id.syncingProgress), "progress", 0, 0) }

    //    private val changelogDialog by lazy { ChangelogDialog(this) }
    private val snackbar by lazy { binding.root.findViewById<View>(R.id.snackbar) }
    private lateinit var snackbarView: View
    private val syncing by lazy { binding.root.findViewById<View>(R.id.syncing) }
    private lateinit var syncingView: View
    private val backPressedSubject: Subject<NavItem> = PublishSubject.create()

    private var _binding: MainActivityBinding? = null
    private val binding get() = _binding!!
    private val theme by lazy { colors.themeObservable() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = MainActivityBinding.inflate(inflater, container, false)

        viewModel.bindView(this)
        Log.d("Lifecycle", "main bind view")
//        onNewIntentIntent.onNext(intent)

        (snackbar as? ViewStub)?.setOnInflateListener { _, view ->
            snackbarView = view
            view.findViewById<View>(R.id.snackbarButton).clicks()
                    .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                    .subscribe(snackbarButtonIntent)
        }

        (syncing as? ViewStub)?.setOnInflateListener { _, view ->
            syncingView = view
            (syncingView.findViewById(R.id.syncingProgress) as ProgressBar)?.progressTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
            (syncingView.findViewById(R.id.syncingProgress) as ProgressBar)?.indeterminateTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
        }

        toggle.syncState()
        binding.toolbar.setNavigationOnClickListener {
            activity!!.window.currentFocus?.let { focus ->
                val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focus.windowToken, 0)

                focus.clearFocus()
            }
            homeIntent.onNext(Unit)
        }

//        itemTouchCallback.adapter = conversationsAdapter
        conversationsAdapter.autoScrollToStart(binding.recyclerView)

        // Don't allow clicks to pass through the drawer layout
        binding.drawer.root.clicks().autoDisposable(scope(Lifecycle.Event.ON_DESTROY)).subscribe()

        // Set the theme color tint to the recyclerView, progressbar, and FAB
        theme
                .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                .subscribe { theme ->
                    // Set the color for the drawer icons
                    val states = arrayOf(
                            intArrayOf(android.R.attr.state_activated),
                            intArrayOf(-android.R.attr.state_activated))

                    activity!!.resolveThemeColor(android.R.attr.textColorSecondary)
                            .let { textSecondary -> ColorStateList(states, intArrayOf(theme.theme, textSecondary)) }
                            .let { tintList ->
                                binding.drawer.inboxIcon.imageTintList = tintList
                                binding.drawer.archivedIcon.imageTintList = tintList
                            }

                    // Miscellaneous views
                    listOf(binding.drawer.plusBadge1, binding.drawer.plusBadge2).forEach { badge ->
                        badge.setBackgroundTint(theme.theme)
                        badge.setTextColor(theme.textPrimary)
                    }
                    binding.drawer.plusIcon.setTint(theme.theme)
                    binding.drawer.rateIcon.setTint(theme.theme)
                    binding.compose.setBackgroundTint(theme.theme)

                    // Set the FAB compose icon color
                    binding.compose.setTint(theme.textPrimary)
                }

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            binding.toolbarSearch.setBackgroundTint(activity!!.resolveThemeColor(R.attr.bubbleColor))
        }

        binding.toolbar.inflateMenu(R.menu.main)
        binding.toolbar.setOnMenuItemClickListener {
            optionsItemIntent.onNext(it.itemId)
            true
        }
        binding.toolbar.overflowIcon = binding.toolbar.overflowIcon?.apply {
            setTint(activity!!.resolveThemeColor(android.R.attr.textColorSecondary))
        }
        binding.toolbar.menu.iterator().forEach { menuItem ->
            val tint = activity!!.resolveThemeColor(android.R.attr.textColorSecondary)
            menuItem.icon = menuItem.icon?.apply { setTint(tint) }
        }

        // When certain preferences change, we need to recreate the activity
        val triggers = listOf(prefs.nightMode, prefs.night, prefs.black, prefs.textSize, prefs.systemFont)
        Observable.merge(triggers.map { it.asObservable().skip(1) })
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                .subscribe { activity!!.recreate() }

        return binding.root
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        intent?.run(onNewIntentIntent::onNext)
//    }

    override fun render(state: MainState) {
        if (state.hasError) {
            activity!!.finish()
            return
        }

        val addContact = when (state.page) {
            is Inbox -> state.page.addContact
            is Archived -> state.page.addContact
            else -> false
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

        binding.toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
        binding.toolbarTitle.setVisible(binding.toolbarSearch.visibility != View.VISIBLE)

        binding.toolbar.menu.findItem(R.id.archive)?.isVisible = state.page is Inbox && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.unarchive)?.isVisible = state.page is Archived && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.add)?.isVisible = addContact && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.pin)?.isVisible = markPinned && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0

        listOf(binding.drawer.plusBadge1, binding.drawer.plusBadge2).forEach { badge ->
            badge.isVisible = drawerBadgesExperiment.variant && !state.upgraded
        }
        binding.drawer.plus.isVisible = state.upgraded
        binding.drawer.plusBanner.isVisible = !state.upgraded
        binding.drawer.rateLayout.setVisible(state.showRating)

        binding.compose.setVisible(state.page is Inbox || state.page is Archived)
        conversationsAdapter.emptyView = binding.empty.takeIf { state.page is Inbox || state.page is Archived }
        searchAdapter.emptyView = binding.empty.takeIf { state.page is Searching }

        when (state.page) {
            is Inbox -> {
                showBackButton(state.page.selected > 0)
                binding.toolbarTitle.text = getString(R.string.main_title_selected, state.page.selected)
                if (binding.recyclerView.adapter !== conversationsAdapter) binding.recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
//                itemTouchHelper.attachToRecyclerView(binding.recyclerView)
                binding.empty.setText(R.string.inbox_empty_text)
            }

            is Searching -> {
                showBackButton(true)
                if (binding.recyclerView.adapter !== searchAdapter) binding.recyclerView.adapter = searchAdapter
                searchAdapter.data = state.page.data ?: listOf()
//                itemTouchHelper.attachToRecyclerView(null)
                binding.empty.setText(R.string.inbox_search_empty_text)
            }

            is Archived -> {
                showBackButton(state.page.selected > 0)
                binding.toolbarTitle.text = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }
                if (binding.recyclerView.adapter !== conversationsAdapter) binding.recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
//                itemTouchHelper.attachToRecyclerView(null)
                binding.empty.setText(R.string.archived_empty_text)
            }
        }

        binding.drawer.inbox.isActivated = state.page is Inbox
        binding.drawer.archived.isActivated = state.page is Archived

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START) && !state.drawerOpen) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (!binding.drawerLayout.isDrawerVisible(GravityCompat.START) && state.drawerOpen) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        when (state.syncing) {
            is SyncRepository.SyncProgress.Idle -> {
                syncing.isVisible = false
                snackbar.isVisible = !state.defaultSms || !state.smsPermission || !state.contactPermission
            }

            is SyncRepository.SyncProgress.Running -> {
                syncing.isVisible = true
                (syncingView.findViewById(R.id.syncingProgress) as ProgressBar).max = state.syncing.max
                progressAnimator.apply { setIntValues((syncingView.findViewById(R.id.syncingProgress) as ProgressBar).progress, state.syncing.progress) }.start()
                (syncingView.findViewById(R.id.syncingProgress) as ProgressBar).isIndeterminate = state.syncing.indeterminate
                snackbar.isVisible = false
            }
        }

        when {
            !state.defaultSms -> {
                (snackbarView.findViewById(R.id.snackbarTitle) as QkTextView)?.setText(R.string.main_default_sms_title)
                (snackbarView.findViewById(R.id.snackbarMessage) as QkTextView)?.setText(R.string.main_default_sms_message)
                (snackbarView.findViewById(R.id.snackbarButton) as QkTextView)?.setText(R.string.main_default_sms_change)
            }

            !state.smsPermission -> {
                (snackbarView.findViewById(R.id.snackbarTitle) as QkTextView)?.setText(R.string.main_permission_required)
                (snackbarView.findViewById(R.id.snackbarMessage) as QkTextView)?.setText(R.string.main_permission_sms)
                (snackbarView.findViewById(R.id.snackbarButton) as QkTextView)?.setText(R.string.main_permission_allow)
            }

            !state.contactPermission -> {
                (snackbarView.findViewById(R.id.snackbarTitle) as QkTextView)?.setText(R.string.main_permission_required)
                (snackbarView.findViewById(R.id.snackbarMessage) as QkTextView)?.setText(R.string.main_permission_contacts)
                (snackbarView.findViewById(R.id.snackbarButton) as QkTextView)?.setText(R.string.main_permission_allow)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityResumedIntent.onNext(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    fun showBackButton(show: Boolean) {
        toggle.onDrawerSlide(binding.drawer.root, if (show) 1f else 0f)
        toggle.drawerArrowDrawable.color = when (show) {
            true -> activity!!.resolveThemeColor(android.R.attr.textColorSecondary)
            false -> activity!!.resolveThemeColor(android.R.attr.textColorPrimary)
        }
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog(activity!!)
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS), 0)
    }

    override fun clearSearch() {
        activity!!.window.currentFocus?.let { focus ->
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(focus.windowToken, 0)

            focus.clearFocus()
        }
        binding.toolbarSearch.text = null
    }

    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun themeChanged() {
        binding.recyclerView.scrapViews()
    }

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
        blockingDialog.show(activity!!, conversations, block)
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(resources.getQuantityString(R.plurals.dialog_delete_message, count, count))
                .setPositiveButton(R.string.button_delete) { _, _ -> confirmDeleteIntent.onNext(conversations) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun showChangelog(changelog: ChangelogManager.CumulativeChangelog) {
//        changelogDialog.show(changelog)
    }

    override fun showArchivedSnackbar() {
        Snackbar.make(binding.drawerLayout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
            setActionTextColor(colors.theme().theme)
            show()
        }
    }

    fun handleBackPressed(): Boolean {
        backPressedSubject.onNext(NavItem.BACK)
        return false
    }
}