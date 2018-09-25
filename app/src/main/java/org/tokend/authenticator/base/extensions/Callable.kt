package org.tokend.authenticator.base.extensions

import io.reactivex.Single
import java.util.concurrent.Callable

fun <T : Any> Callable<T>.toSingle(): Single<T> = Single.fromCallable(this)
fun <T : Any> (() -> T).toSingle(): Single<T> = Single.fromCallable(this)