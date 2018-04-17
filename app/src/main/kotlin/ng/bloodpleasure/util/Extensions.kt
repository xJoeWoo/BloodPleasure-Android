package ng.bloodpleasure.util

import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Ng on 16/04/2018.
 */
fun <T> T.e(tag: String = "TAG"): T = also { Log.e(tag, toString()) }

fun <T> T.w(tag: String = "TAG"): T = also { Log.w(tag, toString()) }

fun Disposable?.safeDispose() {
    if (this != null && !isDisposed) dispose()
}

fun <T> Observable<T>.observeOnComputation() = observeOn(Schedulers.computation())

fun <T> Observable<T>.observeOnIO() = observeOn(Schedulers.io())

fun <T> Flowable<T>.observeOnIO() = observeOn(Schedulers.io())

fun <T> Observable<T>.observeOnComputationSubscribeOnMain() =
    observeOn(Schedulers.computation())
//        .subscribeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.observeOnIOSubscribeOnMain() =
    observeOn(Schedulers.io())
//        .subscribeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.observeOnIOSubscribeOnMain() =
    observeOn(Schedulers.io())
//        .subscribeOn(AndroidSchedulers.mainThread())

fun jsMethod(name: String, vararg params: Any?): String =
    "javascript:$name(${params.joinToString()})"

fun <T> T.toObservable(): Observable<T> = Observable.just(this)

fun <T> T.toSingle(): Single<T> = Single.just(this)

fun <T> T.toFlowable(): Flowable<T> = Flowable.just(this)
//
//fun <T> Observable<T>.publishOnTwo(): Observable<T> = publish().autoConnect(2)
//
//fun <T> Flowable<T>.publishOnTwo(): Flowable<T> = publish().autoConnect(2)