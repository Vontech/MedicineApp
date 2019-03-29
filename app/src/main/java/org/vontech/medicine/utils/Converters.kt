package org.vontech.medicine.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import org.vontech.medicine.auth.UserSession
import java.lang.Exception
import java.util.*

var gson = Gson()

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