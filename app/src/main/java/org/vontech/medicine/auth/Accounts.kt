package org.vontech.medicine.auth

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.httpPost
import org.json.JSONObject
import org.vontech.medicine.R
import org.vontech.medicine.utils.responseJsonToUserSession
import java.util.*

// Constants for authentication
const val CREATE_USER_ENDPOINT = "/api/users"
const val LOGIN_USER_ENDPOINT = "/oauth/token"

data class User (
    val username: String,
    val email: String,
    val profilePictureLocation: String?
)

data class UserSession (
    val accessToken: String,
    val refreshToken: String,
    val expirationDate: Calendar
)

fun logout(userSession: UserSession) {
    throw NotImplementedError()
}

fun createUser(username: String, email: String, password: String, passwordAgain: String, onFinish: (errorMessage: String?) -> Unit) {

    val body = JSONObject(mapOf(
        "username" to username,
        "email" to email,
        "password" to password,
        "passwordConf" to passwordAgain
    ))

    try {
        CREATE_USER_ENDPOINT.httpPost()
            .header(Headers.CONTENT_TYPE, "application/json")
            .body(body.toString())
            .also { println(it) }
            .response { req, res, result ->
                when(res.statusCode) {
                    200 -> onFinish(null)
                    else -> {
                        val bod = JSONObject(String(res.data))
                        onFinish(bod.getString("message"))
                    }
                }
            }
    } catch (e: Exception) {
        Log.e("Accounts.kt", e.message)
        onFinish("An error occurred during account creation. Please try again later.")
    }


}

/**
 * Attempts a login using the given username and password. onFinish is called with a valid
 * UserSession if login was successful (otherwise null).
 * @param username The username of the user who wishes to login
 * @param password The password to use for logging in
 * @param context The Context of the calling application
 * @param onFinish A function which handles the User Session (or null result)
 */
fun attemptLogin(username: String, password: String, context: Context, onFinish: (session: UserSession?) -> Unit) {

    val body = listOf (
        "username" to username,
        "password" to password,
        "grant_type" to "password"
    )

    try {
        LOGIN_USER_ENDPOINT.httpPost(body)
            .header(mapOf(
                Headers.CONTENT_TYPE to "application/x-www-form-urlencoded",
                Headers.AUTHORIZATION to "Basic ${context.getString(R.string.MED_API_KEY)}"
            ))
            .also { println(it) }
            .responseString { result ->
                if (result.component1() == null) {
                    onFinish(null)
                } else {
                    val userSession = responseJsonToUserSession(result.component1()!!)
                    onFinish(userSession)
                }
            }
    } catch (e: Exception) {
        Log.e("Accounts.kt", e.message)
        onFinish(null)
    }



}