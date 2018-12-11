package org.tokend.authenticator.base.repository

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * Repository that holds a single [T] item.
 */
abstract class SingleItemRepository<T> : Repository() {
    protected var mItem: T? = null

    protected val itemSubject: BehaviorSubject<T> = BehaviorSubject.create()

    protected val itemObservable: Observable<T>
        get() = itemSubject

    protected open fun broadcast() {
        mItem?.let { itemSubject.onNext(it) }
    }

    open val itemValue: T?
        get() = itemSubject.value

    abstract protected fun getItem(): Observable<T>

    protected open fun getStoredItem(): Observable<T> {
        return Observable.empty()
    }

    protected open fun storeItem(item: T) {}

    protected open fun onNewItem(newItem: T) {
        isNeverUpdated = false
        isFresh = true

        mItem = newItem

        broadcast()
    }
}