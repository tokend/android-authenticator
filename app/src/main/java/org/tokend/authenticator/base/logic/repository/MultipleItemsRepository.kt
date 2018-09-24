package org.tokend.authenticator.base.logic.repository

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

/**
 * Repository that holds a list of [T] items.
 */
abstract class MultipleItemsRepository<T> : Repository() {
    protected abstract val itemsCache: RepositoryCache<T>

    protected val itemsSubject: BehaviorSubject<List<T>> =
            BehaviorSubject.createDefault<List<T>>(listOf())

    /**
     * Emits items list updates.
     * Will emit actual list on subscribe.
     */
    open val itemsObservable: Observable<List<T>>
        get() = itemsSubject

    open val itemsList: List<T>
        get() = itemsSubject.value ?: emptyList()

    protected open fun broadcast() {
        itemsSubject.onNext(itemsCache.items)
    }

    protected abstract fun getItems(): Single<List<T>>

    protected open fun onNewItems(newItems: List<T>) {
        isNeverUpdated = false
        isFresh = true

        itemsCache.transform(newItems)

        broadcast()
    }
}