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

import com.bluelinelabs.conductor.Router
import com.moez.QKSMS.common.base.QkView
import io.reactivex.Observable

interface MainView : QkView<MainState> {

    fun activityResumed(): Observable<*>
    fun drawerOpened(): Observable<Boolean>
    fun drawerItemSelected(): Observable<DrawerItem>
    fun optionsItemSelected(): Observable<Int>
    fun plusBannerClicked(): Observable<*>
    fun ratingDismissed(): Observable<*>
    fun ratingClicked(): Observable<*>
    fun snackbarClicked(): Observable<*>
    fun backPressed(): Observable<*>

    fun getRouter(): Router

    fun requestPermissions()
}

enum class DrawerItem { INBOX, ARCHIVED, SCHEDULED, BLOCKING, SETTINGS, PLUS, HELP, INVITE }