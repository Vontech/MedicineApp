package org.vontech.medicine

import android.app.Application
import com.github.kittinunf.fuel.core.FuelManager
import org.vontech.medicine.utils.API_URL

class MedicineApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        // Setup the FuelManager
        FuelManager.instance.basePath = API_URL

    }

}