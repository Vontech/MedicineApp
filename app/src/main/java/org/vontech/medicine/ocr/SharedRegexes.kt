package org.vontech.medicine.ocr

const val PHONE_REGEX = "(\\({0,1}\\d{3}\\){0,1}\\s*\\d{3}-\\d{4})"

val DOSAGE_TYPE = mapOf(
    "MCG" to "MCG",
    "MG" to "MG"
)
val DOSAGE_TYPE_REGEX = "(${DOSAGE_TYPE.keys.joinToString("|")})"

// TODO: Allow this to handle \d{2} at the end without leaving out digits
const val DATE_REGEX = "(?:[0][0-9]|[1][0-2])\\/(?:[0-2]\\d|[3][0-1])\\/\\d{4}"