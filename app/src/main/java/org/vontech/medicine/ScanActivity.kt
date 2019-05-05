package org.vontech.medicine

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.otaliastudios.cameraview.CameraListener
import kotlinx.android.synthetic.main.activity_scan.*
import org.vontech.medicine.ocr.MedicineDocumentExtraction
import org.vontech.medicine.ocr.ScanBuilder
import org.vontech.medicine.ocr.models.getModel
import org.vontech.medicine.ocr.remainingFields
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.MedicationStore
import java.util.*

class ScanActivity : AppCompatActivity() {

    // Data for the current scan
    private var firstImage: String? = null
    private var scanBuilder: ScanBuilder? = null

    // Handlers and vars to reference running tasks
    private var isCapturing = false
    private var stopCapturing = false
    private var runnables = mutableListOf<Runnable>()
    private val imageTakingHandler = Handler()

    // Storage references
    private lateinit var medicationStore: MedicationStore

    // Global references
    private lateinit var app: MedicineApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        medicationStore = MedicationStore(this)
        app = this.application as MedicineApplication

        setupCamera()
        setupViews()
    }

    private fun setupCamera() {
        scanCamera.setLifecycleOwner(this)
        scanCamera.addCameraListener(object: CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                scanBuilder!!.processImage(jpeg!!) {
                    Log.i("ScanActivity.kt","PROCESSED IMAGE")
                    attemptMedicationExtraction()
                }

                // Save this image if it is the first image
                if (firstImage == null) {
                    val filename = "medz-${UUID.randomUUID()}"
                    this@ScanActivity.openFileOutput(filename, Context.MODE_PRIVATE).use {
                        it.write(jpeg)
                    }
                    firstImage = filename
                }

            }
        })
    }

    private fun setupViews() {

        scanButton.setOnClickListener {
            if (!isCapturing) {
                isCapturing = true
                scanButton.text = resources.getString(R.string.scan_button_scan_stop)
                startCapturing()
            } else {
                isCapturing = false
                scanButton.text = resources.getString(R.string.scan_button_scan)
                stopCapture()
            }

        }

    }

    private fun startCapturing() {

        scanBuilder = ScanBuilder()
        stopCapturing = false

        val runnable = object : Runnable {

            override fun run() {
                if (!stopCapturing) {
                    try {
                        runnables.add(this)
                        scanCamera.captureSnapshot()
                        //do your code here
                    } catch (e: Exception) {
                        // TODO: handle exception
                        Log.i("ScanActivity.kt", "Runnable killed: " + e.localizedMessage)
                    } finally {
                        runnables.remove(this)
                        if (!stopCapturing) {
                            imageTakingHandler.postDelayed(this, 2000)
                        }
                    }
                }
            }
        }

        imageTakingHandler.post(runnable)

    }

    private fun attemptMedicationExtraction(): MedicineDocumentExtraction? {

        Log.i("ScanActivity.kt", "Attempting extraction!")
        val docResult = scanBuilder!!.build()
        val model = getModel(docResult)
        val extraction = model?.extract(docResult, app.medicineNames)
        if (extraction != null) {
            Log.i("ScanActivity.kt", extraction.toString())
            val remaining = remainingFields(extraction)
            val toShow = "REMAINING:\n" + remaining.joinToString("\n")
            tempFields.text = toShow
        }
        return extraction
    }

    private fun moveToEditing() {
        val extracted = attemptMedicationExtraction()
        val medicineToEdit = Medication(
            dose=extracted?.dosageAmount,
            name=extracted?.name,
            notes=""
        )
        val editMedicationIntent = Intent(this, EditMedicationActivity::class.java)
        editMedicationIntent.putExtra(getString(R.string.scan_medication), medicineToEdit)
        startActivity(editMedicationIntent)
        finish()
    }

    private fun stopCapture() {

        // Stop capturing turn off all delays, and cancels all tasks
        stopCapturing = true

        // Two options - start runnable waiting for other runnable to finish,
        // or kill all runnables
        Log.i("ScanActivity.kt", "Removing all handlers")

        attemptMedicationExtraction()
        moveToEditing()


    }

}
