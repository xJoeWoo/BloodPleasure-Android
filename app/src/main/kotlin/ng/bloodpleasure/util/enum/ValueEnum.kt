package ng.bloodpleasure.util.enum

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Created by Ng on 16/04/2018.
 */
interface ValueEnum<T> {
    @get:JsonValue
    val value: T
}