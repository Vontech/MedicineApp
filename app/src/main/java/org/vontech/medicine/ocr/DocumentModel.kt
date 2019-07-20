package org.vontech.medicine.ocr

import android.util.Log
import org.vontech.medicine.utils.MedicineNameDefinition
import java.util.*
import kotlin.collections.HashMap

/**
 * An abstract class providing methods to search a document for requested fields
 * @author Aaron Vontell
 */
abstract class MedicineDocumentModel {

    abstract fun modelScore(scannedDoc: DocumentScan): Float

    abstract fun extract(
        scannedDoc: DocumentScan,
        medicineNames: HashMap<String, MedicineNameDefinition>
    ): MedicineDocumentExtraction

    fun fuzzySearch(pattern: String, toSearch: String, ignoreCase: Boolean = true): MatchResult? {
        return fuzzySearchAll(pattern, toSearch, ignoreCase).toList().getOrNull(0)
    }

    fun fuzzySearchString(pattern: String, toSearch: String, ignoreCase: Boolean = true): String? {
        return fuzzySearch(pattern, toSearch, ignoreCase)?.value
    }

    fun fuzzySearchAllStrings(pattern: String, toSearch: String, ignoreCase: Boolean = true): List<String> {
        return fuzzySearchAll(pattern, toSearch, ignoreCase).toList().map{ m -> m.value}
    }

    fun fuzzySearchAll(pattern: String, toSearch: String, ignoreCase: Boolean = true): Sequence<MatchResult> {

        val regex = if (ignoreCase) {
            Regex(pattern, RegexOption.IGNORE_CASE)
        } else {
            Regex(pattern)
        }

        return regex.findAll(toSearch)

    }

    fun getLikelyMedicineNames(scannedDoc: DocumentScan, medicineNames: HashMap<String, MedicineNameDefinition>): List<String> {

        val elements = scannedDoc.elements.map { it.elements }.flatten().sortedBy { -1.0 * (it.confidence?: 0f) }
        val possibleNames = mutableListOf<String>()
        elements.forEach {
            val c = it.text.toLowerCase()
            if (c in medicineNames) {
                possibleNames.add(medicineNames[c]!!.brandName)
            }
        }

        Log.i("MEDICINE NAMES", possibleNames.toString())

        return possibleNames

    }

}

data class MedicineDocumentExtraction (
    val name: String? = null,
    val dosageAmount: Float? = null,
    val dosageType: String? = null,
    val dosageFrequency: Int? = null, // TODO: CHANGE TO ENUM
    val dosageTimes: List<Int>? = null, // TODO: CHANGE TO ENUM
    val totalQuantity: String? = null,
    val quantityType: String? = null,
    val rxIdentifier: String? = null,
    val fillDate: Date? = null, // TODO: USE JODA
    val discardDate: Date? = null, // TODO: USE JODA
    val contactNumbers: List<String>? = null,
    val contactEmails: List<String>? = null,
    val contactNames: List<String>? = null,
    val medicationDescription: String? = null,
    val applicationAction: String? = null // TODO: CHANGE TO ENUM
)

enum class MedicineField {
    NAME,
    DOSAGE_AMOUNT,
    DOSAGE_TYPE,
    DOSAGE_FREQUENCY,
    DOSAGE_TIMES,
    TOTAL_QUANTITY,
    QUANTITY_TYPE,
    RX_IDENTIFIER,
    FILL_DATE,
    DISCARD_DATE,
    CONTACT_NUMBERS,
    CONTACT_EMAILS,
    CONTACT_NAMES,
    MEDICATION_DESCRIPTION,
    APPLICATION_ACTION
}

