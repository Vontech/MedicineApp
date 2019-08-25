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
import org.apache.commons.lang3.StringUtils
import org.joda.time.LocalTime
import java.lang.reflect.Type
import org.joda.time.DateTime
import org.joda.time.DateTimeZone




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

class DateTimeDeserializer : JsonDeserializer<DateTime>, JsonSerializer<DateTime> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        je: JsonElement, type: Type,
        jdc: JsonDeserializationContext
    ): DateTime? {
        val dateAsString = je.asString
        return if (je.asString.length == 0) null else DATE_TIME_FORMATTER.parseDateTime(dateAsString)
    }

    override fun serialize(
        src: DateTime?, typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(if (src == null) StringUtils.EMPTY else DATE_TIME_FORMATTER.print(src))
    }

    companion object {
        internal val DATE_TIME_FORMATTER: org.joda.time.format.DateTimeFormatter =
            ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC)
    }
}

fun getSpecialGson(): Gson {
    val builder = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, LocalTimeSerializer())
        .registerTypeAdapter(DateTime::class.java, DateTimeDeserializer())

    val gson = builder.create()
    return gson
}