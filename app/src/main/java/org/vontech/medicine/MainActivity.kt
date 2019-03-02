package org.vontech.medicine

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.vontech.medicine.auth.attemptLogin
import org.vontech.medicine.auth.createUser

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        attemptLogin("androidtest2", "12345", this) {userSession ->
            Log.i("MainActivity.kt", userSession.toString())
        }

    }
}
