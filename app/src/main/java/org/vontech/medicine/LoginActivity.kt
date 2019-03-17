package org.vontech.medicine

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import org.vontech.medicine.auth.UserSession
import org.vontech.medicine.auth.attemptLogin

class LoginActivity : AppCompatActivity() {

    private lateinit var app: MedicineApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        app = this.application as MedicineApplication

        setupViews()
    }

    private fun displayError(message: String) {
        Log.i("LoginActivity.kt", message)
        errorText.visibility = View.VISIBLE
        errorText.text = message
    }

    private fun submitLogin() {

        // Reset progress views
        loginProgress.visibility = View.GONE
        errorText.visibility = View.GONE

        // Retrieve and validate username and password
        val usernameValue = usernameEntry.text.toString()
        val passwordValue = passwordEntry.text.toString()

        if (usernameValue.trim().isEmpty()) {
            displayError(getString(R.string.login_username_empty_error))
            return
        }
        if (passwordValue.trim().isEmpty()) {
            displayError(getString(R.string.login_password_empty_error))
            return
        }

        // Start a login indeterminate progress indicator here
        loginProgress.visibility = View.VISIBLE

        attemptLogin(usernameValue, passwordValue, this) { session: UserSession? ->

            // Hide indeterminate progress indicator
            loginProgress.visibility = View.GONE

            // Show error message here if session is null
            if (session == null) {
                displayError(getString(R.string.login_failed_error))
                return@attemptLogin
            }

            // Store user session and continue to MainActivity if session is good
            app.buildApi(session)
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            this.finish()

        }

    }

    fun setupViews() {

        // Add listener for logging in
        submitButton.setOnClickListener {
            submitLogin()
        }

    }
}
