package ng.bloodpleasure.data

import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor

/**
 * Created by Ng on 16/04/2018.
 */
object Temperature {

    private val subject: PublishProcessor<TemperatureMessage> = PublishProcessor.create()

    val observe: Flowable<TemperatureMessage> get() = subject

    fun publish(source: Flowable<TemperatureData>): Disposable =
        source
            .map {
                TemperatureMessage(
                    TemperatureMessageStatus.NORMAL,
                    TemperatureMessagePayload.Data(it)
                )
            }
            .onErrorResumeNext { it: Throwable ->
                Flowable.just(
                    TemperatureMessage(
                        TemperatureMessageStatus.ERROR,
                        TemperatureMessagePayload.Error(it)
                    )
                )
            }
            .subscribe { subject.onNext(it) }

}


