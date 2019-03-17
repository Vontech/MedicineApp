package org.vontech.medicine

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.vontech.medicine.auth.attemptLogin
import org.vontech.medicine.auth.createUser

class MainActivity : AppCompatActivity() {

    private lateinit var app: MedicineApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        app = this.application as MedicineApplication

        // If user session is available, attempt to build an API session (await this)

        // If that fails, open up the login activity

        attemptLogin("androidtest2", "12345", this) {userSession ->
            app.buildApi(userSession)
        }

    }
}
