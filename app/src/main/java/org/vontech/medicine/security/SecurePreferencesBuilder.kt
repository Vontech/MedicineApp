package org.vontech.medicine.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.securepreferences.SecurePreferences
import org.vontech.medicine.R

val KEYSTORE_PASSWORD = "MEDICINE.KEYSTORE.PASSWORD"


class SecurePreferencesBuilder(private val context: Context) {

    val PREFERENCES_FILE = context.getString(R.string.medication_prefs)

    fun build(): SharedPreferences {

        try {
            //This will only create a certificate once as it checks
            //internally whether a certificate with the given name
            //already exists.
            createKeys(this.context, KEYSTORE_PASSWORD)
        } catch (e: Exception) {
            //Probably will never happen.
            throw RuntimeException(e)
        }
        var pass = getSigningKey(KEYSTORE_PASSWORD)
        if (pass == null) {
            //This is a device less than JBMR2 or something went wrong.
            //I recommend eitehr not supporting it or fetching device hardware ID as shown below.
            //do note this is barely better than obfuscation.
            //Compromised security but may prove to be better than nothing
            pass = getDeviceSerialNumber(context)
            //bitshift everything by some pre-determined amount for added security
            pass = bitshiftEntireString(pass)
        }

        Log.i("PASSWORD", pass)

        return SecurePreferences(context, pass, PREFERENCES_FILE)

    }

}

/**
 * Bitshift the entire string to obfuscate it further
 * and make it harder to guess the password.
 */
fun bitshiftEntireString(str: String): String {
    val msg = StringBuilder(str)
    val userKey = 6
    for (i in 0 until msg.length) {
        msg.setCharAt(i, (msg[i].toInt() + userKey).toChar())
    }
    return msg.toString()
}

/**
 * Gets the hardware serial number of this device.
 *
 * @return serial number or Settings.Secure.ANDROID_ID if not available.
 * Credit: SecurePreferences for Android
 */
@SuppressLint("HardwareIds")
private fun getDeviceSerialNumber(context: Context): String {
    // We're using the Reflection API because Build.SERIAL is only available
    // since API Level 9 (Gingerbread, Android 2.3).
    try {
        val deviceSerial = Build::class.java.getField("SERIAL").get(
            null
        ) as String
        return if (TextUtils.isEmpty(deviceSerial)) {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } else {
            deviceSerial
        }
    } catch (ignored: Exception) {
        // Fall back  to Android_ID
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

}