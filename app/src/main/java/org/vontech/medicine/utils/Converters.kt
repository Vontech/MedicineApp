package org.vontech.medicine.utils

import org.json.JSONObject
import org.vontech.medicine.auth.UserSession
import java.util.*

/**
 * Converts a JSON object into a UserSession object
 * @param json The JSON string to parse
 * @return the UserSession object represented by the given JSON
 */
fun jsonToUserSession(json: String): UserSession {
    val jObj = JSONObject(json)
    val expires = Calendar.getInstance()
    expires.add(Calendar.SECOND, jObj.getInt("expires_in"))
    return UserSession(
        jObj.getString("access_token"),
        jObj.getString("refresh_token"),
        expires
    )
}