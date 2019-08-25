package org.vontech.medicine.utils

import android.content.Context
import android.content.SharedPreferences


fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)//context.getSharedPreferences(MED_KEY, Context.MODE_PRIVATE) //SecurePreferencesBuilder(context).build()
}