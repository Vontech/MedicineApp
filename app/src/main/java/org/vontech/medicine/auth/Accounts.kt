package org.vontech.medicine.auth

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.httpPost
import org.json.JSONObject

// Constants for authentication
const val CREATE_USER_ENDPOINT = "/api/users"

data class User (
    val username: String,
    val email: String,
    val profilePictureLocation: String?
)

data class UserSession (
    val accessToken: String,
    val refreshToken: String,
    val expirationDate: String
)

fun authenticate(username: String, password: String): UserSession {
    throw NotImplementedError()
}

fun logout(userSession: UserSession) {
    throw NotImplementedError()
}

fun createUser(username: String, email: String, password: String, passwordAgain: String) {

    val body = JSONObject(mapOf(
        "username" to username,
        "email" to email,
        "password" to password,
        "passwordConf" to passwordAgain
    ))

    CREATE_USER_ENDPOINT.httpPost()
        .header(Headers.CONTENT_TYPE, "application/json")
        .body(body.toString())
        .also { println(it) }
        .response { result -> println(result) }

}