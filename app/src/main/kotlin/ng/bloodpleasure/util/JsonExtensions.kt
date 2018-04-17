package ng.bloodpleasure.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by Ng on 17/04/2018.
 */

val gson: Gson = Gson()

fun <T> T.toJson(): String = gson.toJson(this)

inline fun <reified T> String.fromJson(): T = gson.fromJson(this, typeTokenOf<T>().type)

inline fun <reified T> typeTokenOf(): TypeToken<T> = object : TypeToken<T>() {}