package org.vontech.medicine

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_registration.*
import org.vontech.medicine.auth.UserSession
import org.vontech.medicine.auth.attemptLogin
import org.vontech.medicine.auth.createUser

class RegistrationActivity : AppCompatActivity() {

    private lateinit var app: MedicineApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        app = this.application as MedicineApplication

        setupViews()
    }

    private fun setupViews() {

        submitRegistrationButton.setOnClickListener {
            attemptCreateAccount()
        }

    }

    private fun displayError(message: String) {
        Log.i("RegistrationActivity.kt", message)
        errorText.visibility = View.VISIBLE
        errorText.text = message
    }

    private fun loginUponSuccess(username: String, password: String) {

        attemptLogin(username, password, this) { session: UserSession? ->

            // Hide indeterminate progress indicator
            registrationProgress.visibility = View.GONE

            // Show error message here if session is null
            if (session == null) {
                displayError(getString(R.string.registration_login_error))
                return@attemptLogin
            }

            // Store user session and continue to MainActivity if session is good
            app.buildApi(session)
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            this.finish()

        }

    }

    private fun attemptCreateAccount() {

        // Reset progress views
        registrationProgress.visibility = View.GONE
        errorText.visibility = View.GONE

        // Validate fields
        val email = emailEntry.text.toString()
        val password = passwordEntry.text.toString()
        val passwordConfirm = passwordConfirmEntry.text.toString()

        // TODO: Regex validate email and passwords
        if (email.trim().isEmpty()) {
            displayError(getString(R.string.registration_username_empty_error))
            return
        }
        if (password.trim().isEmpty()) {
            displayError(getString(R.string.registration_password_empty_error))
            return
        }
        if (password != passwordConfirm) {
            displayError(getString(R.string.registration_passwords_not_matched_error))
            return
        }

        // Start a login indeterminate progress indicator here
        registrationProgress.visibility = View.VISIBLE

        createUser(email, email, password, passwordConfirm) { errorMessage ->

            if (errorMessage != null) {
                Log.i("RegistrationActivity.kt", errorMessage)
                registrationProgress.visibility = View.GONE
                displayError(errorMessage)
            } else {
                Log.i("RegistrationActivity.kt", "Creation successful!")
                loginUponSuccess(email, password)
            }

        }

    }

}
