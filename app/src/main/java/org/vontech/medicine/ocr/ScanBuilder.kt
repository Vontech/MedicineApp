package org.vontech.medicine.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText

/**
 * Handles the building of an OCR record given multiple scans of a single document.
 * @author Aaron Vontell
 */
class ScanBuilder {

    private val textParts = mutableListOf<String>()
    private val elements = mutableListOf<FirebaseVisionText.Line>()

    fun addScannedText(text: String, lines: List<FirebaseVisionText.Line>) {
        Log.i("ScanBuilder.kt", text)
        textParts.add(text)
        elements.addAll(lines)
    }

    fun build(): DocumentScan {
        return DocumentScan(
            textParts,
            textParts.joinToString("\n"),
            elements
        )
    }

    fun processImage(jpeg: ByteArray, onFinish: () -> Unit) {

        val backgroundTask = ScanProcessAsyncTask(this, onFinish)

        backgroundTask.execute(jpeg)

    }

}

class ScanProcessAsyncTask(private val scanBuilder: ScanBuilder, private val onFinish: () -> Unit): AsyncTask<ByteArray, Void, Bitmap>() {

    override fun doInBackground(vararg jpegs: ByteArray): Bitmap {

        // Convert to Bitmap
        val jpeg = jpegs[0]
        return BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)

    }

    override fun onPostExecute(result: Bitmap) {

        // Process with firebase
        val image = FirebaseVisionImage.fromBitmap(result)
        val recognizer = FirebaseVision.getInstance()
            .onDeviceTextRecognizer

        recognizer.processImage(image)
            .addOnSuccessListener { texts ->
                val lines = mutableListOf<FirebaseVisionText.Line>()
                texts.textBlocks.forEach {
                    lines.addAll(it.lines.toList())
                }
                scanBuilder.addScannedText(texts.text, lines)

                // Due to memory issue, recycle the drawable
                result.recycle()

                // Update the UI
                onFinish()

            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                e.printStackTrace()
            }

    }
}

/**
 * A completed, stitched OCR record built using the ScanBuilder
 * @author Aaron Vontell
 */
class DocumentScan(
    val textParts: List<String>,
    val joinedText: String,
    val elements: MutableList<FirebaseVisionText.Line>
)