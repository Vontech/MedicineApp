package org.vontech.medicine.utils

import android.content.Context
import com.opencsv.CSVReader
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

val MEDICINE_ASSET = "medicines.txt.bz2"

data class MedicineNameDefinition(
    val brandName: String,
    val technicalName: String
)

class MedicineReader(private val context: Context) {

    fun readMedicines(): HashMap<String, MedicineNameDefinition> {

        val am = context.assets
        val input = am.open(MEDICINE_ASSET)
        val decompressed = BZip2CompressorInputStream(input)
        val csvReader = CSVReader(decompressed.reader())
        var nextLine = csvReader.readNext()

        val lookup = HashMap<String, MedicineNameDefinition>()
        while (nextLine != null) {
            (nextLine[0].toLowerCase().split("\\s*") + nextLine[0].toLowerCase().split("\\s*")).forEach {
                lookup[it] = MedicineNameDefinition(nextLine[0], nextLine[1])
            }
            nextLine = csvReader.readNext()
        }

        return lookup

    }

}