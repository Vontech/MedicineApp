package org.vontech.medicine

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import org.vontech.medicine.auth.attemptLogin
import org.vontech.medicine.pokos.Medication

class MainActivity : AppCompatActivity() {

    private lateinit var app: MedicineApplication
    private lateinit var prefs: SharedPreferences
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private lateinit var medicineList: ArrayList<Medication>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = this.getSharedPreferences(getString(R.string.medication_prefs), Context.MODE_PRIVATE)
        medicineList = getArrayList(getString(R.string.medication_list))

        newMedicationButton.setOnClickListener {
            val intent = Intent(this, EditMedicationActivity::class.java)
            startActivity(intent)
        }

        Log.d("Medications", medicineList.toString())
        scanMedicationButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        // Instantiate RecyclerView and set its adapter
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        adapter = RecyclerAdapter(medicineList)
        recyclerView.adapter = adapter

        app = this.application as MedicineApplication
        val isLoggedIn = app.attemptToLoadExistingSession(this)

        if (isLoggedIn) {
            Log.i("MainActivity.kt", "Logged in!")
            Log.i("MainActivity.kt", app.userSession.toString())
        }
    }

    private fun getArrayList(key: String): ArrayList<Medication> {
//        val gson = Gson()
        val json = prefs.getString(key, null)
//        val type = object : TypeToken<ArrayList<Medication>>() {}.type

        val turnsType = object : TypeToken<ArrayList<Medication>>() {}.type
        if (Gson().fromJson<ArrayList<Medication>>(json, turnsType) == null) {
            return arrayListOf()
        } else {
            return Gson().fromJson<ArrayList<Medication>>(json, turnsType)
        }

//        return gson.fromJson(json, type)
    }
}
