package org.vontech.medicine

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.otaliastudios.cameraview.CameraListener
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.scan_result_item.view.*
import org.vontech.medicine.ocr.*
import org.vontech.medicine.ocr.models.getModel
import org.vontech.medicine.pokos.Medication
import org.vontech.medicine.utils.EditState
import org.vontech.medicine.utils.MedicationStore
import java.util.*
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.util.TypedValue
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth









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
                Log.i("ScanActivity.kt", "PICTURE TAKEN")
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

        medicineScanHeader.setOnClickListener {
            if (!isCapturing) {
                isCapturing = true
                startCapturing()
            } else {
                isCapturing = false
                stopCapture()
            }

        }



    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Set correct distance for bottom scanning view
        val scanHeight = scanContainer.height

        val params = separationView.layoutParams

        val r = resources
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            42f,
            r.displayMetrics
        )

        params.height = scanHeight - scanOverviewContainer.height - px.toInt()
        separationView.layoutParams = params
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
            Log.e("RESULTS", toShow)
            updateResultViews(extraction)
        }
        return extraction
    }

    private fun updateResultViews(extraction: MedicineDocumentExtraction) {

        val remainingFields = remainingFields(extraction)
        val viewsToShow = mutableListOf<View>()

        // Update progress bar
        scanProgressBar.max = 3
        scanProgressBar.progress = 3 - remainingFields.size

        if (remainingFields.isEmpty()) {
            moveToEditing()
            return
        }

        val remaining: MedicineField = remainingFields.first()

        // First attach remaining
        scanCallToAction.text = "Looking for " + medicineNames[remaining]
        Log.i("RESULTS", "Adding as remaining: " + medicineNames[remaining])

        // Attach found views
        getDisplayInformation(extraction).forEach {
            val resultView = LayoutInflater.from(this).inflate(R.layout.scan_result_item, null)
            resultView.scan_result_name.text = it.name
            resultView.scan_result_description.text = it.value
            Log.i("RESULTS", "Adding as result: " + it.name)
            viewsToShow.add(resultView)
        }


        // Show all views
        scanResultsList.removeAllViews()
        Log.i("RESULTS", viewsToShow.size.toString())
        viewsToShow.forEach {
            scanResultsList.addView(it)
        }

    }

    private fun moveToEditing() {
        val docResult = scanBuilder!!.build()
        val model = getModel(docResult)
        val extracted = model?.extract(docResult, app.medicineNames)

        // Attempt to get a dosage type
        var dosageType = DosageType.MG
        try {
            dosageType = DosageType.valueOf(extracted?.dosageType!!.toUpperCase())
        } catch (e: Exception) {
            // Do nothing, it is just a bad unit type
        }

        val medicineToEdit = Medication(
            dose=extracted?.dosageAmount,
            doseType=dosageType,
            name=extracted?.name,
            notes=""
        )
        val editMedicationIntent = Intent(this, EditMedicationActivity::class.java)
        editMedicationIntent.putExtra(this.getString(R.string.edit_screen_state), EditState.SCANNING)
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
