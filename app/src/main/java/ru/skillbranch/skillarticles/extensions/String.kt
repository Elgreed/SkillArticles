package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr:String, ignoreCase:Boolean = true): List<Int>{
    var result = mutableListOf<Int>()
    if (substr.isNullOrBlank()) return result

    var ind = 0
    while (ind != -1){
        ind = this?.indexOf(substr, ind, ignoreCase)!!
        if (ind != -1) result.add(ind++)
        else break
    }
    return result
}