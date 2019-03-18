package org.vontech.medicine

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import org.vontech.medicine.auth.UserSession
import org.vontech.medicine.utils.*

/**
 * The medicine application stores global information such as the current user session
 * and API handle. If logged in, both the medicineApi and userSession fields must not be
 * null.
 *
 * 1) On app startup, application attempts to load an existing user session
 * 2) If exists, calls buildApi, which stores both the user session and api handle
 * 3) If not exists, opens login activity
 * 4) If user logs in, login activity calls buildApi with user session
 * 5) If medicineApi or userSession getters are ever called when they are null, a userLogin
 *    screen should be opened
 */
class MedicineApplication : Application() {

    var medicineApi: MedicineApi? = null
    var userSession: UserSession? = null

    override fun onCreate() {
        super.onCreate()

        // Setup the FuelManager
        FuelManager.instance.basePath = API_URL

    }

    fun attemptToLoadExistingSession(activity: Activity): Boolean {
        val prefs = this.getSharedPreferences(SHARED_PREFS_KEY, 0)

        // If key exists and session is good, store the session and return true
        if (prefs.contains(SESSION_STORE_KEY)) {
            val sessionString = prefs.getString(SESSION_STORE_KEY, "")
            if (sessionString.isNotEmpty()) {
                Log.i("MedicineApplication.kt", "Session String: $sessionString")
                val userSession = jsonToUserSession(sessionString)
                buildApi(userSession)
                return true
            }
        }

        // Otherwise, end the current activity and open the login activity
        Log.i("MedicineApplication.kt", "Starting login activity")
        activity.finish()
        val loginIntent = Intent(this, LoginActivity::class.java)
        activity.startActivity(loginIntent)

        return false
    }

    fun buildApi(userSession: UserSession?) {
        Log.i("MedicineApplication.kt", userSession.toString())
        if (userSession == null) {
            // Do something
        } else {

            // Save variables to memory
            this.userSession = userSession
            this.medicineApi = MedicineApi(userSession)

            // Save session to storage
            storeUserSession(userSession)
            Log.i("MedicineApplication.kt", "Session saved to device")

        }

    }

    private fun storeUserSession(userSession: UserSession) {

        val prefs = this.getSharedPreferences(SHARED_PREFS_KEY, 0)
        val editor = prefs.edit()

        editor.putString(SESSION_STORE_KEY, userSessionToJson(userSession))
        editor.commit() // TODO: Should we use apply?

    }

}