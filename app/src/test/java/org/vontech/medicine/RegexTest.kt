package org.vontech.medicine

import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import org.vontech.medicine.ocr.DATE_REGEX
import org.vontech.medicine.ocr.models.FILLED_DATE_REGEX
import org.vontech.medicine.utils.MockMedicineDocumentModel

class RegexText: FeatureSpec({

    val medDoc = MockMedicineDocumentModel()

    feature("the shared regexes") {

        scenario("should catch dates") {

            val found = medDoc.fuzzySearch(DATE_REGEX, "12/22/2018")
            found.shouldNotBeNull()
            found.value.shouldNotBeEmpty()
            found.value shouldBe "12/22/2018"

        }

        scenario("should catch dates labels with OCR errors") {

            val found = medDoc.fuzzySearch(FILLED_DATE_REGEX, "date filled.12/22/2018")
            found.shouldNotBeNull()
            found.value.shouldNotBeEmpty()
            found.groupValues[1] shouldBe "12/22/2018"

        }

    }

})