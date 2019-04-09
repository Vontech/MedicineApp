package org.vontech.medicine.ocr

import java.util.*

/**
 * An abstract class providing methods to search a document for requested fields
 * @author Aaron Vontell
 */
abstract class MedicineDocumentModel {

    abstract fun modelScore(scannedDoc: DocumentScan): Float

    abstract fun extract(scannedDoc: DocumentScan): MedicineDocumentExtraction

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
    return remaining
}