fun remainingFields(extraction: MedicineDocumentExtraction): List<MedicineField> {
    val remaining = mutableListOf<MedicineField>()
    if (extraction.name.isNullOrBlank()) remaining.add(MedicineField.NAME)
    if (extraction.dosageAmount == null) remaining.add(MedicineField.DOSAGE_AMOUNT)
    if (extraction.dosageType == null) remaining.add(MedicineField.DOSAGE_TYPE)
    if (extraction.dosageFrequency == null) remaining.add(MedicineField.DOSAGE_FREQUENCY)
    if (extraction.dosageTimes == null) remaining.add(MedicineField.DOSAGE_TIMES)
    if (extraction.totalQuantity == null) remaining.add(MedicineField.TOTAL_QUANTITY)
    if (extraction.quantityType == null) remaining.add(MedicineField.QUANTITY_TYPE)
    if (extraction.rxIdentifier == null) remaining.add(MedicineField.RX_IDENTIFIER)
    if (extraction.fillDate == null) remaining.add(MedicineField.FILL_DATE)
    if (extraction.discardDate == null) remaining.add(MedicineField.DISCARD_DATE)
    if (extraction.contactNumbers.isNullOrEmpty()) remaining.add(MedicineField.CONTACT_NUMBERS)
    if (extraction.contactEmails.isNullOrEmpty()) remaining.add(MedicineField.CONTACT_EMAILS)
    if (extraction.contactNames.isNullOrEmpty()) remaining.add(MedicineField.CONTACT_NAMES)
    if (extraction.medicationDescription.isNullOrBlank()) remaining.add(MedicineField.MEDICATION_DESCRIPTION)
    if (extraction.applicationAction.isNullOrBlank()) remaining.add(MedicineField.APPLICATION_ACTION)
    return remaining
}

// TODO: Eventually put this into string resource file
val medicineNames = mapOf(
    MedicineField.NAME to "Medicine Name",
    MedicineField.DOSAGE_AMOUNT to "Dosage Amount",
    MedicineField.DOSAGE_TYPE to "Dosage Type",
    MedicineField.DOSAGE_FREQUENCY to "Dosage Frequency",
    MedicineField.DOSAGE_TIMES to "Dosage Times",
    MedicineField.TOTAL_QUANTITY to "Total Quantity",
    MedicineField.QUANTITY_TYPE to "Quantity Type",
    MedicineField.RX_IDENTIFIER to "Prescription No. (Rx #)",
    MedicineField.FILL_DATE to "Fill Date",
    MedicineField.DISCARD_DATE to "Expiration Date",
    MedicineField.CONTACT_NUMBERS to "Contact Phone Number",
    MedicineField.CONTACT_NAMES to "Contact Name",
    MedicineField.MEDICATION_DESCRIPTION to "Description",
    MedicineField.APPLICATION_ACTION to "Application Method"
)

data class MedicationDisplayInformation(
    val name: String,
    val value: String,
    val byline: String?
)

fun getDisplayInformation(extraction: MedicineDocumentExtraction): List<MedicationDisplayInformation> {

    val infos = mutableListOf<MedicationDisplayInformation>()
    if (!extraction.name.isNullOrBlank()) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.NAME),
                extraction.name.toString(),
                null
        ))
    }
    if (extraction.dosageAmount != null) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.DOSAGE_AMOUNT),
                extraction.dosageAmount.toString(),
                null
            ))
    }
    if (extraction.dosageType != null) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.DOSAGE_TYPE),
                extraction.dosageType.toString(),
                null
            ))
    }
    if (extraction.dosageFrequency != null) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.DOSAGE_FREQUENCY),
                extraction.dosageFrequency.toString(),
                null
            ))
    }
    if (extraction.dosageTimes != null) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.DOSAGE_TIMES),
                extraction.dosageTimes.toString(),
                null
            ))
    }
    if (extraction.totalQuantity != null) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.TOTAL_QUANTITY),
                extraction.totalQuantity.toString(),
                null
            ))
    }
    if (extraction.quantityType != null) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.QUANTITY_TYPE),
                extraction.quantityType.toString(),
                null
            ))
    }
    if (extraction.rxIdentifier != null) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.RX_IDENTIFIER),
                extraction.rxIdentifier.toString(),
                null
            ))
    }
    if (extraction.fillDate != null) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.FILL_DATE),
                extraction.fillDate.toString(),
                null
            ))
    }
    if (extraction.discardDate != null) {
        infos.add(
            MedicationDisplayInformation(
                medicineNames.getValue(MedicineField.DISCARD_DATE),
                extraction.discardDate.toString(),
                null
            ))
    }
//    if (!extraction.contactNumbers.isNullOrEmpty()) remaining.add(MedicineField.CONTACT_NUMBERS)
//    if (!extraction.contactEmails.isNullOrEmpty()) remaining.add(MedicineField.CONTACT_EMAILS)
//    if (!extraction.contactNames.isNullOrEmpty()) remaining.add(MedicineField.CONTACT_NAMES)
//    if (!extraction.medicationDescription.isNullOrBlank()) remaining.add(MedicineField.MEDICATION_DESCRIPTION)
//    if (!extraction.applicationAction.isNullOrBlank()) remaining.add(MedicineField.APPLICATION_ACTION)
    return infos

}

