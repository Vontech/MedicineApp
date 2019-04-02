package org.vontech.medicine.pokos

import java.io.Serializable
import java.util.*

data class Medication(var name: String, var dose: Int, var notes: String) : Serializable {
    val id = UUID.randomUUID().toString()
}