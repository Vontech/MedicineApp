package org.vontech.medicine.ocr.models

import org.vontech.medicine.ocr.*
import java.text.SimpleDateFormat
import java.util.*


// Variables for regexes
val ITEM_TYPES = listOf("tablets", "softgels", "capsules")

// Regexes for CVS 2018 Pill Bottles
const val RX_NUM_REGEX = "Rx\\s*(#\\s*\\d{7})"

val QTY_REGEX_ONE = "(\\d+ (?:${ITEM_TYPES.joinToString("|")}))"
const val QTY_REGEX_TWO = "(qty|qtv)\\s*:{0,1}\\s*(\\d+)"
val QTY_REGEX = "(?:$QTY_REGEX_ONE|$QTY_REGEX_TWO)"

// Do some fuzziness
val FILLED_DATE_REGEX = "Filled[:\\.]{0,1}\\s*($DATE_REGEX)"
val ORIGINAL_DATE_REGEX = "Orig[:\\.]{0,1}($DATE_REGEX)"
val DISCARD_DATE_REGEX = "d After[:\\.]{0,1}\\s*($DATE_REGEX)"

val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)


class CVS2018Rx: MedicineDocumentModel() {

    override fun extract(scannedDoc: DocumentScan): MedicineDocumentExtraction {
        // Extract each part of the bottle:

        val s = scannedDoc.joinedText

        val phones = this.fuzzySearchAllStrings(PHONE_REGEX, s)
        val dosageType = this.fuzzySearchString(DOSAGE_TYPE_REGEX, s)
        val rxNumber = this.fuzzySearch(RX_NUM_REGEX, s)?.groupValues?.get(1)
        val fillDateRaw = this.fuzzySearch(FILLED_DATE_REGEX, s)?.groupValues?.get(1)
        val discardDateRaw = this.fuzzySearch(DISCARD_DATE_REGEX, s)?.groupValues?.get(1)
        val fillDate: Date? = if (fillDateRaw != null) dateFormat.parse(fillDateRaw) else null
        val discardDate: Date? = if (discardDateRaw != null) dateFormat.parse(discardDateRaw) else null


        return MedicineDocumentExtraction(
            dosageType = dosageType,
            rxIdentifier = rxNumber,
            fillDate = fillDate,
            discardDate = discardDate,
            contactNumbers = phones
        )

    }

    override fun modelScore(scannedDoc: DocumentScan): Float {
        return 1.0f
    }

}


// USEFUL EXAMPLES

//DIANE RIBA WOLMAN
//hroid Levosvl. Synthroid, Unithi
//s MOUTH DAILY
//macy 0288
//NTELL
//OKINE 125 MCG
//TABLET (125
//refil before 12/28/201
//e: (860) 582-8167
//EO7

//CA DIANE RIBA WOLMAN
//macy 0288
//ROXINE 125 MCG
//ieraid Eevenvi. Symthrsvid
//1refill before 12/28/201
//507
//ONTELL
//TABLET (125
//MO DAILY

//DIANE RIBA WOLMAN
//hroid Levosvl. Synthroid, Unithi
//s MOUTH DAILY
//macy 0288
//NTELL
//OKINE 125 MCG
//TABLET (125
//refil before 12/28/201
//e: (860) 582-8167
//EO7

//TAKE THIS MEDICATON
//PLENTY OF WATER
//DONOT TAKE ANTACIDS CA
//OR IRON WITHIN 4 HRS OF T
//THIS DRUG.
//RPH: KAREN M FE
//Orig: 12/28/2016
//Date filled: 10/25/2017
//Discard After 10/25
//This is a GRAY, OBLONG
//TABLET imprinted with M
//and L 10 on the badk.