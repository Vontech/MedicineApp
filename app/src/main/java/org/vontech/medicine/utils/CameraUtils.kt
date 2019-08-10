package org.vontech.medicine.utils

import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v7.app.AppCompatActivity
import org.vontech.medicine.EditMedicationActivity

class CameraUtils {
    val REQUEST_IMAGE_CAPTURE = 2

//    private fun openCamera() {
//
////        val intent = Intent("android.media.action.IMAGE_CAPTURE")
////        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
//
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.resolveActivity(packageManager)?.also {
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//            }
//        }
//    }
//
//    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
//            val imageBitmap = data.extras.get("data") as Bitmap
//            EditMedicationActivity.pillImageView.setImageBitmap(imageBitmap)
//        }
//    }
}