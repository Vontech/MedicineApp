package org.vontech.medicine.pokos

import java.io.Serializable

data class Medication(var name: String, var dose: Int, var notes: String) : Serializable