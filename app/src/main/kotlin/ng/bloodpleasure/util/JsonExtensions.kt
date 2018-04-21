package ng.bloodpleasure.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Created by Ng on 17/04/2018.
 */


val objectMapper: ObjectMapper = ObjectMapper()

fun <T> T.toJson(): String = objectMapper.writeValueAsString(this)

inline fun <reified T> String.fromJson(): T = objectMapper.readValue(this, ref<T>())

inline fun <reified T> ref(): TypeReference<T> = object : TypeReference<T>() {}