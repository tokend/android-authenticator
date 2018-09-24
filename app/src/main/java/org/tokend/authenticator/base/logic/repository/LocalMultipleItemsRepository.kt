package org.tokend.authenticator.base.logic.repository

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.CompletableSubject

/**
 * Repository of [T] items with on-device storage.
 */
abstract class LocalMultipleItemsRepository<T> : MultipleItemsRepository<T>() {
    private var updateResultSubject: CompletableSubject? = null

    private var updateDisposable: Disposable? = null
    override fun update(): Completable {
        return synchronized(this) {
            val resultSubject = updateResultSubject.let {
                if (it == null) {
                    val new = CompletableSubject.create()
                    updateResultSubject = new
                    new
                } else {
                    it
                }
            }

            isLoading = true

            updateDisposable?.dispose()
            updateDisposable =
                    itemsCache.loadFromDb()
                            .subscribeBy(
                                    onComplete = {
                                        isNeverUpdated = false
                                        isFresh = true
                                        broadcast()

                                        isLoading = false
                                        updateResultSubject = null
                                        resultSubject.onComplete()
                                    },
                                    onError = {
                                        isLoading = false
                                        errorsSubject.onNext(it)

                                        updateResultSubject = null
                                        resultSubject.onError(it)
                                    }
                            )

            resultSubject
        }
    }

    open fun add(item: T): Boolean {
        return itemsCache.add(item).also { changesOccurred ->
            if (changesOccurred) {
                broadcast()
            }
        }
    }

    open fun delete(item: T): Boolean {
        return itemsCache.delete(item).also { changesOccurred ->
            if (changesOccurred) {
                broadcast()
            }
        }
    }

    open fun update(item: T): Boolean {
        return itemsCache.update(item).also { changesOccurred ->
            if (changesOccurred) {
                broadcast()
            }
        }
    }
}