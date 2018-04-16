package ng.bloodpleasure.util.bt

/**
 * Created by Ng on 17/04/2018.
 */
class BytesCollector(
    val length: Int,
    var currentLength: Int = 0,
    val buffer: ByteArray = ByteArray(length)
) {
    val isCompleted: Boolean get() = currentLength == length
}