package org.vontech.medicine

import android.app.Application
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import org.vontech.medicine.auth.UserSession
import org.vontech.medicine.utils.API_URL

class MedicineApplication : Application() {

    private var medicineApi: MedicineApi? = null

    override fun onCreate() {
        super.onCreate()

        // Setup the FuelManager
        FuelManager.instance.basePath = API_URL

    }

    fun attemptToLoadExistingSession() {

    }

    fun buildApi(userSession: UserSession?) {
        Log.i("MedicineApplication.kt", userSession.toString())
        if (userSession == null) {
            // Do something
        } else {
            medicineApi = MedicineApi(userSession)
        }

    }

    fun getApi(): MedicineApi {

        if (medicineApi == null) {
            // Do something here, like show a dialog
        }

        return medicineApi!!
    }

}