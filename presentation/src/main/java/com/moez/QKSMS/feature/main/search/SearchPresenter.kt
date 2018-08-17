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
package com.moez.QKSMS.feature.main.search

import com.moez.QKSMS.common.base.QkPresenter
import com.moez.QKSMS.extensions.removeAccents
import com.moez.QKSMS.repository.ConversationRepository
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchPresenter @Inject constructor(
        private val conversationRepo: ConversationRepository
) : QkPresenter<SearchView, SearchState>(SearchState()) {

    override fun bindIntents(view: SearchView) {
        super.bindIntents(view)

        view.queryChanges()
                .debounce(200, TimeUnit.MILLISECONDS)
                .map { query -> query.removeAccents() }
                .doOnNext { newState { copy(loading = true) } }
                .switchMap { query ->
                    when (query.length >= 2) {
                        true -> Observable.just(query)
                                .observeOn(Schedulers.io())
                                .map(conversationRepo::searchConversations)

                        false -> Observable.just(listOf())
                    }
                }
                .autoDisposable(view.scope())
                .subscribe { data -> newState { copy(loading = false, data = data) } }
    }

}