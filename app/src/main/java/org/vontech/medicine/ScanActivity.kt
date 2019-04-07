package org.vontech.medicine

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.otaliastudios.cameraview.CameraListener
import kotlinx.android.synthetic.main.activity_scan.*
import org.vontech.medicine.ocr.ScanBuilder
import org.vontech.medicine.ocr.models.getModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
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

    private fun attemptMedicationExtraction() {

        Log.i("ScanActivity.kt", "Attempting extraction!")
        val docResult = scanBuilder!!.build()
        val model = getModel(docResult)
        model?.extract(docResult).apply {
            Log.i("ScanActivity.kt", this.toString())
        }

    }

    private fun stopCapture() {

        // Stop capturing turn off all delays, and cancels all tasks
        stopCapturing = true

        // Two options - start runnable waiting for other runnable to finish,
        // or kill all runnables
        Log.i("ScanActivity.kt", "Removing all handlers")

        attemptMedicationExtraction()


    }

}
