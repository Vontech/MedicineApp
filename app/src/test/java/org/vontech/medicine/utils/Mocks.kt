package org.vontech.medicine.utils

import org.vontech.medicine.ocr.DocumentScan
import org.vontech.medicine.ocr.MedicineDocumentExtraction
import org.vontech.medicine.ocr.MedicineDocumentModel

class MockMedicineDocumentModel: MedicineDocumentModel() {

    override fun modelScore(scannedDoc: DocumentScan): Float {
        return 1.0f
    }

    override fun extract(scannedDoc: DocumentScan): MedicineDocumentExtraction {
        return MedicineDocumentExtraction()
    }

}