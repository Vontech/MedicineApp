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
        val isLoggedIn = app.attemptToLoadExistingSession(this)

        if (isLoggedIn) {
            Log.i("MainActivity.kt", "Logged in!")
            Log.i("MainActivity.kt", app.userSession.toString())
        }

    }
}
