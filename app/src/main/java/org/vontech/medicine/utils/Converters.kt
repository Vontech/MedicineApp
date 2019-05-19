package org.vontech.medicine.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import org.vontech.medicine.auth.UserSession
import java.lang.Exception
import java.util.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonDeserializationContext
import org.joda.time.format.ISODateTimeFormat
import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializer
import org.joda.time.LocalTime
import java.lang.reflect.Type


var gson = getSpecialGson()

/**
 * Converts a JSON object into a UserSession object
 * @param json The JSON string to parse
 * @return the UserSession object represented by the given JSON
 */
fun responseJsonToUserSession(json: String): UserSession {
    val jObj = JSONObject(json)
    val expires = Calendar.getInstance()
    expires.add(Calendar.SECOND, jObj.getInt("expires_in"))
    return UserSession(
        jObj.getString("access_token"),
        jObj.getString("refresh_token"),
        expires
    )
}

fun userSessionToJson(userSession: UserSession): String {
    return gson.toJson(userSession)
}

fun jsonToUserSession(json: String): UserSession? {

    return try {
        gson.fromJson(json, UserSession::class.java)
    } catch (e: JsonSyntaxException) {
        null
    }

}

class LocalTimeSerializer : JsonDeserializer<LocalTime>, JsonSerializer<LocalTime> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        je: JsonElement, type: Type,
        jdc: JsonDeserializationContext
    ): LocalTime? {
        val dateAsString = je.asString
        return if (dateAsString.isEmpty()) {
            null
        } else {
            TIME_FORMAT.parseLocalTime(dateAsString)
        }
    }

    override fun serialize(
        src: LocalTime?, typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val retVal: String
        if (src == null) {
            retVal = ""
        } else {
            retVal = TIME_FORMAT.print(src)
        }
        return JsonPrimitive(retVal)
    }

    companion object {

        private val TIME_FORMAT = ISODateTimeFormat.hourMinuteSecond()
    }

}

fun getSpecialGson(): Gson {
    val builder = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, LocalTimeSerializer())
    val gson = builder.create()
    return gson
}