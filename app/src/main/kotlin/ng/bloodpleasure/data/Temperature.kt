package ng.bloodpleasure.data

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor

/**
 * Created by Ng on 16/04/2018.
 */
object Temperature {

    private val subject: PublishProcessor<TemperatureMessage> = PublishProcessor.create()

    val flowable: Flowable<TemperatureMessage> = subject

    fun publish(source: Flowable<TemperatureMessage>): Disposable =
        source

            .onErrorResumeNext { it: Throwable ->
                Flowable.just(
                    TemperatureMessage(
                        TemperatureMessageStatus.ERROR,
                        TemperatureMessagePayload.Error(it)
                    )
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { subject.onNext(it) }

}


