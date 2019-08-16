package org.vontech.medicine.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.delete_dialog.*
import org.vontech.medicine.R

/**
 * Configure the popup confirmation dialog styling and content
 * @param title the title displayed on the dialog
 * @param message the description on the dialog
 */
fun AppCompatActivity.buildDialog(title: String, message: String): Dialog {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.delete_dialog)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.titleTextView.text = title
    dialog.messageTextView.text = message
    dialog.negativeButton.setOnClickListener { dialog.dismiss() }
    return dialog
}