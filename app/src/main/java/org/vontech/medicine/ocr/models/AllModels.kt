package org.vontech.medicine.ocr.models

import org.vontech.medicine.ocr.DocumentScan
import org.vontech.medicine.ocr.MedicineDocumentModel

val ALL_MODELS = listOf(CVS2018Rx())

fun getModel(scannedDoc: DocumentScan): MedicineDocumentModel? {

    var max = 0.0f
    var chosen: MedicineDocumentModel? = null
    ALL_MODELS.forEach {
        val score = it.modelScore(scannedDoc)
        if (score > max) {
            max = score
            chosen = it
        }
    }
    return chosen

}