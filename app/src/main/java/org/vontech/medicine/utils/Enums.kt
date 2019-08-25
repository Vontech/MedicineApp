package org.vontech.medicine.utils

enum class EditState {
    READ,       // Medication exists, information displayed, not editable
    EDITING,    // Medication exists, information displayed, editable
    ADDING,     // Medication DNE, information not display, editable
    SCANNING,   // Medication DNE, information displayed, editable
}