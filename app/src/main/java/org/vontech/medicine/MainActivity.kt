package org.vontech.medicine

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.vontech.medicine.auth.createUser

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createUser("androidtest2", "aaron2@vontech.org", "12345", "12345")

    }
}
