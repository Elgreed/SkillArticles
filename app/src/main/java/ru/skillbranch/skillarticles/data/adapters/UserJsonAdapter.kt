package ru.skillbranch.skillarticles.data.adapters

import ru.skillbranch.skillarticles.data.local.User

class UserJsonAdapter() : JsonAdapter<User> {
    override fun fromJson(json: String): User? {
        val usr:User? = null
        val user = json.split(',').map {
            val keyValue = it.replace("[\'\"]".toRegex(), "").split(':')
            val key = keyValue.first()
            val value = keyValue.last()
            key to value
        }.toMap()
        return if (user["id"] != null && user["name"] != null){
            User(
                id = user["id"]!!,
                name = user["name"]!!,
                avatar = if (user["avatar"] == "null") null else user["avatar"],
                rating = user["rating"]?.toInt() ?: 0,
                respect = user["respect"]?.toInt() ?: 0,
                about = if (user["about"] == "null") null else user["about"]
            )
        }else{
            usr
        }
    }

    override fun toJson(obj: User?): String {
        val json =
            "\\((.*)\\)".toRegex().find(obj.toString())?.groupValues?.get(1)
                ?.replace(", ", ",")
                ?.replace("[^,=]+".toRegex()) {
                    "\"" + it.value + "\""
                }?.replace("=", ":")

        return json ?: ""
    }
}