package ru.skillbranch.skillarticles.extensions

import android.util.Log

fun String?.indexesOf(substr : String, ignoreCase : Boolean = true) : List<Int> {
    val result = mutableListOf<Int>()

    var index = 0

//    Log.d("Query", "indexesOf: substring ${substr} and this ${this}")

    this?.let {
        while (substr.isNotEmpty() && index < it.length && it.indexOf(substr, index, ignoreCase) != -1) {
            val subIndex = it.indexOf(substr, index, ignoreCase)
            result.add(subIndex)
            index = subIndex.inc()
        }
    }


    return result
}
