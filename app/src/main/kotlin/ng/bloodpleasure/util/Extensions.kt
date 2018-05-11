package ng.bloodpleasure.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.dimen
import java.util.*
import java.util.stream.IntStream

/**
 * Created by Ng on 16/04/2018.
 */
fun <T> T.e(tag: String = "TAG"): T = also { Log.e(tag, toString()) }

fun <T> T.w(tag: String = "TAG"): T = also { Log.w(tag, toString()) }

fun Disposable?.safeDispose() {
    if (this != null && !isDisposed) dispose()
}

fun <T> Observable<T>.observeOnComputation(): Observable<T> = observeOn(Schedulers.computation())

fun <T> Observable<T>.observeOnIO(): Observable<T> = observeOn(Schedulers.io())

fun <T> Flowable<T>.observeOnIO(): Flowable<T> = observeOn(Schedulers.io())

fun jsMethod(name: String, vararg params: Any?): String =
    "javascript:$name(${params.joinToString()})".also { it.e("JsCall") }

fun <T> T.toObservable(): Observable<T> = Observable.just(this)

fun <T> T.toFlowable(): Flowable<T> = Flowable.just(this)

val Context.isDebugging: Boolean get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

val Context.statusBarHeight: Int
    get() = dimen(resources.getIdentifier("status_bar_height", "dimen", "android"))


private val random: Random by lazy { Random() }

fun ClosedRange<Int>.random(): Int =
    random.nextInt(endInclusive - start) + start

fun ClosedRange<Int>.randoms(count: Long): IntStream =
    random.ints(count, start, (endInclusive - start))

fun Any.wrapWithSingleQuotation(): String = "\'$this\'"

fun Any.wrapWithQuotation(): String = "\"$this\""

fun ByteArray.toHex() =
    joinToString(separator = "") { it.toInt().and(0xff).toString(16).padStart(2, '0') }