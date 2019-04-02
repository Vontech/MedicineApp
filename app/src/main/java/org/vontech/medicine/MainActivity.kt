package org.vontech.medicine

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
    private var medicineList: ArrayList<Medication> = getArrayList(R.string.medication_prefs)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = this.getSharedPreferences(getString(R.string.medication_prefs), Context.MODE_PRIVATE)

        newMedicationButton.setOnClickListener {
            val intent = Intent(this, EditMedicationActivity::class.java)
            startActivity(intent)
        }

        // Instantiate RecyclerView and set its adapter
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        adapter = RecyclerAdapter(medicineList)
        recyclerView.adapter = adapter

        app = this.application as MedicineApplication

        // If user session is available, attempt to build an API session (await this)

        // If that fails, open up the login activity

        attemptLogin("androidtest2", "12345", this) {userSession ->
            app.buildApi(userSession)
        }
    }

    fun getArrayList(key: String): ArrayList<Medication> {
//        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json = prefs.getString(key, null)
        val type = object : TypeToken<ArrayList<Medication>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveArrayList(list: ArrayList<Medication>, key: String) {
//        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }


}
