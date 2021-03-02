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

import androidx.recyclerview.widget.ItemTouchHelper
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.common.util.SmsAnalytics
import com.moez.QKSMS.extensions.removeAccents
import com.moez.QKSMS.interactor.*
import com.moez.QKSMS.manager.ChangelogManager
import com.moez.QKSMS.manager.PermissionManager
import com.moez.QKSMS.manager.RatingManager
import com.moez.QKSMS.model.SyncLog
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.SyncRepository
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject constructor(
        markAllSeen: MarkAllSeen,
        migratePreferences: MigratePreferences,
        syncRepository: SyncRepository,
        private val changelogManager: ChangelogManager,
        private val conversationRepo: ConversationRepository,
        private val deleteConversations: DeleteConversations,
        private val markArchived: MarkArchived,
        private val markBlocked: MarkBlocked,
        private val markPinned: MarkPinned,
        private val markRead: MarkRead,
        private val markUnarchived: MarkUnarchived,
        private val markUnpinned: MarkUnpinned,
        private val markUnread: MarkUnread,
        private val navigator: Navigator,
        private val permissionManager: PermissionManager,
        private val prefs: Preferences,
        private val ratingManager: RatingManager,
        private val syncMessages: SyncMessages
) : QkViewModel<MainView, MainState>(MainState(page = Inbox(data = conversationRepo.getConversations()))) {

    init {
        disposables += deleteConversations
        disposables += markAllSeen
        disposables += markArchived
        disposables += markUnarchived
        disposables += migratePreferences
        disposables += syncMessages

        // Show the syncing UI
        disposables += syncRepository.syncProgress
                .sample(16, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribe { syncing -> newState { copy(syncing = syncing) } }

        // Show the rating UI
        disposables += ratingManager.shouldShowRating
                .subscribe { show -> newState { copy(showRating = show) } }


        // Migrate the preferences from 2.7.3
        migratePreferences.execute(Unit)


        // If we have all permissions and we've never run a sync, run a sync. This will be the case
        // when upgrading from 2.7.3, or if the app's data was cleared
        val lastSync = Realm.getDefaultInstance().use { realm ->
            realm.where(SyncLog::class.java)?.max("date") ?: 0
        }
        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
            syncMessages.execute(Unit)
        }

        ratingManager.addSession()
        markAllSeen.execute(Unit)
    }

    override fun bindView(view: MainView) {
        super.bindView(view)

        when {
            !permissionManager.isDefaultSms() -> navigator.showDefaultSmsDialog()
            !permissionManager.hasReadSms() || !permissionManager.hasContacts() -> view.requestPermissions()
        }

        val permissions = view.activityResumedIntent
                .observeOn(Schedulers.io())
                .map { Triple(permissionManager.isDefaultSms(), permissionManager.hasReadSms(), permissionManager.hasContacts()) }
                .distinctUntilChanged()
                .share()

        // If the default SMS state or permission states change, update the ViewState
        permissions
                .doOnNext { (defaultSms, smsPermission, contactPermission) ->
                    newState { copy(defaultSms = defaultSms, smsPermission = smsPermission, contactPermission = contactPermission) }
                }
                .autoDisposable(view.scope())
                .subscribe()

        // If we go from not having all permissions to having them, sync messages
        permissions
                .skip(1)
                .filter { it.first && it.second && it.third }
                .take(1)
                .autoDisposable(view.scope())
                .subscribe { syncMessages.execute(Unit) }

        // Show changelog
//        if (changelogManager.didUpdate() && Locale.getDefault().language.startsWith("en")) {
//            view.showChangelog()
//        }

        view.changelogMoreIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showChangelog() }

        view.queryChangedIntent
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map { query -> query.removeAccents() }
                .withLatestFrom(state) { query, state ->
                    if (query.isEmpty() && state.page is Searching) {
                        newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                    }
                    query
                }
                .filter { query -> query.length >= 2 }
                .doOnNext {
                    newState {
                        val page = (page as? Searching) ?: Searching()
                        copy(page = page.copy(loading = true))
                    }
                }
                .observeOn(Schedulers.io())
                .switchMap { query -> Observable.just(query).map { conversationRepo.searchConversations(it) } }
                .autoDisposable(view.scope())
                .subscribe { data -> newState { copy(page = Searching(loading = false, data = data)) } }

        view.composeIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showCompose() }

        view.homeIntent
                .withLatestFrom(state) { _, state ->
                    when {
                        state.page is Searching -> view.clearSearch()
                        state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                        state.page is Archived && state.page.selected > 0 -> view.clearSelection()

                        else -> newState { copy(drawerOpen = true) }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.drawerOpenIntent
                .autoDisposable(view.scope())
                .subscribe { open -> newState { copy(drawerOpen = open) } }

        view.drawerItemIntent
                .doOnNext { newState { copy(drawerOpen = false) } }
                .doOnNext {
                    if (it == DrawerItem.BACKUP) {
                        navigator.showBackup()
                        SmsAnalytics.logEvent("Menu_Backup_Click")
                    }
                }
                .doOnNext {
                    if (it == DrawerItem.SCHEDULED) {
                        navigator.showScheduled()
                        SmsAnalytics.logEvent("Menu_Schedule_Click")
                    }
                }
                .doOnNext {
                    if (it == DrawerItem.BLOCKING) {
                        navigator.showBlockedConversations()
                        SmsAnalytics.logEvent("Menu_Schedule_Click")
                    }
                }
                .doOnNext {
                    if (it == DrawerItem.SETTINGS) {
                        navigator.showSettings()
                        SmsAnalytics.logEvent("Menu_Setting_Click")
                    }
                }
                .doOnNext { if (it == DrawerItem.PLUS) navigator.showQksmsPlusActivity("main_menu") }
                .doOnNext {
                    if (it == DrawerItem.HELP) {
                        navigator.showSupport()
                        SmsAnalytics.logEvent("Menu_Feedback_Click")
                    }
                }
                .doOnNext { if (it == DrawerItem.INVITE) navigator.showInvite() }
                .doOnNext { if (it == DrawerItem.FONTS) navigator.showFonts() }
                .doOnNext { if (it == DrawerItem.BUBBLE) navigator.showBubble() }
                .doOnNext { if (it == DrawerItem.THEME) navigator.showTheme() }
                .doOnNext { if (it == DrawerItem.THEME_COLOR) navigator.showThemeColor() }
                .distinctUntilChanged()
                .doOnNext {
                    when (it) {
                        DrawerItem.INBOX -> {
                            newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                            SmsAnalytics.logEvent("Menu_Inbox_Click")
                        }
                        DrawerItem.ARCHIVED -> {
                            newState { copy(page = Archived(data = conversationRepo.getConversations(true))) }
                            SmsAnalytics.logEvent("Menu_Archived_Click")
                        }
                        else -> {
                        } // Do nothing
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.archive }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markArchived.execute(conversations)
                    view.clearSelection()
                    SmsAnalytics.logEvent("Main_Menu_Archive")
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.unarchive }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markUnarchived.execute(conversations)
                    view.clearSelection()
                    SmsAnalytics.logEvent("Main_Menu_Unarchive")
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.delete }
                .filter { permissionManager.isDefaultSms().also { if (!it) navigator.showDefaultSmsDialog() } }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    view.showDeleteDialog(conversations)
                    SmsAnalytics.logEvent("Main_Menu_Delete")
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.pin }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markPinned.execute(conversations)
                    view.clearSelection()
                    SmsAnalytics.logEvent("Main_Menu_Pin")
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.unpin }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markUnpinned.execute(conversations)
                    view.clearSelection()
                    SmsAnalytics.logEvent("Main_Menu_Unpin")
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.read }
                .filter { permissionManager.isDefaultSms().also { if (!it) navigator.showDefaultSmsDialog() } }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markRead.execute(conversations)
                    view.clearSelection()
                    SmsAnalytics.logEvent("Main_Menu_Read")
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.unread }
                .filter { permissionManager.isDefaultSms().also { if (!it) navigator.showDefaultSmsDialog() } }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markUnread.execute(conversations)
                    view.clearSelection()
                    SmsAnalytics.logEvent("Main_Menu_Unread")
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.optionsItemIntent
                .filter { itemId -> itemId == R.id.block }
                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                    markBlocked.execute(conversations)
                    view.clearSelection()
                    SmsAnalytics.logEvent("Main_Menu_Block")
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.plusBannerIntent
                .autoDisposable(view.scope())
                .subscribe {
                    newState { copy(drawerOpen = false) }
                    navigator.showQksmsPlusActivity("main_banner")
                }

        view.rateIntent
                .autoDisposable(view.scope())
                .subscribe {
                    navigator.showRating()
                    ratingManager.rate()
                }

        view.dismissRatingIntent
                .autoDisposable(view.scope())
                .subscribe { ratingManager.dismiss() }

        view.conversationsSelectedIntent
                .withLatestFrom(state) { selection, state ->
                    val pin = selection
                            .mapNotNull(conversationRepo::getConversation)
                            .sumBy { if (it.pinned) -1 else 1 } >= 0
                    val read = selection
                            .mapNotNull(conversationRepo::getConversation)
                            .sumBy { if (it.read) -1 else 1 } >= 0
                    val selected = selection.size

                    when (state.page) {
                        is Inbox -> {
                            val page = state.page.copy(markPinned = pin, markRead = read, selected = selected)
                            newState { copy(page = page.copy(markRead = read, selected = selected)) }
                        }

                        is Archived -> {
                            val page = state.page.copy(markPinned = pin, markRead = read, selected = selected)
                            newState { copy(page = page) }
                        }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        // Delete the conversation
        view.confirmDeleteIntent
                .autoDisposable(view.scope())
                .subscribe { conversations ->
                    deleteConversations.execute(conversations)
                    view.clearSelection()
                }

        view.swipeConversationIntent
                .autoDisposable(view.scope())
                .subscribe { (threadId, direction) ->
                    val action = if (direction == ItemTouchHelper.RIGHT) prefs.swipeRight.get() else prefs.swipeLeft.get()
                    when (action) {
                        Preferences.SWIPE_ACTION_ARCHIVE -> markArchived.execute(listOf(threadId)) { view.showArchivedSnackbar() }
                        Preferences.SWIPE_ACTION_DELETE -> view.showDeleteDialog(listOf(threadId))
                        Preferences.SWIPE_ACTION_CALL -> conversationRepo.getConversation(threadId)?.recipients?.firstOrNull()?.address?.let(navigator::makePhoneCall)
                        Preferences.SWIPE_ACTION_READ -> markRead.execute(listOf(threadId))
                        Preferences.SWIPE_ACTION_UNREAD -> markUnread.execute(listOf(threadId))
                    }
                }

        view.undoArchiveIntent
                .withLatestFrom(view.swipeConversationIntent) { _, pair -> pair.first }
                .autoDisposable(view.scope())
                .subscribe { threadId -> markUnarchived.execute(listOf(threadId)) }

        view.snackbarButtonIntent
                .withLatestFrom(state) { _, state ->
                    when {
                        !state.defaultSms -> navigator.showDefaultSmsDialog()
                        !state.smsPermission -> view.requestPermissions()
                        !state.contactPermission -> view.requestPermissions()
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.backPressedIntent
                .withLatestFrom(state) { _, state ->
                    when {
                        state.drawerOpen -> newState { copy(drawerOpen = false) }

                        state.page is Searching -> view.clearSearch()

                        state.page is Inbox && state.page.selected > 0 -> view.clearSelection()

                        state.page is Archived && state.page.selected > 0 -> view.clearSelection()

                        state.page !is Inbox -> newState { copy(page = Inbox(data = conversationRepo.getConversations())) }

                        else -> newState { copy(hasError = true) }
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()
    }

}