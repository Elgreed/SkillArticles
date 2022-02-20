package ru.skillbranch.skillarticles.data.adapters

import ru.skillbranch.skillarticles.data.local.User
import ru.skillbranch.skillarticles.extensions.asMap

class UserJsonAdapter : JsonAdapter<User>{

    override fun fromJson(json: String): User? {
        if (json.isEmpty()) return null

        val resultData = json
                .replace("{", "")
                .replace("}", "")
                .split(",")
                .map { elem -> elem.split("=")[1] }


        return User(resultData[0], resultData[1], resultData[2], Integer.valueOf(resultData[3]), Integer.valueOf(resultData[4]), resultData[5])
    }

    override fun toJson(obj: User?): String {
        return obj?.asMap().toString()
    }

}